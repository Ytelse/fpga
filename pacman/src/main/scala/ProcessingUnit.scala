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
class ProcessingUnit(k: Int) extends Module {
  // TODO: parameterize this width
  val yWidth = 10
  val yReg = Reg(UInt(width=yWidth), init=UInt(0))
  val xReg = Reg(Bits(width=k), init=Bits(0))
  val resetReg = Reg(Bool(), init=Bool(false))
  val io = new Bundle {
    val xs = Bits(width=k).asInput
    val ws = Bits(width=k).asInput
    val xOut = Bits(width=k).asOutput
    val yOut = UInt(width=yWidth).asOutput
    val resetIn = Bool().asInput
    val resetOut = Bool().asOutput
  }

  yReg := Mux(io.resetIn, UInt(0), PopCount(~(io.xs ^ io.ws)) + yReg)

  resetReg := io.resetIn
  io.resetOut := resetReg

  xReg := io.xs
  io.yOut := yReg
  io.xOut := xReg
}
