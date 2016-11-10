package Pacman

import Chisel._

class Counter(start: Int, max: Int, step: Int = 1) extends Module {
  val io = new Bundle {
    val enable = Bool().asInput
    val rst = Bool().asInput
    val value = UInt().asOutput
  }

  val startValue = UInt(start, width=UInt(max - 1).getWidth)
  val v = Reg(init = startValue)
  when (io.rst) {
    v := startValue
  } .otherwise {
    v := Mux(io.enable, v + UInt(step), v)
  }
  io.value := v
}

