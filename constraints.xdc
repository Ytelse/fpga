## Clock signal

set_property -dict { PACKAGE_PIN D4    IOSTANDARD LVCMOS33 } [get_ports { CLK100MHZ }]; #IO_L12P_T1_MRCC_35 Sch=gclk[100]
create_clock -add -name sys_clk_pin -period 10.00 -waveform {0 5} [get_ports { CLK100MHZ }];

##Quad SPI Flash

#set_property -dict { PACKAGE_PIN L12   IOSTANDARD LVCMOS33 } [get_ports { qspi_cs }]; #IO_L6P_T0_FCS_B_14 Sch=qspi_cs
#set_property -dict { PACKAGE_PIN J13   IOSTANDARD LVCMOS33 } [get_ports { qspi_dq[0] }]; #IO_L1P_T0_D00_MOSI_14 Sch=qspi_dq[0]
#set_property -dict { PACKAGE_PIN J14   IOSTANDARD LVCMOS33 } [get_ports { qspi_dq[1] }]; #IO_L1N_T0_D01_DIN_14 Sch=qspi_dq[1]
#set_property -dict { PACKAGE_PIN K15   IOSTANDARD LVCMOS33 } [get_ports { qspi_dq[2] }]; #IO_L2P_T0_D02_14 Sch=qspi_dq[2]
#set_property -dict { PACKAGE_PIN K16   IOSTANDARD LVCMOS33 } [get_ports { qspi_dq[3] }]; #IO_L2N_T0_D03_14 Sch=qspi_dq[3]

##FPGA_EP

#set_property -dict { PACKAGE_PIN R16 IOSTANDARD LVCMOS33 } [get_ports { EP[0] }]; #IO_L9N_T1_DQS_D13_14=EP[0]
#set_property -dict { PACKAGE_PIN T14 IOSTANDARD LVCMOS33 } [get_ports { EP[1] }]; #IO_L10P_T1_D14_14=EP[1]
#set_property -dict { PACKAGE_PIN T15 IOSTANDARD LVCMOS33 } [get_ports { EP[2] }]; #IO_L10N_T1_D15_14=EP[2]
#set_property -dict { PACKAGE_PIN N13 IOSTANDARD LVCMOS33 } [get_ports { EP[3] }]; #IO_L11P_T1_SRCC_14=EP[3]
#set_property -dict { PACKAGE_PIN P13 IOSTANDARD LVCMOS33 } [get_ports { EP[4] }]; #IO_L11N_T1_SRCC_14=EP[4]
#set_property -dict { PACKAGE_PIN N14 IOSTANDARD LVCMOS33 } [get_ports { EP[5] }]; #IO_L12P_T1_MRCC_14=EP[5]
#set_property -dict { PACKAGE_PIN P14 IOSTANDARD LVCMOS33 } [get_ports { EP[6] }]; #IO_L12N_T1_MRCC_14=EP[6]
#set_property -dict { PACKAGE_PIN N11 IOSTANDARD LVCMOS33 } [get_ports { EP[7] }]; #IO_L13P_T2_MRCC_14=EP[7]
#set_property -dict { PACKAGE_PIN N12 IOSTANDARD LVCMOS33 } [get_ports { EP[8] }]; #IO_L13N_T2_MRCC_14=EP[8]
#set_property -dict { PACKAGE_PIN P10 IOSTANDARD LVCMOS33 } [get_ports { EP[9] }]; #IO_L14P_T2_SRCC_14=EP[9]
#set_property -dict { PACKAGE_PIN P11 IOSTANDARD LVCMOS33 } [get_ports { EP[10] }]; #IO_L14N_T2_SRCC_14=EP[10]
#set_property -dict { PACKAGE_PIN R12 IOSTANDARD LVCMOS33 } [get_ports { EP[11] }]; #IO_L15P_T2_DQS_RDWR_B_14=EP[11]
#set_property -dict { PACKAGE_PIN T12 IOSTANDARD LVCMOS33 } [get_ports { EP[12] }]; #IO_L15N_T2_DQS_DOUT_CSO_B_14=EP[12]
#set_property -dict { PACKAGE_PIN R13 IOSTANDARD LVCMOS33 } [get_ports { EP[13] }]; #IO_L16P_T2_CSI_B_14=EP[13]
#set_property -dict { PACKAGE_PIN T13 IOSTANDARD LVCMOS33 } [get_ports { EP[14] }]; #IO_L16N_T2_A15_D31_14=EP[14]
#set_property -dict { PACKAGE_PIN R10 IOSTANDARD LVCMOS33 } [get_ports { EP[15] }]; #IO_L17P_T2_A14_D30_14=EP[15]

