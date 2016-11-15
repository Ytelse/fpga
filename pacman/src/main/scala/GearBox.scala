package Pacman

import Chisel._

import fpgatidbits.ocm.DualPortBRAM

class GearBox(p: GearBoxParameters) extends Module {
  if (p.Previous.MatrixHeight == 0)
    throw new AssertionError("p.Previous.MatrixHeight needs to be set")
  if (p.Previous.NumberOfCores == 0)
    throw new AssertionError("p.Previous.NumberOfCores needs to be set")
  if (p.Next.NumberOfCores == 0)
    throw new AssertionError("p.Next.NumberOfCores needs to be set")
  if (p.Next.K == 0) throw new AssertionError("p.Next.K needs to be set")

  val blocksPerQueue = 3
  val maxReadyBlocksPerQueue = blocksPerQueue - 1
  val wordsPerBlock = p.Previous.MatrixHeight / p.Next.K
  val numberOfQueues = Math.max(p.Previous.NumberOfCores, p.Next.NumberOfCores)
  if (numberOfQueues != 1) {
    throw new AssertionError("GearBox does not support multiple cores yet!")
  }

  val io = new Bundle {
    val xsIn = Vec.fill(p.Previous.NumberOfCores) { Bits(width = 1) }.asInput
    val validIn = Bool().asInput
    val prevDone = Bool().asInput
    val prevStart = Bool().asInput
    val ready = Bool().asOutput

    val nextReady = Bool().asInput
    val startNext = Bool().asOutput
    val xsOut = Vec.fill(p.Next.NumberOfCores) { Bits(width = p.Next.K) }.asOutput
  }

  // Make bitBuffers
  val bitBuffers = Array.fill(p.Previous.NumberOfCores)(Module(new BitToWord(p.Next.K)))
  val fillingBlock = Module(new AsyncUpDownCounter(0, 2))
  fillingBlock.io.up    := io.prevStart
  fillingBlock.io.down  := ShiftRegister(io.prevDone, 1)

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

  val inputSelectCounter = Array.range(0, numberOfQueues)
    .map(i => Module(new WrappingCounter(i, numberOfQueues,
                                         numberOfQueues - p.Previous.NumberOfCores)))
    .toArray
  val inputEnables = Vec.fill(p.Previous.NumberOfCores){ Bool(true) } ++
                     Vec.fill(numberOfQueues - p.Previous.NumberOfCores) { Bool(false) }

  val outputSelectCounter = Array.range(0, p.Next.NumberOfCores)
    .map(i => Module(new WrappingCounter(i, numberOfQueues, p.Next.NumberOfCores)))
    .toArray





  /*
   *  From here on we assume a 1-to-1 GearBox
   */
  queues(0).io.input := bitBuffers(0).io.word
  io.xsOut(0) := queues(0).io.output

  val bitCounter = Module(new Counter(0, p.Next.K))
  val blocksReady = Module(new UpDownCounter(0, maxReadyBlocksPerQueue))

  val signalResetBitBuffers = bitCounter.io.value === UInt(p.Next.K - 1)
  val signalBitBufferFull = ShiftRegister(io.validIn && signalResetBitBuffers, 1)
  val signalNewPeekBlock = io.nextReady && blocksReady.io.value =/= UInt(0)

  // Count how many bits are in the bitBuffers
  bitCounter.io.enable := io.validIn
  bitCounter.io.rst := signalResetBitBuffers

  // Write to the queue when bitBuffers are full
  queues(0).io.writeEnable  := signalBitBufferFull
  queues(0).io.nextBlock    := signalNewPeekBlock

  // Count how many ready blocks we have in the queue
  blocksReady.io.up   := ShiftRegister(io.prevDone, 1)
  blocksReady.io.down := signalNewPeekBlock

  val reservedBlocks = blocksReady.io.value + fillingBlock.io.value
  val hasEmptyBlocks = reservedBlocks =/= UInt(maxReadyBlocksPerQueue)

  io.ready := hasEmptyBlocks
  io.startNext := ShiftRegister(signalNewPeekBlock, 1)
}
