package Pacman

import Chisel._

import fpgatidbits.ocm.DualPortBRAM

class CircularPeekQueue(blockSize: Int, numberOfBlocks: Int, dataWidth: Int) extends Module {

  class ResetCounter(start: Int, max: Int, step: Int = 1) extends Module {
    val io = new Bundle {
      val enable = Bool().asInput
      val writeEnable = Bool().asInput
      val writeData = Bool().asInput
      val value = UInt().asOutput
    }

    val startValue = UInt(start, width=UInt(max - 1).getWidth)
    val v = Reg(init = startValue)
    when (io.writeEnable) {
      v := io.writeData
    } .otherwise {
      v := Mux(io.enable, v + UInt(step), v)
    }
    io.value := v
  }

  val addressWidth = (Math.log(blockSize * numberOfBlocks) / Math.log(2)).ceil.toInt
  val io = new Bundle {
    val writeEnable = Bool().asInput
    val nextBlock = Bool().asInput
    val input = Bits(width=dataWidth).asInput
    val output = Bits(width=dataWidth).asOutput
  }

  val currentBlockOffset = Module(new Counter(0, blockSize * numberOfBlocks, step=blockSize))
  currentBlockOffset.io.enable  := io.nextBlock
  currentBlockOffset.io.rst     := currentBlockOffset.io.value === UInt(blockSize * numberOfBlocks)
  val inBlockReadOffset = Module(new ResetCounter(0, blockSize))
  inBlockReadOffset.io.enable := Bool(true)
  inBlockReadOffset.io.writeEnable    := io.nextBlock || inBlockReadOffset.io.value === UInt(blockSize - 1)
  inBlockReadOffset.io.writeData := Mux(io.nextBlock, UInt(1), UInt(0))

  val writeAddr = Reg(init=UInt(blockSize, width=UInt(blockSize * numberOfBlocks).getWidth))
  when (io.writeEnable) {
    when (writeAddr === UInt(blockSize * numberOfBlocks)) {
      writeAddr := UInt(0)
    } .otherwise {
      writeAddr := writeAddr + UInt(1)
    }
  }

  val queue = Module(new DualPortBRAM(addressWidth, dataWidth))
  val inputPort = queue.io.ports(0)
  val outputPort = queue.io.ports(1)

  outputPort.req.addr := currentBlockOffset.io.value + Mux(io.nextBlock, UInt(0), inBlockReadOffset.io.value)
  outputPort.req.writeEn := Bool(false)
  io.output := outputPort.rsp.readData

  inputPort.req.addr      := writeAddr
  inputPort.req.writeEn   := io.writeEnable
  inputPort.req.writeData := io.input
}
