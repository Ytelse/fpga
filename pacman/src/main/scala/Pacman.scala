package Pacman

import Chisel._

class Pacman extends Module {
  val io = new Bundle {
    val test_led = Bits(width = 1).asOutput
  }

  io.test_led := UInt(1);
}

object Main {
  def main(args: Array[String]) {
    chiselMainTest(args, () => Module(new Pacman)) { bl =>
      new Tester(bl)
    }
  }
}
