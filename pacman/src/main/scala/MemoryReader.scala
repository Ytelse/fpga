package Pacman

import Chisel._

/**
  * A MemoryReader handles weight reading for each Processing
  * Unit. The module selects which addresses in the BRAM
  * that each runner gets. It also reads this in chunks,
  * so each runners width doesn't have to be a
  * "nice number" for the bram.
  *
  * `n` is the number of processing units the MemoryReader is connected to.
  * `k` is the width of the lines to each MemoryReader. That is,
  * the number of bits each reader reads at any given time.
  */
class MemoryReader(n: Int, k: Int, addrWidth: Int, offset: Int, rowLength: Int)
    extends Module {
  // TODO: How should the cars know which
  // addresses to read? If we parametrize it,
  // we "hardcode" it into the cars, which may
  // be what we want.
  val addrReg = Reg(UInt(width = addrWidth), init = UInt(x=offset))
  val io = new Bundle {
    val data = UInt(width = n * k).asInput
    val addr = UInt(width = addrWidth).asOutput
    val weights = Vec.fill(n) { Bits(width = k) }.asOutput
  }
  // NOTE: we've assumed byte addressing
  val stepSize = n * k / 8
  when(addrReg + UInt(x=stepSize) === UInt(x=rowLength)) {
    addrReg := UInt(x=offset)
  }.otherwise {
    addrReg := addrReg + UInt(x=stepSize)
  }

  // TODO: if `k` is large, the runners may not be able to
  // calculate the entire sum dotproduct in one cycle?
  // If so, we might need to wait for some cycles, before
  // requesting new memory from BRAM. Check it up.
  for (i <- 0 until n) {
    val upperBit = (i + 1) * k - 1
    var lowerBit = i * k
    io.weights(i) := io.data(upperBit, lowerBit)
  }
  io.addr := addrReg
}
