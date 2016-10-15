package Pacman

import Chisel._

object Utils {
  def equv(a: UInt, b: UInt): UInt = {
    val out = UInt(width=1);
    when (a === b) { out := UInt(1) }
    .otherwise ( out := UInt(0) )
    out
  }
}
