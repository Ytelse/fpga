package Pacman

import Chisel._

class CoreTests(c: Core, p: LayerParameters) extends Tester(c) {
}

object CoreTest {
  def main(args: Array[String]) {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    val p = new LayerParameters(K=4, BiasWidth=8, AccumulatorWidth=10,
      NumberOfPUs=4, MatrixWidth=64);

    chiselMainTest(margs, () => Module(new Core(p))) {
      c => new CoreTests(c, p)
    }
  }
}
