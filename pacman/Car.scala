package Pacman

import Chisel._

class Car(n: Int) extends Module {
  var io = new Bundle {
    val out = Vec.fill(n){ UInt(width=8) }.asOutput
  }
  for (i <- 0 until n) {
    io.out(i) := UInt(i);
  }
}

class CarTests(c: Car, n: Int) extends Tester(c) {
  step(1)
  for (i <- 0 until n) {
    expect(c.io.out(i), i)
  }
}
