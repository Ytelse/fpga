package Pacman

import Chisel._


class PacmanWrapper() extends Module
{
	val queueSize = 16;
	val io = new Bundle()
	{
		val usb_data = Decoupled(Bits(INPUT, width = 8)).flip
		val net_result = Decoupled(UInt(OUTPUT, width = 4))
		val ulpi_clk = Clock(INPUT)
	}

	io.ulpi_clk.setName("ulpi_clk")
	val AsyncFifo_inst = Module(new AsyncFifo(Bits(width = 8), queueSize, io.ulpi_clk, Driver.implicitClock))
	val Pacman_inst = Module(new Pacman(8))	

	AsyncFifo_inst.io.enq <> io.usb_data
	Pacman_inst.io.inDataStream <> AsyncFifo_inst.io.deq
	io.net_result <> Pacman_inst.io.digitOut
}


object PacmanWrapperTest
{
	def main(args: Array[String]): Unit =
	{
		val margs = Array("--backend", "v", "--genHarness", "--compile")
		chiselMain(margs, () => Module(new PacmanWrapper()))
	}
}

