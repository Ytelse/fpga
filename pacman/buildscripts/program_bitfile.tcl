if {$argc != 1} {
    return -code error {Bitfile must be supplied as an argument}
}
set bitfile [lindex $argv 0]

open_hw
set hn [connect_hw_server]
puts "Connected to hardware server $hn"
open_hw_target

set_property PROGRAM.FILE "${bitfile}" [current_hw_device]

puts "Programming bitfile: $bitfile"
program_hw_device

close_hw
