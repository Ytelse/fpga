package Pacman

import fpgatidbits.ocm.DualPortBRAM
import scala.language.reflectiveCalls
import Chisel._



class MCUOutput/*(parameters: LayerParameters )*/ extends Module {
  val io = new Bundle {
    val oneHotIn = Decoupled(Bits(width=10 /*width of input */  )).flip
    val ebiD = Bits(width=16).asOutput
    val ebiRdy = Bool().asInput
    val ebiAck = Bool().asInput
    val ebiVal = Bool().asOutput
  }
  val outBuff = Vec.fill(4) { Reg(init=Bits(0,width=4)) }
  val fifo = Module( new DualPortBRAM(1024,4))
  val outCtrl = Module(new MCUOutCtrl)
  val data = OHToUInt(io.oneHotIn.bits)
  val ackTgl = Reg(init=Bool(false))
  val ebiRdy = Reg(next=io.ebiRdy)
  val ebiAck = Reg(next=io.ebiAck)
  val rdyTgl = Reg(init=Bool(false))

  // Output 

  io.ebiD := Cat(outBuff(0), outBuff(1), outBuff(2), outBuff(3))
  io.oneHotIn.ready := outCtrl.io.offset =/= UInt(1023)
  io.ebiVal := Mux(ebiRdy, outCtrl.io.validOut,Bool(false))
  
  // Control Input
  ackTgl := ebiAck
  rdyTgl := ebiRdy
  outCtrl.io.readyLow := rdyTgl && ~ebiRdy
  outCtrl.io.validIn := io.oneHotIn.valid
  outCtrl.io.fillIn := ~ackTgl && ebiAck
   
  //Set up fifo buffer write and read ports 
  fifo.io.ports(0).req.writeEn := io.oneHotIn.valid
  fifo.io.ports(0).req.addr := outCtrl.io.addrIn 
  fifo.io.ports(0).req.writeData := data(3,0)

  fifo.io.ports(1).req.addr := outCtrl.io.addrOut
  fifo.io.ports(1).req.writeEn := Bool(false)
  // Fill out buffer when in the right state
  when(outCtrl.io.state(0)===Bits(1)) { outBuff(0) := fifo.io.ports(1).rsp.readData }
  when(outCtrl.io.state(1)===Bits(1)) { outBuff(1) := fifo.io.ports(1).rsp.readData }
  when(outCtrl.io.state(2)===Bits(1)) { outBuff(2) := fifo.io.ports(1).rsp.readData }
  when(outCtrl.io.state(3)===Bits(1)) { outBuff(3) := fifo.io.ports(1).rsp.readData }
  
}

