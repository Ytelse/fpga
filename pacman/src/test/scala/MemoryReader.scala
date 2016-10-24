package Pacman

import Chisel._

class MemoryReaderTests(c: MemoryReader, n: Int, k: Int, addrWidth: Int, offset: Int, readingLength: Int) extends Tester(c) {
  var stepSize = n * k / 8
  // Go far enough to see wrapping
  for (i <- 0 until readingLength / stepSize + 1) {
    val randomNums = (0 until n).map((_) => rnd.nextInt(math.pow(2, k).toInt))
    var num = 0;
    for (j <- 0 until n) {
      num |= randomNums(j) << (k * j)
    }
    poke(c.io.data, num)
    expect(c.io.addr, (stepSize * i) % readingLength)

    step(1)

    expect(c.io.addr, (stepSize * (i + 1)) % readingLength)
    for (j <- 0 until n) {
      expect(c.io.weights(j), randomNums(j))
    }
  }

  reset()
  expect(c.io.addr, 0)
}

object MemoryReaderTest {
  def main(args: Array[String]): Unit = {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    val n = 4
    val k = 4
    val addrWidth = 64;
    val offset = 0;
    val readingLength = 8;
    chiselMainTest(margs, () => Module(new MemoryReader(n, k, addrWidth, offset, readingLength))) {
      c => new MemoryReaderTests(c, n, k, addrWidth, offset, readingLength)
    }
  }
}
