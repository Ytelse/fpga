package Pacman

import Chisel._

class AToN(width_in : Int, width_out : Int) extends Module
{
	val io = new Bundle
	{
		val a_data = UInt(INPUT, width_in)
		val a_rdy = Bool(INPUT)
		val a_val = Bool(OUTPUT)

		val n_data = UInt(OUTPUT, width_out)
		val n_rdy = Bool(OUTPUT)
		val n_val = Bool(INPUT)
	}
	if(width_in == width_out)
	{
		io.n_data := io.a_data
		io.n_rdy := io.a_rdy
		io.a_val := io.n_val
	}
	else if(width_in < width_out)
	{
		var number_of_registers = width_out / width_in
		//TODO make sure that width_in divides width_out evenly
		var regs:Array[UInt] = new Array[UInt](number_of_registers)
		val cnt_reg = Reg(init = UInt(0,number_of_registers)) //TODO this could use fewer bits
		val cnt_reg_incremented = cnt_reg + UInt(1)
		val last_input = cnt_reg === UInt(number_of_registers)
		val a_value = ~last_input
		val valid_a_trans = io.a_rdy && a_value
		val valid_n_trans = io.n_val && last_input

		when(valid_n_trans)
		{
			cnt_reg := UInt(0)
		}
		.elsewhen(valid_a_trans)
		{
			cnt_reg := cnt_reg_incremented
		}
		.otherwise{cnt_reg := cnt_reg}
		
		for (i <- 0 until number_of_registers)
		{
			regs(i) = Reg(init = UInt(0,width_in))
		}

		when(valid_a_trans)
		{
			regs(0) := io.a_data
		}
		.otherwise{regs(0) := regs(0)}
		for (i <- 1 until number_of_registers)
		{
			when(valid_a_trans)
			{
				regs(i) := regs(i-1)
			}
			.otherwise{regs(i) := regs(i)}
		}
		io.n_data := UInt(0)
		for (i <- 0 until number_of_registers)
		{
			io.n_data((i+1)*width_in,i*width_in) := regs(number_of_registers - 1 - i)
		}

		io.a_val := a_value
		io.n_rdy := last_input
	}
	else
	{
		var number_of_registers = width_in / width_out
		var regs:Array[UInt] = new Array[UInt](number_of_registers)
		val cnt_reg = Reg(init = UInt(0,number_of_registers)) //TODO bit width can be smaller
		val cnt_incremented = cnt_reg + UInt(1)
		val last_output = cnt_reg === UInt(number_of_registers)		
		val n_rdy = !last_output
		val valid_a_trans = io.a_rdy && last_output
		val valid_n_trans = io.n_val && n_rdy

		when(valid_n_trans)
		{
			cnt_reg := cnt_incremented
		}
		.elsewhen(valid_a_trans)
		{
			cnt_reg := UInt(0)
		}

		for (i <- 0 until number_of_registers)
		{
			regs(i) = Reg(UInt(0,width_out))
		}

		for(i <- 0 until number_of_registers)
		{
			when(valid_a_trans)
			{
				regs(i) := io.a_data((i+1)*width_out, i*width_out)
			}
		}

		for(i <- 0 until number_of_registers)
		{
			when(cnt_reg === UInt(i))
			{
				io.n_data := regs(i)
			}
		}
	}
}
