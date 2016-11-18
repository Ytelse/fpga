--
--  USB test application
--
--  There are two test modes: text mode and binary mode.
--  The initial mode is text mode.
--
--  In text mode, the device reads bytes from the host until it receives
--  a carriage return character. It responds by sending the received byte
--  sequence in reverse order followed by carriage return and linefeed.
--  The device then goes back to reading data from the host.
--  If the host sends more than 1020 bytes between carriage returns, only
--  the first 1020 bytes are returned.
--
--  In text mode, the following byte values have a special meaning:
--    0x00 : ignored
--    0x01 : switch to binary mode
--    0x02 : reset device and reconnect to the USB bus
--    0x04 : enable TXCORK flag
--    0x05 : disable TXCORK flag (also done automatically if TX buffer full)
--    0x06 : send a synchronous stream
--    0x07 : send an asynchronous blast
--
--  In binary mode, the device reads a stream of requests from the host.
--  A request consists of two bytes: C and V.
--  If (C > 0), the device reponds by sending C bytes with values counting
--  up from V.
--  If (C == 0) and (V > 0), the device sleeps for ~ (V/60.0) seconds.
--  If (C == 0) and (V == 0), the device switches to text mode.
--
--  For example, given the request stream [ 3, 2, 0, 3, 1, 1 ],
--  the device responds by sending [ 2, 3, 4 ] then sleeping for 0.05 seconds,
--  then sending [ 1 ].
--
--  In stream mode, the device autonomously sends a sequence of 64*1024*1024
--  bytes. Flow control is applied by pausing the data sequence when the
--  transmit buffer is full. The payload is an incrementing sequence,
--  starting from 0 and wrapping at 253. The device switches back to text
--  mode at the end of the stream.
--
--  In blast mode, the device autonomously sends 64*1024*1024 bytes without
--  regard for flow control. If the transmit buffer is full, bytes are lost.
--  Data byte rate is 25 MHz.
--

library ieee;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.usb_pkg.all;

entity InputBridge is

    port(
        rxdat:  out std_logic_vector(7 downto 0);
        rxval : out std_logic;
        rxrdy : in std_logic;
        
	PHY_DATABUS16_8 : out std_logic;
	PHY_RESET :       out std_logic;
	PHY_XCVRSELECT :  out std_logic;
	PHY_TERMSELECT :  out std_logic;
	PHY_OPMODE :      out std_logic_vector(1 downto 0);
	PHY_LINESTATE :   in  std_logic_vector(1 downto 0);
	PHY_CLKOUT :      in  std_logic;
	PHY_TXVALID :     out std_logic;
	PHY_TXREADY :     in  std_logic;
	PHY_RXVALID :     in  std_logic;
	PHY_RXACTIVE :    in  std_logic;
	PHY_RXERROR :     in  std_logic;
	PHY_DATAIN :      in  std_logic_vector(7 downto 0);
	PHY_DATAOUT :     out std_logic_vector(7 downto 0));

end entity InputBridge;

architecture arch of InputBridge is

    constant RXBUFSIZE_BITS : integer := 11;
    constant TXBUFSIZE_BITS : integer := 7;
    --constant BLAST_DUTY_OFF : integer := 7;
    --constant BLAST_DUTY_ON :  integer := 5;

    -- Clock signal
    signal CLK : std_logic;

    -- USB interface signals
    signal usb_devreset :   std_logic;
    signal usb_busreset :   std_logic;
    signal usb_highspeed :  std_logic;
    signal usb_suspend :    std_logic;
    signal usb_online :     std_logic;
    signal usb_rxval :      std_logic;
    signal usb_rxdat :      std_logic_vector(7 downto 0);
    signal usb_rxrdy :      std_logic;
    signal usb_rxlen :      std_logic_vector(RXBUFSIZE_BITS-1 downto 0);
    signal usb_txval :      std_logic;
    signal usb_txdat :      std_logic_vector(7 downto 0);
    signal usb_txrdy :      std_logic;
    signal usb_txroom :     std_logic_vector(TXBUFSIZE_BITS-1 downto 0);

    -- General purpose counter
    signal s_txcork : std_logic := '0';


    -- Things i have tried myself.
    signal lastByte : std_logic_vector(7 downto 0) := (others => '0');

begin

    -- Direct interface to serial data transfer component
    usb_serial_inst : usb_serial
        generic map (
            VENDORID        => X"4ac3",
            PRODUCTID       => X"a200",
            VERSIONBCD      => X"0031",
            VENDORSTR       => "Pacman company",
            PRODUCTSTR      => "The parallel awesome captcha machine associating numbers",
            SERIALSTR       => "17112016",
            HSSUPPORT       => true,
            SELFPOWERED     => true,
            RXBUFSIZE_BITS  => RXBUFSIZE_BITS,
            TXBUFSIZE_BITS  => TXBUFSIZE_BITS )
        port map (
            CLK             => CLK,
            RESET           => usb_devreset,
            USBRST          => usb_busreset,
            HIGHSPEED       => usb_highspeed,
            SUSPEND         => usb_suspend,
            ONLINE          => usb_online,
            RXVAL           => usb_rxval,
            RXDAT           => usb_rxdat,
            RXRDY           => usb_rxrdy,
            RXLEN           => usb_rxlen,
            TXVAL           => usb_txval,
            TXDAT           => usb_txdat,
            TXRDY           => usb_txrdy,
            TXROOM          => usb_txroom,
            TXCORK          => s_txcork,
            PHY_DATAIN      => PHY_DATAIN,
            PHY_DATAOUT     => PHY_DATAOUT,
            PHY_TXVALID     => PHY_TXVALID,
            PHY_TXREADY     => PHY_TXREADY,
            PHY_RXACTIVE    => PHY_RXACTIVE,
            PHY_RXVALID     => PHY_RXVALID,
            PHY_RXERROR     => PHY_RXERROR,
            PHY_LINESTATE   => PHY_LINESTATE,
            PHY_OPMODE      => PHY_OPMODE,
            PHY_XCVRSELECT  => PHY_XCVRSELECT,
            PHY_TERMSELECT  => PHY_TERMSELECT,
            PHY_RESET       => PHY_RESET );

    -- 60 MHz clock signal
    CLK <= PHY_CLKOUT;

    -- Configure USB PHY
    PHY_DATABUS16_8 <= '0';		-- 8 bit mode

    --leds(7 downto 0) <= lastByte(7 downto 0);--usb_rxdat(7 downto 0);

    -- Assign signals to USB component
    usb_devreset <= '0';
    usb_txval <= '0';
    usb_txdat <= (others => '0');

    s_txcork <= '0';
    
    usb_rxrdy <= rxrdy;
    rxval <= usb_rxval;
    rxdat <= usb_rxdat;
    
    --process is
    --begin
    --    wait until rising_edge(CLK);
    --    if usb_rxval = '1' and usb_online = '1' then
    --        lastByte <= usb_rxdat;
    --    end if;
    --end process;

end architecture arch;
