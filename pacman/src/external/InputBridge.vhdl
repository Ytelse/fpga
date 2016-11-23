
library ieee;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.usb_pkg.all;

entity InputBridge is

    port(
        rxdat:  out std_logic_vector(7 downto 0);
        rxval : out std_logic;
        rxrdy : in std_logic;
        ready : out std_logic;
        
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

    constant waitCycles : integer := 60000000 * 30;
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
    signal clock_count : std_logic_vector(31 downto 0) := (others => '0');
    signal wait_over : std_logic := '0';
    
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
    rxval <= usb_rxval when wait_over = '1' else '0';
    rxdat <= usb_rxdat;
    ready <= wait_over;
    
    process is
    begin
        wait until rising_edge(CLK);
        clock_count <= std_logic_vector(unsigned(clock_count) + 1);
        if(unsigned(clock_count) = waitCycles) then
            wait_over <= '1';
        end if;
        if(usb_online = '0') then
            clock_count <= (others => '0');
            wait_over <= '0';
        end if;
    end process;

end architecture arch;
