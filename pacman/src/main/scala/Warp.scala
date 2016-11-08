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
  val preprocessedBiases = biases.map(b => b / 2)
  val (w, b) = MemoryLayout.getStreams(parameters, weights, preprocessedBiases)
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
  io.start      <> control.io.start
  io.ready      <> control.io.ready
  io.xOut.ready <> control.io.nextReady
  io.xOut.valid <> control.io.valid
  io.done       <> control.io.done
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
  val PUsPerMUs = p.NumberOfPUs / p.NumberOfMS
  val cyclesBeforeReady = totalCycles + PUsPerMUs - 2
  val cyclesBeforeDone = totalCycles + p.NumberOfPUs // TODO
  val cyclesBeforeBurst = cyclesPerPass

  val io = new Bundle {
    val ready = Bool().asOutput
    val start = Bool().asInput
    val nextReady = Bool().asInput
    val valid = Bool().asOutput
    val done = Bool().asOutput

    val selectX = UInt().asOutput
    val memoryRestart = Bool().asOutput
    val chainRestart = Bool().asOutput
  }

  val runningReg = Reg(init=Bool(false))
  val readyReg = Reg(init=Bool(true))

  val totalCycleCounter = Module(new Counter(0, cyclesBeforeReady - 1))
  val cycleInPassCounter = Module(new Counter(0, cyclesPerPass - 1))
  val selectXCounter = Module(new Counter(0, p.NumberOfPUs))
  val doneCounter = Module(new Counter(0, cyclesBeforeDone))

  val isRunning = runningReg || io.start
  val signalReady = totalCycleCounter.io.value === UInt(cyclesBeforeReady - 1)
  val isReady = readyReg
  val signalFinished = (totalCycleCounter.io.value === UInt(cyclesBeforeReady - 1))
  val signalNextPass = cycleInPassCounter.io.value === UInt(cyclesPerPass - 1)
  val signalBurst = cycleInPassCounter.io.value === UInt(cyclesBeforeBurst - 1)
  val isValid = !(selectXCounter.io.value === UInt(p.NumberOfPUs))
  val signalDone = !(doneCounter.io.value === UInt(cyclesBeforeDone - 1))

  when(io.start) {
    runningReg := Bool(true)
  }.elsewhen(signalFinished) {
    runningReg := Bool(false)
  }.otherwise {
    runningReg := runningReg
  }

  when(io.start) {
    readyReg := Bool(false)
  }.elsewhen(signalReady) {
    readyReg := Bool(true)
  }.otherwise {
    readyReg := readyReg
  }

  cycleInPassCounter.io.rst := signalNextPass
  cycleInPassCounter.io.enable := isRunning

  totalCycleCounter.io.rst := (isReady && !io.start) || signalReady
  totalCycleCounter.io.enable := isRunning

  selectXCounter.io.rst := signalBurst
  selectXCounter.io.enable := signalBurst || isValid

  doneCounter.io.rst := io.start
  doneCounter.io.enable := io.start || signalDone

  io.ready := isReady && io.nextReady
  io.valid := isValid
  io.done := signalDone

  io.selectX := selectXCounter.io.value
  io.memoryRestart := (isReady && !io.start) || signalReady
  io.chainRestart := cycleInPassCounter.io.value === UInt(0)
}
