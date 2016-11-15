package Pacman

import Chisel._

class NetSimulationHarness(
  layers: List[LayerData], numberOfTestInputs: Int
) extends Module {

  val firstLayer = layers(0)
  val lastLayer = layers.last
  val inputWordSize = firstLayer.parameters.K
  val parallelInputs = firstLayer.parameters.NumberOfCores
  val totalInputWidth = inputWordSize * parallelInputs
  val inputCycles = firstLayer.parameters.MatrixWidth / firstLayer.parameters.K
  val outputWordSize = lastLayer.parameters.K
  val parallelOutputs = lastLayer.parameters.NumberOfCores
  val totalOutputWidth = outputWordSize * parallelOutputs

  val io = new Bundle {
    val xIn = Vec.fill(parallelInputs)(Bits(width=totalInputWidth)).asInput
    val xInValid = Bool().asInput
    val start = Bool().asInput
  }

  /*
   * Input
   */
  val catXIn = io.xIn.reduceLeft(Cat(_, _))

  val net = Module(new Net(layers))

  val testInputMem = Mem(Bits(width=totalInputWidth), numberOfTestInputs, true)

  val writeAddr = Module(new Counter(0, numberOfTestInputs))
  writeAddr.io.enable := io.xInValid
  writeAddr.io.rst := writeAddr.io.value === UInt(numberOfTestInputs - 1)

  when(io.xInValid) {
    testInputMem(writeAddr.io.value) := catXIn
  }

  val hasStarted = Module(new Switch())
  hasStarted.io.signalOn := io.start
  hasStarted.io.rst := Bool(false)

  val signalNewInput = net.io.ready && hasStarted.io.state

  val inputCycleCounter = Module(new CounterWithSyncAndAsyncReset(0, inputCycles))
  inputCycleCounter.io.enable := Bool(true)
  inputCycleCounter.io.syncRst := inputCycleCounter.io.value === UInt(inputCycles - 1)
  inputCycleCounter.io.asyncRst := signalNewInput

  val inputOffset = Module(new AsyncCounter(0, numberOfTestInputs * totalInputWidth, totalInputWidth))
  inputOffset.io.enable := signalNewInput

  val readAddr = ShiftRegister(inputOffset.io.value + inputCycleCounter.io.value, 1)
  val readData = testInputMem(readAddr)

  net.io.xsIn := Vec(
    Range(0, parallelInputs)
      .map(i => {
             val upper = (i + 1) * totalInputWidth - 1
             val lower = i * totalInputWidth
             readData(upper, lower)
           }).toArray
  )

  net.io.start := ShiftRegister(signalNewInput, 1)


  /*
   * Output
   */

  val outputMem = Mem(Bits(width=totalOutputWidth), numberOfTestInputs, true)
  val bitBuffers = Array.fill(parallelOutputs)(Module(new BitToWord(outputWordSize)))
  bitBuffers.zipWithIndex.foreach{
    case (b, i) => {
      b.io.bit := net.io.xsOut(i)
      b.io.enable := net.io.xsOutValid
    }
  }

}
