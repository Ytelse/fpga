package Pacman

import Chisel._

class Warp(parameters: LayerParameters,
           weights: Array[Array[Int]],
           biases: Array[Int]) extends Module {

  val io = new Bundle {
    val xIn = Vec.fill(parameters.NumberOfCores)
              { Bits(width=parameters.K) }.asInput
    val start = Bool().asInput
    val ready = Bool().asOutput
    val xOut = Decoupled(Vec.fill(parameters.NumberOfCores)
               { Bits(width=1) })
    val done = Bool().asOutput
  }

  val control = Module(new WarpControl(parameters))

  val chains = List.fill(parameters.NumberOfCores)
              { Module(new Chain(parameters)) }
  val activators = List.fill(parameters.NumberOfCores)
              { Module(new Activation(parameters)) }
  val (w, b) = MemoryLayout.getStreams(parameters, weights, biases)
  val memoryStreamer = Module(new MemoryStreamer(parameters, w, b))

  // Connect chains to activators
  chains.zip(activators).foreach({case (c, a) => { a.io.in := c.io.ys }})
  // Connect input vector, weights, and biases to the chains
  for (i <- 0 until parameters.NumberOfCores) {
    chains(i).io.xs := io.xIn(i)
    chains(i).io.weights := memoryStreamer.io.weights
    chains(i).io.bias := memoryStreamer.io.bias
  }

  // Hook up output from activators to module output
  for (i <- 0 until parameters.NumberOfCores) {
    io.xOut.bits(i) := activators(i).io.out(control.io.selectX)
  }

  // Hook up control
  io.start      <> control.io.inStart
  io.ready      <> control.io.inReady
  io.xOut.ready <> control.io.outReady
  io.xOut.valid <> control.io.outValid
  io.done       <> control.io.outDone
  chains.foreach(c => c.io.restartIn := control.io.chainRestart)
  memoryStreamer.io.restart := control.io.memoryRestart
}

class WarpControl(p: LayerParameters) extends Module {
  class Counter(start : Int, max : Int) extends Module {
    val io = new Bundle {
      val enable = Bool().asInput
      val rst = Bool().asInput
      val value = UInt().asOutput
    }

    val startValue = UInt(start, width=UInt(max).getWidth)
    val v = Reg(init = startValue)
    when(io.enable) {
      v := Mux(io.rst, startValue, v + UInt(1))
    }.otherwise {
      v := v
    }
    io.value := v
  }

  val passesRequired = p.MatrixHeight / p.NumberOfPUs
  val cyclesPerPass = p.MatrixWidth / p.K
  val totalCycles = passesRequired * cyclesPerPass

  val io = new Bundle {
    val inStart = Bool().asInput
    val inReady = Bool().asOutput

    val outReady = Bool().asInput
    val outValid = Bool().asOutput
    val outDone = Bool().asOutput

    val selectX = UInt().asOutput
    val memoryRestart = Bool().asOutput
    val chainRestart = Bool().asOutput
  }

  val totalCycleCounter = Module(new Counter(0, totalCycles - 1))
  val cycleInPassCounter = Module(new Counter(0, cyclesPerPass - 1))
  val passCounter = Module(new Counter(0, passesRequired - 1))
  val finished = (totalCycleCounter.io.value === UInt(totalCycles - 1))
  val nextPass = cycleInPassCounter.io.value === UInt(cyclesPerPass - 1)

  cycleInPassCounter.io.rst := nextPass && !finished
  cycleInPassCounter.io.enable := !finished

  passCounter.io.rst := io.inStart
  passCounter.io.enable := nextPass && !finished

  totalCycleCounter.io.rst := Bool(false)
  totalCycleCounter.io.enable := !(totalCycleCounter.io.value === UInt(totalCycles - 1))

  io.inReady := Bool(false)
  io.outValid := Bool(false)
  io.outDone := Bool(false)

  io.selectX := UInt(0)
  io.memoryRestart := Bool(true)
  io.chainRestart := io.inStart
}
