package Pacman

import Chisel._

class Pacman extends Module {
  val testData = Utils.readDumpFile()
  val testInput = testData.vectors.map(_(0)).toArray
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

  val netTestHarness = Module(new NetLedHarness(layers, testInput))
  val io = new Bundle {
    val test_led = Vec.fill(4){Bits(width = 1)}.asOutput
  }

  io.test_led := netTestHarness.io.leds
}

object Main {
  def main(args: Array[String]) {
    // Utils.readDumpFile()
    chiselMainTest(args, () => Module(new Pacman)) { bl =>
      new Tester(bl)
    }
  }
}
