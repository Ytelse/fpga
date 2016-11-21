
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity top is
  Port (CLK100MHZ : in std_logic; 
        ulpi_data  : inout  std_logic_vector(7 downto 0);
	   --ulpi_data_out : out std_logic_vector(7 downto 0) := x"00";
	   ulpi_dir      : in  std_logic;
    	ulpi_nxt      : in  std_logic;
    	ulpi_stp      : out std_logic := '1';
	   ulpi_reset    : out std_logic := '1';
	   ulpi_clk60    : in  std_logic;
	   led : out std_logic_vector(7 downto 0));
end top;

architecture Behavioral of top is
    component PacmanWrapper
        port(   io_usb_data_bits : in std_logic_vector(7 downto 0);
                io_usb_data_ready : out std_logic;
                io_usb_data_valid : in std_logic;
                io_net_result_bits : out std_logic_vector(3 downto 0);
                io_net_result_ready : in std_logic;
                io_net_result_valid : out std_logic;
                clk : in std_logic;
                ulpi_clk : in std_logic;
                reset : in std_logic);
    end component;

    signal LEDS :              std_logic_vector(7 downto 0);
    signal PHY_DATABUS16_8 :  std_logic;
    signal PHY_RESET :        std_logic;
    signal PHY_XCVRSELECT :   std_logic;
    signal PHY_TERMSELECT :   std_logic;
    signal PHY_OPMODE :       std_logic_vector(1 downto 0);
    signal PHY_LINESTATE :     std_logic_vector(1 downto 0);
    signal PHY_CLKOUT :        std_logic;
    signal PHY_TXVALID :      std_logic;
    signal PHY_TXREADY :       std_logic;
    signal PHY_RXVALID :       std_logic;
    signal PHY_RXACTIVE :      std_logic;
    signal PHY_RXERROR :       std_logic;
    signal PHY_DATAIN :        std_logic_vector(7 downto 0);
    signal PHY_DATAOUT :      std_logic_vector(7 downto 0);
    
    signal temp_data_out : std_logic_vector(7 downto 0);
    signal temp_data_in : std_logic_vector(7 downto 0);
    
    signal usb_rxdat : std_logic_vector(7 downto 0);
    signal usb_rxrdy : std_logic;
    signal usb_rxval : std_logic;
    signal n_dat : std_logic_vector(3 downto 0);
    signal n_rdy : std_logic;
    signal n_val : std_logic;
    signal led_reg : std_logic_vector(7 downto 0);
    
    signal ready : std_logic;
    signal temp_clock : std_logic;
begin

    temp_clock <= CLK100MHZ;

    ulpi_data <= temp_data_out when ulpi_dir = '0'
        else (others => 'Z');
        
    temp_data_in <= ulpi_data when ulpi_dir = '1'
        else (others => '0');

    led <= not led_reg;
    
    -- a small test
    n_rdy <= '1';
    process is
    begin
        wait until rising_edge(temp_clock);
        if(ready = '1') then
            led_reg(7 downto 4) <= (others => '0');
        else
            led_reg(7 downto 4) <= (others => '1');
        end if; 
        if n_val = '1' then
            led_reg(3 downto 0) <= n_dat(3 downto 0);
        end if;
    end process;
    --end a small test
    
    PacmanWrapper_inst : PacmanWrapper
        port map(
            io_usb_data_bits => usb_rxdat,
            io_usb_data_ready => usb_rxrdy,
            io_usb_data_valid => usb_rxval,
            io_net_result_bits => n_dat,
            io_net_result_ready => n_rdy,
            io_net_result_valid => n_val,
            clk => temp_clock,
            ulpi_clk => ulpi_clk60,
            reset => '0'
            );

    inputBridge_inst : entity work.InputBridge
     port map(
     rxdat => usb_rxdat,
     rxval => usb_rxval,
     rxrdy => usb_rxrdy,
     ready => ready,
             
    PHY_DATABUS16_8 => PHY_DATABUS16_8,
    PHY_RESET =>       PHY_RESET,
    PHY_XCVRSELECT =>  PHY_XCVRSELECT,
    PHY_TERMSELECT =>  PHY_TERMSELECT,
    PHY_OPMODE =>      PHY_OPMODE,
    PHY_LINESTATE =>   PHY_LINESTATE,
    PHY_CLKOUT =>      PHY_CLKOUT,
    PHY_TXVALID =>     PHY_TXVALID,
    PHY_TXREADY =>     PHY_TXREADY,
    PHY_RXVALID =>     PHY_RXVALID,
    PHY_RXACTIVE =>    PHY_RXACTIVE,
    PHY_RXERROR =>     PHY_RXERROR,
    PHY_DATAIN =>      PHY_DATAIN,
    PHY_DATAOUT =>     PHY_DATAOUT);
    
    ulpi_port_inst : entity work.ulpi_port
    port map(
        ulpi_data_in  => temp_data_in,
        ulpi_data_out => temp_data_out,
        ulpi_dir      => ulpi_dir,
        ulpi_nxt      => ulpi_nxt,
        ulpi_stp      => ulpi_stp,
        ulpi_reset    => ulpi_reset,
        ulpi_clk60    => ulpi_clk60,
    
        utmi_reset       => PHY_RESET,
        utmi_xcvrselect  => PHY_XCVRSELECT,
        utmi_termselect  => PHY_TERMSELECT,
        utmi_opmode      => PHY_OPMODE,
        utmi_linestate   => PHY_LINESTATE,
        utmi_clkout      => PHY_CLKOUT,
        utmi_txvalid     => PHY_TXVALID,
        utmi_txready     => PHY_TXREADY,
        utmi_rxvalid     => PHY_RXVALID,
        utmi_rxactive    => PHY_RXACTIVE,
        utmi_rxerror     => PHY_RXERROR,
        utmi_host_datapd => '0',
        utmi_drv_vbus    => '0',
        utmi_datain      => PHY_DATAIN,
        utmi_dataout     => PHY_DATAOUT
    );

end Behavioral;