##Reset Button

#set_property -dict { PACKAGE_PIN T10 IOSTANDARD LVCMOS33 } [get_ports { reset }]; #IO_L22N_T3_A04_D20_14=reset

##EBI-A BUS

#set_property -dict { PACKAGE_PIN T10 IOSTANDARD LVCMOS33 } [get_ports { EBIa[0] }]; #IO_L6N_T0_VREF_15=EBIa[0]
#set_property -dict { PACKAGE_PIN E12 IOSTANDARD LVCMOS33 } [get_ports { EBIa[1] }]; #IO_L13P_T2_MRCC_15=EBIa[1]
#set_property -dict { PACKAGE_PIN D13 IOSTANDARD LVCMOS33 } [get_ports { EBIa[2] }]; #IO_L12P_T1_MRCC_15=EBIa[2]
#set_property -dict { PACKAGE_PIN C12 IOSTANDARD LVCMOS33 } [get_ports { EBIa[3] }]; #IO_L11N_T1_SRCC_15=EBIa[3]
#set_property -dict { PACKAGE_PIN C14 IOSTANDARD LVCMOS33 } [get_ports { EBIa[4] }]; #IO_L8P_T1_AD10P_15=EBIa[4]
#set_property -dict { PACKAGE_PIN A13 IOSTANDARD LVCMOS33 } [get_ports { EBIa[5] }]; #IO_L7N_T1_AD2P_15=EBIa[5]
#set_property -dict { PACKAGE_PIN B14 IOSTANDARD LVCMOS33 } [get_ports { EBIa[6] }]; #IO_L8N_T1_AD10N_15=EBIa[6]
#set_property -dict { PACKAGE_PIN A15 IOSTANDARD LVCMOS33 } [get_ports { EBIa[7] }]; #IO_L9N_T1_DQS_AD3N_15=EBIa[7]
#set_property -dict { PACKAGE_PIN E11 IOSTANDARD LVCMOS33 } [get_ports { EBIa[8] }]; #IO_L14P_T2_SRCC_15=EBIa[8]
#set_property -dict { PACKAGE_PIN E13 IOSTANDARD LVCMOS33 } [get_ports { EBIa[9] }]; #IO_L13N_T2_MRCC_15=EBIa[9]
#set_property -dict { PACKAGE_PIN C11 IOSTANDARD LVCMOS33 } [get_ports { EBIa[10] }]; #IO_L11P_T1_SRCC_15=EBIa[10]
#set_property -dict { PACKAGE_PIN C13 IOSTANDARD LVCMOS33 } [get_ports { EBIa[11] }]; #IO_L12N_T1_MRCC_15=EBIa[11]
#set_property -dict { PACKAGE_PIN C16 IOSTANDARD LVCMOS33 } [get_ports { EBIa[12] }]; #IO_L10P_T1_AD11P_15=EBIa[12]
#set_property -dict { PACKAGE_PIN A14 IOSTANDARD LVCMOS33 } [get_ports { EBIa[13] }]; #IO_L7N_T1_AD2N_15=EBIa[13]
#set_property -dict { PACKAGE_PIN B15 IOSTANDARD LVCMOS33 } [get_ports { EBIa[14] }]; #IO_L9P_T1_DQS_AD3P_15=EBIa[14]
#set_property -dict { PACKAGE_PIN B16 IOSTANDARD LVCMOS33 } [get_ports { EBIa[15] }]; #IO_L10N_T1_AD11N_15=EBIa[15]

##EBI-D BUS

#set_property -dict { PACKAGE_PIN H13 IOSTANDARD LVCMOS33 } [get_ports { EBId[0] }]; #IO_L20N_T3_A19_15=EBId[0]

