package Pacman

import Chisel._

class NetSimulationHarnessTests(
  c: NetSimulationHarness,
  layers: List[LayerData],
  testInputs: Array[Array[Int]],
  testOutputs: Array[Array[Int]]
) extends Tester(c) {
  poke(c.io.start, false)
  poke(c.io.xInValid, false)

  def vecToBigInt(vec: Seq[Int]): BigInt = {
    int(Bits(vec.reverse.map((n) => n.toString).fold("b")(_ + _)))
  }

  testOutputs.foreach(l => {
    print(l.indexWhere(_ == 1))
    print(" ")
  })
  println("")

  val numTests = testInputs.length
  val defaultPush = 50
  var pushedTests = 0
  var Cycle = 0;

  def push(num: Int) {
    testInputs.slice(pushedTests, pushedTests + num)
              .foreach(test => {
                test.grouped(layers(0).parameters.K).foreach(chunk => {
                  poke(c.io.xIn(0), vecToBigInt(chunk))
                  poke(c.io.xInValid, true)
                  step(1)
                  Cycle += 1
                  poke(c.io.xInValid, false)
                })
              })
    pushedTests += num
  }

  push(100)

  poke(c.io.start, true)
  step(1)
  poke(c.io.start, false)

  while (peek(c.io.inputCount) < numTests) {
    if (pushedTests - peek(c.io.inputCount) < defaultPush) {
      val toPush = Math.min(defaultPush, numTests - pushedTests)
      push(toPush)
    }
    step(1000)
    Cycle += 1000
    print("pushed %d/%d".format(pushedTests, testInputs.length))
  }
  poke(c.io.start, false)

  while (peek(c.io.done) == 0x0) { step(1) }
  for (i <- 0 until testInputs.length) {
    expect(peekAt(c.outputMem, i) == vecToBigInt(testOutputs(i)), "Image #%d".format(i))
  }
  println("Total cycles: %d".format(Cycle))
  println("  %5.2f cycles per image".format(Cycle.toFloat / testInputs.length))
}

object NetSimulation {
  def main(args: Array[String]) {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")

    val testData = Utils.readDumpFile()
    val testInput = testData.vectors.map(_(0)).toArray
    val testOutput = testData.vectors.map(_(4)).toArray
    val parametersList = Array(
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 32,
        AddressWidth = 0,
        NumberOfMS = 16,
        MatrixWidth = 784,
        MatrixHeight = 256,
        NumberOfCores = 1
      ),
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 16,
        AddressWidth = 0,
        NumberOfMS = 8,
        MatrixWidth = 256,
        MatrixHeight = 256,
        NumberOfCores = 1
      ),
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 16,
        AddressWidth = 0,
        NumberOfMS = 8,
        MatrixWidth = 256,
        MatrixHeight = 256,
        NumberOfCores = 1
      ),
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 10,
        AddressWidth = 0,
        NumberOfMS = 5,
        MatrixWidth = 256,
        MatrixHeight = 10,
        NumberOfCores = 1
      )
    )
    val layers = Range(0, 4).map(i =>
      new LayerData(
        parameters=parametersList(i),
        weights=testData.matrices(i),
        biases=testData.biases(i)
      )
    ).toList

    chiselMainTest(margs, () => Module(new NetSimulationHarness(layers, testInput.length, 128))) {
      c => new NetSimulationHarnessTests(c, layers, testInput, testOutput)
    }
  }
}
