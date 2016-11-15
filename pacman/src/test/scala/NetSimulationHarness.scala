package Pacman

import Chisel._

class NetSimulationHarness(
  layers: List[LayerData], numberOfTestInputs: Int, bufferLength: Int
) extends Module {

  val firstLayer = layers(0)
  val lastLayer = layers.last
  val inputWordSize = firstLayer.parameters.K
  val parallelInputs = firstLayer.parameters.NumberOfCores
  val totalInputWidth = inputWordSize * parallelInputs
  val inputCycles = firstLayer.parameters.MatrixWidth / firstLayer.parameters.K
  val outputWordSize = lastLayer.parameters.MatrixHeight
  val parallelOutputs = lastLayer.parameters.NumberOfCores
  val totalOutputWidth = outputWordSize * parallelOutputs
  val inputBlockSize = totalInputWidth * inputCycles

  val io = new Bundle {
    val xIn = Vec.fill(parallelInputs)(Bits(width=totalInputWidth)).asInput
    val xInValid = Bool().asInput
    val inputCount = UInt().asOutput
    val start = Bool().asInput
    val done = Bool().asOutput
    val mem = Vec.fill(1){ UInt(width=totalOutputWidth) }.asOutput
  }

  /*
   * Input
   */
  val catXIn = io.xIn.reduceLeft(Cat(_, _))

  val net = Module(new Net(layers))
  net.io.pipeReady := Bool(true)

  val hasStarted = Module(new Switch())
  hasStarted.io.signalOn := io.start
  hasStarted.io.rst := Bool(false)

  val inputCounter = Module(new Counter(0, numberOfTestInputs))
  val signalNewInputShifted = ShiftRegister(net.io.ready && hasStarted.io.state && (inputCounter.io.value < UInt(numberOfTestInputs)), 1)
  val signalNewInput = net.io.ready && hasStarted.io.state && (inputCounter.io.value < UInt(numberOfTestInputs)) && !signalNewInputShifted

  val queue = Module(new CircularPeekQueue(inputCycles, bufferLength + 1, totalInputWidth))
  queue.io.input := catXIn
  queue.io.writeEnable := io.xInValid
  queue.io.nextBlock := signalNewInput

  inputCounter.io.enable := signalNewInput
  inputCounter.io.rst := Bool(false)
  io.inputCount := inputCounter.io.value

  net.io.xsIn := Vec(
    Range(0, parallelInputs)
      .map(i => {
             val upper = (i + 1) * totalInputWidth - 1
             val lower = i * totalInputWidth
             queue.io.output(upper, lower)
           }).toArray
  )

  net.io.start := ShiftRegister(signalNewInput, 1)

  /*
   * Output
   */
  val outputMem = Mem(Bits(width=totalOutputWidth), numberOfTestInputs, true)

  for (i <- 0 until 1) {
    io.mem(i) := UInt(outputMem(i))
  }

  val bitBuffers = Array.fill(parallelOutputs)(Module(new BitToWord(outputWordSize)))
  bitBuffers.zipWithIndex.foreach{
    case (b, i) => {
      b.io.bit := net.io.xsOut(i)
      b.io.enable := net.io.xsOutValid
    }
  }

  val signalNewOutput = ShiftRegister(net.io.done, 1)
  val outputCounter = Module(new Counter(0, numberOfTestInputs))
  outputCounter.io.enable := signalNewOutput
  outputCounter.io.rst := Bool(false)
  when(signalNewOutput) {
    outputMem(outputCounter.io.value) := bitBuffers.map(_.io.word).reduceLeft(Cat(_, _))
  }

  io.done := outputCounter.io.value === UInt(numberOfTestInputs)
}
