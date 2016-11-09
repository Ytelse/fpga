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
  class Counter(start: Int, max: Int) extends Module {
    val io = new Bundle {
      val enable = Bool().asInput
      val rst = Bool().asInput
      val value = UInt().asOutput
    }

    val startValue = UInt(start, width=UInt(max - 1).getWidth)
    val v = Reg(init = startValue)
    when (io.rst) {
      v := startValue
    } .otherwise {
      v := Mux(io.enable, v + UInt(1), v)
    }
    io.value := v
  }

  class Switch(init: Boolean = false) extends Module {
    val stateReg = Reg(init=Bool(init))
    val io = new Bundle {
      val signalOn = Bool().asInput
      val state = Bool().asOutput
      val rst = Bool().asInput
    }

    when (io.signalOn) {
      stateReg := Bool(!init)
    } .elsewhen (io.rst) {
      stateReg := io.signalOn
      } .otherwise {
        stateReg := stateReg
      }
    if (init) {
      io.state := stateReg && ~io.signalOn
    } else {
      io.state := stateReg || io.signalOn
    }
  }

  val passesRequired = p.MatrixHeight / p.NumberOfPUs
  val cyclesPerPass = p.MatrixWidth / p.K
  val totalActiveCycles = passesRequired * cyclesPerPass
  val lastActiveCycle = totalActiveCycles - 1
  val PUsPerMUs = p.NumberOfPUs / p.NumberOfMS
  val firstReadyCycle = totalActiveCycles + PUsPerMUs - 2
  val firstOutputCycleInPass = cyclesPerPass - 1

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
  // Counters
  val cycleInPass  = Module(new Counter(0, cyclesPerPass))
  val cycle        = Module(new Counter(0, firstReadyCycle))
  val tailCycle    = Module(new Counter(0, p.NumberOfPUs))
  val selectX      = Module(new Counter(0, p.NumberOfPUs))
  // Switches
  val isActive     = Module(new Switch())
  val isReady      = Module(new Switch(true))
  val isOutputting = Module(new Switch())
  val isTailing    = Module(new Switch())
  // Signals
  val signalWaiting         = Bool(false)
  val signalLastActiveCycle = cycle.io.value === UInt(lastActiveCycle)
  val signalFirstReadyCycle = cycle.io.value === UInt(firstReadyCycle)
  val signalOutputtingNext  = cycleInPass.io.value === UInt(firstOutputCycleInPass)
  val signalDone            = tailCycle.io.value === UInt(p.NumberOfPUs - 1)
  val signalResetSelectX    = selectX.io.value === UInt(p.NumberOfPUs - 1)
  val signalStartNewPass    = cycleInPass.io.value === UInt(0)
  val signalLastCycleInPass = cycleInPass.io.value === UInt(cyclesPerPass - 1)
  val signalTailingNext     = signalLastActiveCycle

  // Counters
  cycleInPass.io.enable := isActive.io.state
  cycleInPass.io.rst    := signalLastCycleInPass
  cycle.io.enable       := isActive.io.state
  cycle.io.rst          := signalLastActiveCycle
  tailCycle.io.enable   := isTailing.io.state
  tailCycle.io.rst      := signalDone
  selectX.io.enable     := isOutputting.io.state
  selectX.io.rst        := signalResetSelectX

  // Switches
  isActive.io.signalOn := io.start
  isActive.io.rst := signalLastActiveCycle
  isReady.io.signalOn := io.start
  isReady.io.rst := signalFirstReadyCycle
  isOutputting.io.signalOn := signalOutputting
  isOutputting.io.rst := signalResetSelectX
  isTailing.io.signalOn := signalTailing
  isTailing.io.rst := signalDone


  io.selectX := selectX.io.value
  io.valid := isOutputting.io.state
  io.ready := isReady.io.state && io.nextReady
  io.done := signalDone
  io.chainRestart := signalStartNewPass
}
