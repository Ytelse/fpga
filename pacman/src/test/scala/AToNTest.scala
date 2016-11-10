package Pacman

import Chisel._

class AToNTests(c: AToN, width_in : Int, width_out : Int) extends Tester(c)
{
	println("Testing with width in:",width_in,"and width out:",width_out)
	if(width_in == width_out)
	{
		poke(c.io.a_data,0)
		poke(c.io.n_val,false)
		poke(c.io.a_rdy,false)
		step(1)
		expect(c.io.n_data,0)
		expect(c.io.a_val,false)
		expect(c.io.n_rdy,false)
		step(1)
		poke(c.io.a_data,0x3f)
		poke(c.io.n_val,false)
		poke(c.io.a_rdy,false)
		expect(c.io.n_data,0x3f)
		expect(c.io.a_val,false)
		expect(c.io.n_rdy,false)
		step(1)
	
		poke(c.io.a_data,0xaf)
		poke(c.io.n_val,true)
		poke(c.io.a_rdy,false)
		expect(c.io.n_data,0xaf)
		expect(c.io.a_val,true)
		expect(c.io.n_rdy,false)
		step(1)

		poke(c.io.a_data,0x01)
		poke(c.io.n_val,false)
		poke(c.io.a_rdy,true)
		expect(c.io.n_data,0x01)
		expect(c.io.a_val,false)
		expect(c.io.n_rdy,true)
		step(1)
	}
	else if(width_in < width_out)
	{
		//make shure the input responds correctly
		poke(c.io.a_data,0)
		poke(c.io.n_val,false)
		poke(c.io.a_rdy,false)
		step(1)
		expect(c.io.n_data,0)
		expect(c.io.a_val,true)
		expect(c.io.n_rdy,false)
		step(1)

		poke(c.io.n_val,true)
		step(1)
		expect(c.io.n_data,0)
		expect(c.io.a_val,true)
		expect(c.io.n_rdy,false)		
		step(1)
		val inputVal = 0xa5c6
		var inputMask = 0
		var outputMask = 0
		for (i <- 0 until width_in){
			inputMask = inputMask | (1 << i)
		}
		for (i <- 0 until width_out){
			outputMask = outputMask | (1 << i)
		}
		
		for(i <- 0 until width_out/width_in)
		{
			val sendVal = (inputVal >>> (i * width_in)) & inputMask
			poke(c.io.a_data,sendVal)
			step(1)
			expect(c.io.a_val,true)
			expect(c.io.n_rdy,false)
			step(1)
			poke(c.io.a_rdy,true)
			step(1)
			if(i == (width_out/width_in) - 1)
			{
				expect(c.io.a_val,false)
				expect(c.io.n_rdy,true)
				expect(c.io.n_data,inputVal & outputMask)
				poke(c.io.a_rdy, false)
				step(1)
				expect(c.io.a_val,true)
				expect(c.io.n_rdy,false)
				step(1)
			}
			else
			{
				expect(c.io.a_val,true)
				expect(c.io.n_rdy,false)
				poke(c.io.a_rdy,false)
				step(1)
			}
		}
	}
	else
	{
		step(1)
	}
	
}

object AToNTest
{
	def test(width_in: Int, width_out : Int)
	{
		val margs = Array("--backend", "c", "--genHarness","--compile","--test")
		
		chiselMainTest(margs,() => Module(new AToN(width_in, width_out)))
		{
			c => new AToNTests(c, width_in, width_out)
		}
		println("Done with this test")
	}
	def main(args: Array[String]): Unit =
	{
		println("Testing equal size on input and output")
		test(3,3)
		test(8,8)

		println("Testing width in smaller than width out");
		test(3,9)
		test(4,8)

		println("Testing width in greater than width out")
		test(9,3)
		test(8,4)
	}
}
