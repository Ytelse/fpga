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
  when(io.enable) {
    when(io.rst) {
      v := UInt(start)
    }.otherwise {
      v := v + UInt(step)
    }
  }
  io.value := v
}


class CounterWithSyncAndAsyncReset(start: Int, max: Int, step: Int = 1) extends Module {
  val io = new Bundle {
    val enable = Bool().asInput
    val syncRst = Bool().asInput
    val asyncRst = Bool().asInput
    val value = UInt().asOutput
  }

  val startValue = UInt(start, width=UInt(max - 1).getWidth)
  val v = Reg(init = startValue)

  when(io.enable) {
    when(io.asyncRst) {
      v := UInt(start + step)
    }.elsewhen(io.syncRst) {
      v := UInt(start)
    }.otherwise {
      v := v + UInt(step)
    }
    io.value := Mux(io.asyncRst, UInt(start), v)
  }.otherwise {
    io.value := v
  }
}

class AsyncCounter(start: Int, end: Int, step: Int) extends Module {
  if((end - start) % step != 0) {
    throw new AssertionError("Step size is not a divisor of (end - start)")
  }

  val io = new Bundle {
    val enable = Bool().asInput
    val value = UInt().asOutput
  }

  val startValue = UInt(start, width=UInt(end - 1).getWidth)
  val v = Reg(init=UInt(startValue))

  when(io.enable) {
    val nextValue = Mux(v === UInt(end - step), UInt(start), v + UInt(step))
    v := nextValue
    io.value := nextValue
  }.otherwise {
    io.value := v
  }
}
