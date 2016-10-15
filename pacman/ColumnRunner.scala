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
    val inputs = Vec.fill(k){ Bool() }.asInput
    val weights = Vec.fill(k){ Bool() }.asInput
    /** The accumulating value is the output value of the
     *  previous Runner, or `0` if it is the first runner */
    val accumulator = UInt(width=10).asInput
    /** output = acc + sum(inner(input, weights)) */
    val output = UInt(width=10).asOutput
  }
  io.output := UInt(123)
}

class ColumnRunnerTests(c: ColumnRunner) extends Tester(c) {
  step(1)
  expect(c.io.out, 123)
}
