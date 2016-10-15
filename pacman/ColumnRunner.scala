package Pacman

import Chisel._

/**
 * A ColumnRunner is a block which "runs"
 * along columns in the matrix, holding a
 * part of the input array, and calculating
 * the inner product.
 *
 * The width `k` of a runner is how many bits
 * of the input vector it takes.
 */
class ColumnRunner(k: Int) extends Module {
  var io = new Bundle {
    val xs = Vec.fill(k){ UInt(width=1) }.asInput
    val ws = Vec.fill(k){ UInt(width=1) }.asInput
    /** yIn is the output of the previous Runner,
     *  or `0` if it is the first runner */
    val yIn = UInt(width=10).asInput
    /** output = acc + sum(inner(input, weights)) */
    val yOut = UInt(width=10).asOutput
  }
  val sum = (io.xs, io.ws).zipped
            .map(Utils.equv)
            .reduce(_ + _) + io.yIn
  io.yOut := sum
}

class ColumnRunnerTests(c: ColumnRunner) extends Tester(c) {
  poke(c.io.yIn, 0)
  poke(c.io.xs(0), 1)
  poke(c.io.ws(0), 1)
  step(1)
  expect(c.io.yOut, 1)
  step(1)
  expect(c.io.yOut, 1)
  poke(c.io.ws(0), 0)
  step(1)
  expect(c.io.yOut, 0)
}
