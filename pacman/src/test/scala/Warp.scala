package Pacman

import util.Random
import scala.collection.mutable.PriorityQueue

import Chisel._

class WarpTests(c: Warp,
                p: LayerParameters,
                weights: Array[Array[Int]],
                biases: Array[Int]
  ) extends Tester(c) {
  def vecToBigInt(vec: Seq[Int]): BigInt = {
    int(Bits(vec.reverse.map((n) => n.toString).fold("b")(_ + _)))
  }

  if (p.NumberOfCores == 0)
    throw new AssertionError("Warp need to have NumberOfCores set")
  if (p.MatrixHeight % p.NumberOfPUs != 0)
    throw new AssertionError("NumberOfPUs needs to divide MatrixHeight")

  val Iters = 2
  val xs = List.fill(Iters) {
            List.fill(p.NumberOfCores) {
              List.fill(p.MatrixWidth) {
                Random.nextInt(2) }}}

  val expectedResults = xs.map(e => e.map(vec =>
      Matrix.matrixMul(weights, vec)
        .zip(biases)
        .map({ case (dot, bias) => {
          val a = dot * 2 - p.MatrixWidth + bias
          if (a >= 0) 1 else 0
        }})))

  val passesRequired = p.MatrixHeight / p.NumberOfPUs
  val cyclesPerPass = p.MatrixWidth / p.K
  val PUsPerMUs = p.NumberOfPUs / p.NumberOfMS

  val waitingSteps = List(0, 1, cyclesPerPass / 2, 3)

  var Cycle = 0

  var expectQueue: PriorityQueue[(Int, () => Unit)] =
    PriorityQueue()(Ordering.by(e => -e._1))
  def handleQueue(cycle: Int) {
    if (expectQueue.length == 0)
      return
    var top = expectQueue.minBy(e => e._1)
    if (top._1 == cycle) {
      top._2.apply
      expectQueue.dequeue
      handleQueue(cycle)
    }
  }
  var shouldBeReady = false

  println("Weights")
  Matrix.printMat(weights)
  println("Biases")
  Matrix.printMat(Array(biases))

  for (i <- 0 until Iters) {
    for (j <- 0 until p.NumberOfCores) {
      println("==========")
      println("Step %d".format(i))
      println("xs")
      Matrix.printMat(Array(xs(i)(j).toArray))
      println("output")
      Matrix.printMat(expectedResults(i).map(_.toArray).toArray)
    }
  }

  step(100)
  // poke(c.io.xOut.ready, true)
  // poke(c.io.start, true)
  // Range(0, 15).foreach(f => {
  //                        step(1)
  //                        poke(c.io.start, false)
//  //                        peek(c.control.cycleInPassCounter.io)
//  //                        peek(c.control.totalCycleCounter.io)
//  //                        peek(c.control.selectXCounter.io)
//  //                        peek(c.control.io.ready)
//  //                        peek(c.control.io.valid)
  //                      })
  // poke(c.io.start, true)
  // Range(0, 15).foreach(f => {
  //                        step(1)
  //                        poke(c.io.start, false)
//  //                        peek(c.control.cycleInPassCounter.io)
//  //                        peek(c.control.totalCycleCounter.io)
//  //                        peek(c.control.selectXCounter.io)
//  //                        peek(c.control.io.ready)
//  //                        peek(c.control.io.valid)
  //                      })

  poke(c.io.xOut.ready, true)
  for (iter <- 0 until Iters) {
    poke(c.io.start, true)
    println("ljaksdjlaksjdlk")
    expect(c.io.ready, true)
    val nextReadyCycle = Cycle + passesRequired * cyclesPerPass + PUsPerMUs - 2
    println(("###############", nextReadyCycle))
    expectQueue.enqueue((nextReadyCycle, () => {
      println("expecTqueueuee")
      expect(c.io.ready, true)
      shouldBeReady = true
    }))
    for (pass <- 0 until passesRequired) {
      for (i <- 0 until cyclesPerPass) {
        if ((pass != 0 || i != 0) &&
            (pass != passesRequired - 1 || i !=  cyclesPerPass - 1)) {
          println(("hehe", pass, i))
          println((passesRequired, cyclesPerPass))
          expect(c.io.ready, false)
          poke(c.io.start, false)
        }
        for (core <- 0 until p.NumberOfCores) {
          val lower = i * p.K
          val upper = (i + 1) * p.K
          poke(c.io.xIn(core), vecToBigInt(xs(iter)(core).slice(lower, upper)))

          if (i < p.NumberOfPUs) {
            val _i = i
            val _core = core
            val _iter = iter
            val _pass = pass
            expectQueue.enqueue((Cycle + cyclesPerPass, () => {
//                // peek(c.control.io.selectX)
                // for (i <- 0 until p.NumberOfCores) {
//                //   peek(c.activators(i).io.out)
                // }
//                // peek(c.activators(0).io)
//                // peek(c.control.io)
                expect(c.io.xOut.valid, 1)
                val ind = _i + p.NumberOfPUs * _pass
//                peek(c.memoryStreamer.io)
                println(ind)
                expect(c.io.xOut.bits(_core), expectedResults(_iter)(_core)(ind))
                if (_i == p.NumberOfPUs - 1 && _pass == passesRequired - 1) {
                  expect(c.io.done, true)
                }
              }))
          }
        }
//        peek(c.memoryStreamer.io)
        step(1)
        Cycle += 1

//        peek(c.control.totalCycleCounter.io)
//        peek(c.control.cycleInPassCounter.io)
//        peek(c.control.selectXCounter.io)
//        peek(c.control.io.memoryRestart)

        handleQueue(Cycle)
      }
    }
    while (Cycle < nextReadyCycle) {
      expect(c.io.ready, false)
      step(1)
      Cycle += 1
      handleQueue(Cycle)

//        peek(c.control.totalCycleCounter.io)
//        peek(c.control.cycleInPassCounter.io)
//        peek(c.control.selectXCounter.io)
//        peek(c.control.io.memoryRestart)
    }
    expect(c.io.ready, true)
    step(1)
    Cycle += 1
    handleQueue(Cycle)

    for (i <- 0 until waitingSteps(iter)) {
      step(1)
      Cycle += 1
      handleQueue(Cycle)
      poke(c.io.xOut.ready, false)
      println("heheheheheh")
      expect(c.io.ready, false)
      poke(c.io.xOut.ready, true)
      println("laksjd")
      expect(c.io.ready, shouldBeReady)
    }
    shouldBeReady = false
  }
}

