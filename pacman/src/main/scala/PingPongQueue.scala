package Pacman

import Chisel._

class PingPongQueue(data_width : Int, queue_size : Int) extends module
{
	val io = new Bundle
	{
		val data_in = UInt(INPUT,data_width)
		val valid_trans = Bool(INPUT)
		val write_en = Bool(INPUT)
		val reset = Bool(INPUT)
		
		val isFull = Bool(OUTPUT)
		val data_out = UInt(OUTPUT,data_width)
	}
	
	val adr_reg = Reg(init = UInt(0,log2Up(queue_size)))
	val adr_reg_incremented = adr_reg + UInt(1)
	val at_end = adr_reg === queue_size - 1	
	val enable_count = io.valid || (!io.write_en)
	val memory = Mem(UInt(width = data_width), queue_size, true) //TODO might not be sequential read

	when(reset || at_end)
	{
		adr_reg := UInt(0)
	}
	.elsewhen(enable_count)
	{
		adr_reg := adr_reg_incremented
	}
	
	when(io.write_en)
	{
		memory(adr_reg) := io.data_in
	}

	io.data_out := memory(adr_reg)
}

