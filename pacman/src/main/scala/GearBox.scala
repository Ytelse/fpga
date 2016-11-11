package Pacman

import Chisel._

import fpgatidbits.ocm.DualPortBRAM

class GearBox(p: GearBoxParameters) extends Module {

  val blocksPerQueue = 3
  val maxReadyBlocksPerQueue = blocksPerQueue - 1
  val wordsPerBlock = p.Previous.MatrixHeight / p.Next.K
  val numberOfQueues = Math.max(p.Previous.NumberOfCores, p.Next.NumberOfCores)
  if (numberOfQueues > 1) {
    throw new AssertionError("GearBox does not support multiple cores yet!")
  }

  val io = new Bundle {
    val xsIn = Vec.fill(p.Previous.NumberOfCores) { Bits(width = 1) }.asInput
    val validIn = Bool().asInput
    val prevDone = Bool().asInput
    val prevHasXsComing = Bool().asInput
    val ready = Bool().asOutput

    val nextReady = Bool().asInput
    val startNext = Bool().asOutput
    val xsOut = Vec.fill(p.Next.NumberOfCores) { Bits(width = p.Next.K) }.asOutput
  }

  // Make bitBuffers
  val bitBuffers = Array.fill(p.Previous.NumberOfCores)(Module(new BitToWord(p.Next.K)))

  // Hook up bitBuffers to input so they get filled up
  bitBuffers.zip(io.xsIn).foreach {
    case (b, x) => {
      b.io.enable := io.validIn
      b.io.bit := x
    }
  }

  // Make queues
  val queues = Array.fill(numberOfQueues)(
    Module(new CircularPeekQueue(wordsPerBlock, blocksPerQueue, p.Next.K))
  )

  /*
   *  From here on we assume a 1-to-1 GearBox
   */
  queues(0).io.input := bitBuffers(0).io.word
  io.xsOut(0) := queues(0).io.output

  // Count how many bits are in the bitBuffers
  val bitCounter = Module(new Counter(0, p.Next.K))
  bitCounter.io.enable := io.validIn
  val signalResetBitBuffers = bitCounter.io.value === UInt(p.Next.K - 1)
  bitCounter.io.rst := signalResetBitBuffers

  // Write to the queue when bitBuffers are full
  val signalBitBufferFull = ShiftRegister(io.validIn && signalResetBitBuffers, 1)
  queues(0).io.writeEnable := signalBitBufferFull

  // Count how many ready blocks we have in the queue
  val blockCounter = Module(new Counter(0, maxReadyBlocksPerQueue + 1))
  blockCounter.io.enable := ShiftRegister(io.prevDone, 1)
  blockCounter.io.rst := blockCounter.io.value === UInt(maxReadyBlocksPerQueue)

  // io.ready := !(blockCounter.io.value === UInt(maxReadyBlocksPerQueue))
}
