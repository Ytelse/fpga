package Pacman 

import scala.language.reflectiveCalls
import Chisel._

class MCUOutCtrl extends Module {
  val io = new Bundle{
    val fillIn = Bool().asInput
    val valid = Bool().asInput 
    val state = Vec.fill(5)(Bits(width=1)).asOutput
    val addr = UInt(width=4).asOutput
  }
  val fifoAddr = Reg(UInt(width=4),init=UInt(0))
  val fillToggle = Reg(Bool(),init=Bool(false))
  val fill = Reg(Bool(),init=Bool(false))
  val fillState = Vec(
    Reg(Bits(width=1),init=Bits(0)), // Fill reg 0
    Reg(Bits(width=1),init=Bits(0)), // Fill reg 1
    Reg(Bits(width=1),init=Bits(0)), // Fill reg 2
    Reg(Bits(width=1),init=Bits(0)), // Fill reg 3
    Reg(Bits(width=1),init=Bits(1))  // Idle state
  ) 
 
  // Outputs
  io.state := fillState
  io.addr := fifoAddr

  // The fifo gets 1 valid number
  when (io.valid && ~fill) {
    fifoAddr := fifoAddr + UInt(1)
  }

  // Data is available and the states will cycle once
  when ((fifoAddr >= Bits(4)) && fillToggle){
    fill := ~fill
    fillToggle := ~fillToggle
  }

  // Toggle that the MCU wants data
  when (io.fillIn &&  ~fill ){
    fillToggle := ~fillToggle
  }

  when(fill) { 
    // Cycling of the one-hot value that spacifies the state
    fillState(0) := fillState(4)
    for(i <- 1 until 5){
      fillState(i) := fillState(i-1)
    }

    when(fillState(3)===Bits(1)){ 
      fill := ~fill // resets when the state machine reaches Idle
    } .elsewhen (~io.valid) {
      fifoAddr := fifoAddr - UInt(1)
    } 

  }
}
