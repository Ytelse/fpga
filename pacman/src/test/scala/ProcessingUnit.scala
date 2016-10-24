package Pacman

import Chisel._

class ProcessingUnitTests(c: ProcessingUnit, k: Int) extends Tester(c) {
  val ones = (Math.pow(2, k) - 1).toInt
  poke(c.io.xs, 0)
  poke(c.io.ws, ones)
  poke(c.io.resetIn, false)

  step(1)

  expect(c.io.xOut, 0)
  expect(c.io.yOut, 0)
  expect(c.io.resetOut, false);
  poke(c.io.xs, ones)

  step(1)

  expect(c.io.xOut, ones)
  expect(c.io.yOut, k)
  expect(c.io.resetOut, false);
  poke(c.io.ws, 0)

  step(1)

  expect(c.io.xOut, ones)
  expect(c.io.yOut, k)
  expect(c.io.resetOut, false);
  poke(c.io.ws, ones)

  step(1)

  expect(c.io.xOut, ones)
  expect(c.io.yOut, k * 2)
  expect(c.io.resetOut, false);
  poke(c.io.resetIn, true)

  step(1)

  expect(c.io.xOut, ones)
  expect(c.io.yOut, k)
  expect(c.io.resetOut, true);
  poke(c.io.xs, 0)

  step(1)

  expect(c.io.yOut, 0)
  expect(c.io.xOut, 0)
  expect(c.io.resetOut, true);

  val upperHalf = ones >> (k / 2)
  val lowerHalf = ~upperHalf

  poke(c.io.xs, upperHalf)
  poke(c.io.ws, lowerHalf)
  poke(c.io.resetIn, false)

  step(1)

  expect(c.io.xOut, upperHalf)
  expect(c.io.yOut, 0)
  expect(c.io.resetOut, false);

}

object ProcessingUnitTest {
  def main(args: Array[String]): Unit = {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    val k = 4
    chiselMainTest(margs, () => Module(new ProcessingUnit(k))) {
      c => new ProcessingUnitTests(c, k)
    }
  }
}
