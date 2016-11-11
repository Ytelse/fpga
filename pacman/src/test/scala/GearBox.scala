package Pacman

import Chisel._

class GearBoxTests(c: GearBox, p: GearBoxParameters) extends Tester(c) {

}

object GearBoxTest {
  def main(args: Array[String]) {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    // Random.setSeed(12)
    val p = new GearBoxParameters(
                new LayerParameters(
                  K=4,
                  BiasWidth=8,
                  AccumulatorWidth=10,
                  NumberOfPUs=4,
                  NumberOfMS=2,
                  MatrixWidth=784,
                  MatrixHeight=256,
                  NumberOfCores=1
                ), new LayerParameters(
                  K=2,
                  BiasWidth=8,
                  AccumulatorWidth=10,
                  NumberOfPUs=32,
                  NumberOfMS=8,
                  MatrixWidth=256,
                  MatrixHeight=256,
                  NumberOfCores=1
                  ))
    chiselMainTest(margs, () => Module(new GearBox(p))) {
      c => new GearBoxTests(c, p)
    }
  }
}
