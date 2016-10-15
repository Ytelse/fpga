package Pacman

import Chisel._

class Runner extends Module {
  var io = new Bundle {
    val out = UInt(width=8).asOutput
  }

  io.out := UInt(123)
}

class RunnerTests(c: Runner) extends Tester(c) {
  step(1)
  expect(c.io.out, 123)
}
