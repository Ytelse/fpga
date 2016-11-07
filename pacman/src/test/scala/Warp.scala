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

  val Iters = 4
  val xs = List.fill(Iters){List.fill(p.NumberOfCores)(
      List.fill(p.MatrixWidth){ Random.nextInt(2) })}

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
      return;
    var top = expectQueue.minBy(e => e._1)
    if (top._1 == cycle) {
      top._2.apply
      expectQueue.dequeue
      handleQueue(cycle)
    }
  }
  var shouldBeReady = false


  for (iter <- 0 until Iters) {
    poke(c.io.start, true)
    expect(c.io.ready, true)
    val nextReadyCycle = Cycle + passesRequired * cyclesPerPass + PUsPerMUs - 2
    expectQueue.enqueue((nextReadyCycle, () => {
      expect(c.io.ready, true)
      shouldBeReady = true
    }))
    for (pass <- 0 until passesRequired) {
      for (i <- 0 until cyclesPerPass) {
        if (pass != 0 || i != 0) {
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
                expect(c.io.xOut.valid, 1)
                expect(c.io.xOut.bits(_core),
                  expectedResults(_iter)(_core)(_i))
                if (_i == p.NumberOfPUs - 1 && _pass == passesRequired - 1) {
                  expect(c.io.done, true)
                }
              }))
          }
        }
        step(1)
        Cycle += 1
        handleQueue(Cycle)
      }
    }
    for (i <- 0 until waitingSteps(iter)) {
      step(1)
      Cycle += 1
      handleQueue(Cycle)
      poke(c.io.xOut.ready, false)
      expect(c.io.ready, false)
      poke(c.io.xOut.ready, true)
      expect(c.io.ready, shouldBeReady)
    }
    shouldBeReady = false
  }

  // println("Weights")
  // Matrix.printMat(weights)
  // println("Biases")
  // Matrix.printMat(Array(biases))

  // for (i <- 0 until p.NumberOfCores * Iters) {
  //   println("==========")
  //   println("Step %d".format(i))
  //   println("xs")
  //   Matrix.printMat(Array(xs(i).toArray))
  //   println("output")
  //   Matrix.printMat(Array(expectedResults(i).toArray))
  // }

  {
  }
}

object WarpTest {
  def main(args: Array[String]) {
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    val p = new LayerParameters(
      K=2,
      BiasWidth=8,
      AccumulatorWidth=10,
      NumberOfPUs=4,
      NumberOfMS=2,
      MatrixWidth=12,
      MatrixHeight=8,
      NumberOfCores=3
      );
    Random.setSeed(1)


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