class WarpControlTests1(c: WarpControl, p: LayerParameters) extends Tester(c) {

  def peekAll() {
    peek(c.io)
    peek(c.isActive.io)
    peek(c.cycleInPass.io)
    peek(c.cycle.io)
    peek(c.tailCycle.io)
    peek(c.selectX.io)
  }


  poke(c.io.start, false)
  poke(c.io.nextReady, true)

  step(100)


  expect(c.io.ready, true)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  // expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, true)

  poke(c.io.start, true)
  step(1)
  peekAll()

  expect(c.io.ready, false)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  // expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

  poke(c.io.start, false)
  step(1)
  peekAll()

  List.range(0, 10).foreach(i => {
    expect(c.io.ready, false)
    expect(c.io.valid, false)
    expect(c.io.done, false)
    // expect(c.io.memoryRestart, i == 9)
    expect(c.io.chainRestart, false)
    step(1)
    peekAll()
  })

  expect(c.io.ready, false)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  // expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, true)
  step(1)

  List.range(0, 5).foreach(_ => {
    expect(c.io.ready, false)
    expect(c.io.valid, true)
    expect(c.io.done, false)
    // expect(c.io.memoryRestart, false)
    expect(c.io.chainRestart, true)
    step(1)
  })

  expect(c.io.ready, true)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  // expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, true)

  step(1)

  expect(c.io.ready, true)
  expect(c.io.valid, true)
  expect(c.io.done, true)
  // expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, true)

  step(1)
  step(1)
  step(1)

  poke(c.io.start, true)
  // expect(c.io.memoryRestart, false)
  step(1)

  expect(c.io.ready, false)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  // expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)
}

class WarpControlTests2(c: WarpControl, p: LayerParameters) extends Tester(c) {
  poke(c.io.start, false)
  poke(c.io.nextReady, true)
//  peek(c.outputCounter.io)
  step(100)
//  peek(c.outputCounter.io)

  expect(c.io.ready, true)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, true)

  poke(c.io.start, true)
  expect(c.io.memoryRestart, false)
  step(1)
  poke(c.io.start, false)

  List.range(0, 5).foreach(_ => {
    expect(c.io.ready, false)
    expect(c.io.valid, false)
    expect(c.io.done, false)
    expect(c.io.memoryRestart, false)
    expect(c.io.chainRestart, false)

    step(1)
  })

  expect(c.io.ready, false)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, true)

  step(1)

  // Get out the four first ys
  List.range(0, 3).foreach(_ => {
    expect(c.io.ready, false)
    expect(c.io.valid, true)
    expect(c.io.done, false)
    expect(c.io.memoryRestart, false)
    expect(c.io.chainRestart, false)

    step(1)
  })

  expect(c.io.ready, false)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

  step(1)

  expect(c.io.ready, false)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, false)

  step(1)

  // 5th Y     112
  expect(c.io.ready, true)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, true)

  step(1)
  poke(c.io.start, true)

  // 6th Y     113
  expect(c.io.ready, true)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, true)

  step(1)
  poke(c.io.start, false)

  // 7th Y     114
  expect(c.io.ready, false)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

  step(1)
