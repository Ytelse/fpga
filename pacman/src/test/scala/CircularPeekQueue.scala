package Pacman

import util.Random

import Chisel._

class CircularPeekQueueTests(c: CircularPeekQueue, p: GearBoxParameters) extends Tester(c) {
  val bits = (b: String) => {Integer.parseInt(b, 2)}
  def pushBits(b: String, push: Boolean = true) {
    poke(c.io.input, bits(b))
    poke(c.io.writeEnable, push)
  peeks()
    step(1)
  }
  def cycle(b: String = "", expectOut: String = "", nextBlock: Boolean = false) {
    if (b.length > 0)  {
      poke(c.io.input, bits(b))
      poke(c.io.writeEnable, true)
    } else {
      poke(c.io.input, Random.nextInt(1234))
      poke(c.io.writeEnable, false)
    }

    poke(c.io.nextBlock, nextBlock)
    step(1)
    peeks()
    if (expectOut.length > 0)
      expect(c.io.output, bits(expectOut))
  }

  def peeks() {
    peek(c.queue.io)
    peek(c.currentBlockOffset.io)
    peek(c.inBlockReadOffset.io)
  }
  step(100)

  cycle("00")
  cycle("01")
  cycle()
  cycle("10")
  cycle("11")
  cycle("10", "00", true)
  // Write 5
  cycle("10", "01")
  cycle("11", "10")
  cycle("10", "11")
  cycle("01", "10")
  cycle("00", "00")
  // Write 10
  cycle("00", "01")
  cycle("00", "10", true)

}

object CircularPeekQueueTest {
  def main(args: Array[String]) {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    // Random.setSeed(12)
    val p = new GearBoxParameters(
                new LayerParameters(MatrixHeight=10),
                new LayerParameters(K=2, MatrixWidth=10))
    val blockSize = p.First.MatrixHeight / p.Second.K
    // TODO:
    val numberOfBlocks = 3
    val dataWidth = p.Second.K
    chiselMainTest(margs, () => Module(new CircularPeekQueue(blockSize, numberOfBlocks, dataWidth))) {
      c => new CircularPeekQueueTests(c, p)
    }
  }
}
