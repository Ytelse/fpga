package Pacman

import Chisel._

/**
 * A Car handles weight reading for each Runner.
 * The module selects which addresses in the BRAM
 * that each runner gets. It also reads this in chunks,
 * so each runners width doesn't have to be a
 * "nice number" for the bram.
 *
 * `n` is the number of Runners the Car is connected to.
 * `k` is the width of the lines to each Car. That is,
 * the number of bits each car reads at any given time.
 */
class Car(n: Int, k: Int) extends Module {
  // TODO: find out address width
  var addrWidth  = 64;
  // TODO: How should the cars know which
  // addresses to read? If we parametrize it,
  // we "hardcode" it into the cars, which may
  // be what we want.
  var addrReg = Reg(UInt());
  var io = new Bundle {
    val data = UInt(width=n * k).asInput
    val addr = UInt(width=addrWidth).asOutput
    val weights = Vec.fill(n){ UInt(width=k) }.asOutput
  }

  addrReg += UInt(n * k)
  io.addr := addrReg
  for (i <- 0 until n) {
    val upperBit = (i + 1) * k - 1
    var lowerBit = i * k
    io.weights(i) := io.data(upperBit, lowerBit)
  }
}

class CarTests(c: Car, n: Int, k: Int) extends Tester(c) {
  step(1)
}
