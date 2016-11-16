package Pacman

import Chisel._

class GearBoxTests(c: GearBox, p: GearBoxParameters) extends Tester(c) {
  def peeks() {
    peek(c.io)
    peek(c.fillingBlock.io)
    peek(c.bitCounter.io)
    peek(c.blocksReady.io)
    peek(c.queues(0).io)
  }

  def cycle(xsIn: Int,
    validIn: Int,
    prevDone: Int,
    prevStart: Int,
    nextReady: Int,
    ready: Int,
    startNext: Int,
    xsOut: Int) {
      if (xsIn >= 0)      poke(c.io.xsIn(0), xsIn)
      if (validIn >= 0)   poke(c.io.validIn, validIn)
      if (prevDone >= 0)  poke(c.io.prevDone, prevDone)
      if (prevStart >= 0) poke(c.io.prevStart, prevStart)
      if (nextReady >= 0) poke(c.io.nextReady, nextReady)
      step(1)
    poke(c.io.prevStart, false)
      if (ready >= 0)     expect(c.io.ready, ready)
      if (startNext >= 0) expect(c.io.startNext, startNext)
      if (xsOut >= 0)     expect(c.io.xsOut(0), xsOut)
  }

  poke(c.io.prevStart, false)
  poke(c.io.validIn, false)
  poke(c.io.prevDone, false)
  poke(c.io.prevStart, false)
  poke(c.io.nextReady, false)
  poke(c.io.xsIn(0), 0)
  step(80)
  expect(c.io.ready, true)
  expect(c.io.startNext, false)
  poke(c.io.prevStart, true)
  step(1)
  poke(c.io.prevStart, false)
  step(19) // 100

  //    xsIn validIn prevDone prevStart nextReady     ready startNext xsOut
  cycle(  1,      1,       0,        0,        1,        1,        0,    -1) // 101
  cycle(  0,      1,       0,        0,        1,        1,        0,    -1)
  cycle(  0,      0,       0,        0,        1,        1,        0,    -1)
  cycle(  0,      1,       0,        0,        1,        1,        0,    -1)
  cycle(  1,      1,       0,        1,        1,        0,        0,    -1)
  cycle(  1,      1,       0,        0,        1,        0,        0,    -1)
  cycle(  1,      1,       1,        0,        1,        0,        0,    -1)
  // Numbers must propagate through the buffer and the queue before output
  cycle( -1,      0,       0,        0,        1,        0,        0,    -1)
  // We get output
  cycle( -1,      0,       0,        0,        1,        1,        1,     1)
  cycle(  1,      1,       0,        0,        1,        1,        0,     2) // 110
  cycle(  1,      1,       0,        0,        1,        1,        0,     3)
  cycle(  0,      1,       0,        0,        1,        1,        0,     1)
  cycle(  1,      1,       0,        0,        1,        1,        0,     2)
  cycle(  1,      1,       0,        0,        1,        1,        0,     3)
  cycle(  0,      1,       1,        1,        0,        0,        0,     1)
  // Fill up queue
  cycle(  1,      1,       0,        0,        0,        0,        0,     2) // 116
  cycle(  0,      1,       0,        0,        0,        0,        0,     3) // 117
  cycle(  1,      1,       0,        0,        0,        0,        0,     1) // 118
  cycle(  0,      1,       0,        0,        0,        0,        0,     2) // 119
  cycle(  0,      1,       0,        0,        0,        0,        0,     3) // 120
  cycle(  1,      1,       1,        0,        0,        0,        0,     1) // 121
  cycle( -1,      0,       0,        0,        0,        0,        0,     2) // 122
  // Read the two blocks as fast as possible
  cycle( -1,     -1,      -1,       -1,        1,       -1,        1,     3) // 123
  cycle( -1,     -1,      -1,       -1,        0,       -1,        0,     2) // 124
  cycle( -1,     -1,      -1,       -1,        0,       -1,        0,     1) // 125
  cycle( -1,     -1,      -1,       -1,        1,       -1,        1,     1) // 126
  cycle( -1,     -1,      -1,       -1,        0,       -1,        0,     1) // 127
  cycle( -1,     -1,      -1,       -1,        0,       -1,        0,     2) // 128
}

class GearBoxTests5x2(c: GearBox, p: GearBoxParameters) extends Tester(c) {
  def peeks() {
    peek(c.io)
    peek(c.fillingBlock.io)
    peek(c.bitCounter.io)
    peek(c.blocksReady.io)
    peek(c.queues(0).io)
  }

  def cycle(xsIn: Array[Int],
    validIn: Int,
    prevDone: Int,
    prevStart: Int,
    nextReady: Int,
    ready: Int,
    startNext: Int,
    xsOut: Array[Int]) {
      if (xsIn.length > 0)
        for (i <- 0 until p.Previous.NumberOfCores)
          poke(c.io.xsIn(i), xsIn(i))
      if (validIn >= 0)   poke(c.io.validIn, validIn)
      if (prevDone >= 0)  poke(c.io.prevDone, prevDone)
      if (prevStart >= 0) poke(c.io.prevStart, prevStart)
      if (nextReady >= 0) poke(c.io.nextReady, nextReady)
      step(1)
    poke(c.io.prevStart, false)
      if (ready >= 0)     expect(c.io.ready, ready)
      if (startNext >= 0) expect(c.io.startNext, startNext)
      if (xsOut.length > 0)
        for (i <- 0 until p.Next.NumberOfCores)
          expect(c.io.xsOut(i), xsOut(i))
  }

  poke(c.io.prevStart, false)
  poke(c.io.validIn, false)
  poke(c.io.prevDone, false)
  poke(c.io.prevStart, false)
  poke(c.io.nextReady, false)
  // poke(c.io.xsIn(0), 0)
  step(80)
  expect(c.io.ready, true)
  expect(c.io.startNext, false)
  poke(c.io.prevStart, true)
  step(1)
  poke(c.io.prevStart, false)
  step(19) // 100

  def i(is: Int*): Array[Int] = { is.toArray }
  println(i(1,2,3))

  //       xsIn       validIn prevDone prevStart nextReady /**/ ready startNext xsOut
  cycle(i(1,0,2,3,0),      1,       0,        0,        1, /**/    1,        0, i()) // 101
  cycle(i(3,3,3,1,1),      0,       0,        0,        1, /**/    1,        0, i())
  cycle(i(3,3,3,1,1),      1,       0,        0,        1, /**/    1,        0, i())
  cycle(i(2,0,0,0,3),      1,       1,        0,        1, /**/    1,        0, i())

  cycle(i(         ),      0,       0,        0,        1, /**/    1,        0, i())
  cycle(i(         ),      0,       0,        0,        1, /**/    1,        1, i(1, 0))































}

object GearBoxTest {
  def main(args: Array[String]) {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    // Random.setSeed(12)
    if (false) {
      val p = new GearBoxParameters(
        new LayerParameters(
          MatrixHeight=6,
          NumberOfCores=1
        ), new LayerParameters(
          K=2,
          NumberOfCores=1
        ))
      chiselMainTest(margs, () => Module(new GearBox(p))) {
        c => new GearBoxTests(c, p)
      }
    }
    if (true) {
      val p = new GearBoxParameters(
        new LayerParameters(
          MatrixHeight=6,
          NumberOfCores=5
        ), new LayerParameters(
          K=2,
          NumberOfCores=2
        ))
      chiselMainTest(margs, () => Module(new GearBox(p))) {
        c => new GearBoxTests5x2(c, p)
      }
    }
  }
}
