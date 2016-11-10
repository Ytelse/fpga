package Pacman

import Chisel._

import fpgatidbits.ocm.DualPortBRAM

class GearBox(p: GearBoxParameters) extends Module {
  val io = new Bundle {
    val ys = Vec.fill(p.First.NumberOfCores){ Bits(width=1) }.asInput
    val ready = Bool().asOutput
    val done = Bool().asInput

    val nextReady = Bool().asInput
    val start = Bool().asOutput
    val xs = Vec.fill(p.Second.NumberOfCores){ Bits(width=p.Second.K) }.asOutput
  }

  println(p)
}
