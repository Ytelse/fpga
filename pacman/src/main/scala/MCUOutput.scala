package Pacman

import scala.language.reflectiveCalls
import Chisel._



class MCUOutput/*(parameters: LayerParameters )*/ extends Module {
  val io = new Bundle {
    val oneHotIn = Decoupled(Bits(width=10 /*width of input */  )).flip
    val ebiD = Bits(width=16).asOutput
    val ebiReEn = Bool().asInput
    val achRead = Bool().asInput
    val ebiValid = Bool().asOutput
  }
  val outBuff = Vec.fill(4) { Reg(init=Bits(0,width=4)) }
  val fifo = Mem(16,Bits(width=4)) 
  val outCtrl = Module(new MCUOutCtrl)
  val data = OHToUInt(io.oneHotIn.bits)
  val rdyAch = Reg(init=Bool(true))
  
  // Output 
  io.ebiD := Cat(outBuff(0), outBuff(1), outBuff(2), outBuff(3))
  io.oneHotIn.ready := outCtrl.io.addr!=UInt(15)
  
  // Control Input
  when (io.achRead) { rdyAch := io.ebiReEn}
  outCtrl.io.valid := io.oneHotIn.valid
  outCtrl.io.fillIn := rdyAch
   
  // Shift data in when valid
  when(io.oneHotIn.valid) {
    fifo(0) := data(3,0) 
    for(i <- 1 until 16) {
      fifo(i) := fifo(i-1)
    }
  }
  // Fill out buffer when in the right state
  when(outCtrl.io.state(0)===Bits(1)) { outBuff(0) := fifo(outCtrl.io.addr) }
  when(outCtrl.io.state(1)===Bits(1)) { outBuff(1) := fifo(outCtrl.io.addr) }
  when(outCtrl.io.state(2)===Bits(1)) { outBuff(2) := fifo(outCtrl.io.addr) }
  when(outCtrl.io.state(3)===Bits(1)) { outBuff(3) := fifo(outCtrl.io.addr) }
  
}
