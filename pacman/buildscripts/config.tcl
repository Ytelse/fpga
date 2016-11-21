if {$argc != 1} {
    return -code error {Bitfile must be supplied as an argument}
}
set bitfile [lindex $argv 0]
set binfile [lindex $argv 1]
set prmfile [lindex $argc 2]

puts "Writing binfile: $binfile"
write_cfgmem -format bin -interface SPIx4 -size 16 -loadbit "up 0x0 $bitfile" -force -file $binfile

open_hw
set hn [connect_hw_server]
puts "Connected to hardware server $hn"
open_hw_target

set my_mem_device [lindex [get_cfgmem_parts {n25q256-3.3v-spi-x1_x2_x4}] 0]
# Create a hardware configuration memory object and associate it with the
# hardware device. Also, set a variable with which to point to this object
set my_hw_cfgmem [create_hw_cfgmem -hw_device [lindex [get_hw_devices] 0] -mem_dev $my_mem_device]

# Set the address range used for erasing to the size of the programming file
set_property PROGRAM.ADDRESS_RANGE  {use_file} $my_hw_cfgmem]

# Set the programming file to program into the SPI flash
set_property PROGRAM.FILES $binfile $my_hw_cfgmem]
set_property PROGRAM.PRM_FILE $prmfile $my_hw_cfgmem]

# Set the termination of unused pins when programming the SPI flash
set_property PROGRAM.UNUSED_PIN_TERMINATION {pull-none} $my_hw_cfgmem

# Configure the hardware device with the programming bitstream
program_hw_devices [lindex [get_hw_devices] 0]

# Set programming options
set_property PROGRAM.BLANK_CHECK  0 $my_hw_cfgmem]
set_property PROGRAM.ERASE  1 $my_hw_cfgmem]
set_property PROGRAM.CFG_PROGRAM  1 $my_hw_cfgmem]
set_property PROGRAM.VERIFY  1 $my_hw_cfgmem]
set_property PROGRAM.CHECKSUM  0 $my_hw_cfgmem]

puts "Programming config memory."
program_hw_cfgmem -hw_cfgmem $my_hw_cfgmem]

close_hw
