package Pacman

import Chisel._

class BitToWord(K: Int) extends Module {
  class RegWithEnable(enable: Bool) extends Module {
    val io = new Bundle {
      val in = Bits(width = 1).asInput
      val out = Bits(width = 1).asOutput
    }

    val reg = Reg(init = Bits(0, width = 1))
    when(enable) {
      reg := io.in
    }

    io.out := reg
  }

  val io = new Bundle {
    val enable = Bool().asInput
    val bit = Bits(width = 1).asInput
    val word = Bits(width = K).asOutput
  }

  val regs = Array.fill(K)(Module(new RegWithEnable(io.enable)))
  regs.zip(regs.drop(1))
    .foreach({
      case (a, b) => {
        b.io.in := a.io.out
      }
    })

  regs(0).io.in := io.bit

  io.word := regs.map(_.io.out).reduceLeft((a, b) => Cat(a, b))

}