//  peek(c.cycleInPassCounter.io)
//  peek(c.totalCycleCounter.io)
//  peek(c.selectXCounter.io)

  // 8th Y     115
  expect(c.io.ready, false)
  expect(c.io.valid, true)
  expect(c.io.done, true)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

  step(1)
}

class WarpControlTests3(c: WarpControl, p: LayerParameters) extends Tester(c) {
  poke(c.io.start, false)
  poke(c.io.nextReady, true)
//  peek(c.outputCounter.io)
  step(100)
//  peek(c.outputCounter.io)

  expect(c.io.ready, true)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, true)

  poke(c.io.start, true)
  expect(c.io.memoryRestart, false)
  step(1)
  poke(c.io.start, false)

  expect(c.io.ready, false)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

  step(1)

  List.range(0, 4).foreach(_ => {
    expect(c.io.ready, false)
    expect(c.io.valid, false)
    expect(c.io.done, false)
    expect(c.io.memoryRestart, false)
    expect(c.io.chainRestart, false)

    step(1)
  })

  List.range(0, 4).foreach(i => {
    expect(c.io.ready, false)
    expect(c.io.valid, true)
    expect(c.io.done, false)
    expect(c.io.memoryRestart, false)
    expect(c.io.chainRestart, i == 0)

    step(1)
  })

  // 110
  expect(c.io.ready, false)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

//  peek(c.cycleInPassCounter.io)
  step(1)

  // 111
  expect(c.io.ready, true)
  expect(c.io.valid, false)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, false)

//  peek(c.totalCycleCounter.io)
//  peek(c.cycleInPassCounter.io)
  step(1)

  // 112
  expect(c.io.ready, true)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, true)
  expect(c.io.chainRestart, true)

//  peek(c.totalCycleCounter.io)
//  peek(c.cycleInPassCounter.io)

  poke(c.io.start, true)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, true)

  step(1)
  poke(c.io.start, false)

//  peek(c.cycleInPassCounter.io)

  // 113
  expect(c.io.ready, false)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

  step(1)

  // 114
  expect(c.io.ready, false)
  expect(c.io.valid, true)
  expect(c.io.done, false)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)

  step(1)

  // 115
  expect(c.io.ready, false)
  expect(c.io.valid, true)
  expect(c.io.done, true)
  expect(c.io.memoryRestart, false)
  expect(c.io.chainRestart, false)
}

object WarpTest {
  def main(args: Array[String]) {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")

    val p1 = new LayerParameters(
      K=1,
      BiasWidth=8,
      AccumulatorWidth=10,
      NumberOfPUs=8,
      NumberOfMS=1,
      MatrixWidth=12,
      MatrixHeight=8,
      NumberOfCores=3
      )
    chiselMainTest(margs, () => Module(new WarpControl(p1))) {
      c => new WarpControlTests1(c, p1)
    }

    val p2 = new LayerParameters(
      K=2,
      BiasWidth=8,
      AccumulatorWidth=10,
      NumberOfPUs=4,
      NumberOfMS=2,
      MatrixWidth=12,
      MatrixHeight=8,
      NumberOfCores=3
      )
    chiselMainTest(margs, () => Module(new WarpControl(p2))) {
      c => new WarpControlTests2(c, p2)
    }

    val p3 = new LayerParameters(
      K=2,
      BiasWidth=8,
      AccumulatorWidth=10,
      NumberOfPUs=4,
      NumberOfMS=4,
      MatrixWidth=12,
      MatrixHeight=8,
      NumberOfCores=3
      )
    chiselMainTest(margs, () => Module(new WarpControl(p3))) {
      c => new WarpControlTests3(c, p3)
    }

    return;
    val p = new LayerParameters(
      K=2,
      BiasWidth=8,
      AccumulatorWidth=10,
      NumberOfPUs=4,
      NumberOfMS=4,
      MatrixWidth=12,
      MatrixHeight=8,
      NumberOfCores=1
      )
    Random.setSeed(12)


    val weights = Array(
      Array(1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1),
      Array(0, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 1),
      Array(1, 0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0),
      Array(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1),
      Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0),
      Array(0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0),
      Array(0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1),
      Array(1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1))
    val biases = Array(0, -6, 14, 5, 2, -5, 1, 9)

    chiselMainTest(margs, () => Module(new Warp(p, weights, biases))) {
      c => new WarpTests(c, p, weights, biases)
    }

  }
}
