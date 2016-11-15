package Pacman

import Chisel._

class NetLedHarness(
  layers: List[LayerData],
  testInput: Array[Array[Int]]
) extends Module {
  if(layers.last.parameters.NumberOfCores != 1) {
    throw new AssertionError("NetLedHarness only support single output")
  }
  if(layers(0).parameters.NumberOfCores != 1) {
    throw new AssertionError("NetLedHarness only support single input")
  }

  val waitCycles = 100000000
  val numberOfInputWords = layers(0).parameters.MatrixWidth / layers(0).parameters.K

  val io = new Bundle {
    val leds = Vec.fill(4)(Bits(width=1)).asOutput
  }

  val net = Module(new Net(layers))

  val bitBuffer = Module(new BitToWord(layers.last.parameters.MatrixHeight))
  bitBuffer.io.enable := net.io.xsOutValid
  bitBuffer.io.bit := net.io.xsOut(0)
  io.leds := OHToUInt(bitBuffer.io.word)

  val inputCycleCounter = Module(new CounterWithSyncAndAsyncReset(0, numberOfInputWords))
  val waitCounter = Module(new Counter(0, waitCycles))
  val signalNewInput = waitCounter.io.value === UInt(waitCycles-1)

  inputCycleCounter.io.enable := Bool(true)
  inputCycleCounter.io.asyncRst :=  signalNewInput
  inputCycleCounter.io.syncRst := inputCycleCounter.io.value === UInt(numberOfInputWords - 1)

  val currentInput = Module(new AsyncCounter(0, testInput.length))
  val testInputROMs = Vec(
    testInput.map(
      input => Vec(input.map(
                     bit => Bits(bit)
                   )
        .grouped(layers(0).parameters.K)
        .map(_.reverse.reduceLeft((a, b) => Cat(a, b))).toArray
      )
    )
  )

  for(i <- 0 until 10) {
    println(
      testInput(i).grouped(28).map(_.mkString).mkString("\n")
    )
    println()
  }

  val nextData = Reg(init=Bits(width=layers(0).parameters.K))
  when(currentInput.io.value === UInt(0)) {
    nextData := testInputROMs(0)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(1)) {
    nextData := testInputROMs(1)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(2)) {
    nextData := testInputROMs(2)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(3)) {
    nextData := testInputROMs(3)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(4)) {
    nextData := testInputROMs(4)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(5)) {
    nextData := testInputROMs(5)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(6)) {
    nextData := testInputROMs(6)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(7)) {
    nextData := testInputROMs(7)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(8)) {
    nextData := testInputROMs(8)(inputCycleCounter.io.value)
  }.elsewhen(currentInput.io.value === UInt(9)) {
    nextData := testInputROMs(9)(inputCycleCounter.io.value)
  }.otherwise {
    nextData := nextData
  }

  waitCounter.io.enable := Bool(true)
  waitCounter.io.rst := signalNewInput

  currentInput.io.enable := signalNewInput

  net.io.start := ShiftRegister(signalNewInput, 1)
  net.io.xsIn := Vec(nextData)
  net.io.pipeReady := Bool(true)
}
