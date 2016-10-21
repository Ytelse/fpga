package Pacman

import Chisel._

class ChainTests(c: Chain,
                 processingUnits: Int,
                 addrWidth: Int,
                 k: Int,
                 memoryReaders: Int,
                 memoryOffsets: List[Int],
                 rowLength: Int
  ) extends Tester(c) {

}

object ChainTest {
  def main(args: Array[String]): Unit = {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    val numProcessingUnits = 4
    val addrWidth = 64;
    val memoryReaders = 2;
    val k = 4;
    val memoryOffsets = List(0, 8, 16, 24)
    val rowLength = 32
    chiselMainTest(margs, () => Module(new Chain(numProcessingUnits, addrWidth, k, memoryReaders, memoryOffsets, rowLength))) {
      c => new ChainTests(c, numProcessingUnits, addrWidth, k, memoryReaders,
        memoryOffsets, rowLength)
    }
  }
}
