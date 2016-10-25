package Pacman

import util.Random

import Chisel._

object Stuff {
  def getWeightMatrix(matWidth: Int, matHeight: Int) : Seq[Int] =
    Seq.fill(matWidth * matHeight)(Random.nextInt(2))

  def getInputVector(len: Int): Seq[Int] =
    Seq.fill(len)(Random.nextInt(2))

  def matrixMul(matrix: Seq[Int], vector: Seq[Int],
                matWidth: Int, matHeight: Int)
      : Seq[Int] = {
    var result = new Array[Int](matHeight)
    for (y <- 0 until matHeight) {
      val i = y * matWidth + matHeight
      var sum = 0
      for (z <- 0 until matWidth) {
        val w_i = y * matWidth + z
        val x_i = z
        sum += (if (matrix(w_i) == vector(x_i)) 1 else 0)
      }
      result(y) = sum
    }
    result
  }

  def printMat(mat: Seq[Int], h: Int, w: Int) {
    for (y <- 0 until h) {
      for (z <- 0 until w) {
        val i = y * w + z
        print(mat(i))
        print(" ")
      }
      print("\n")
    }
    print("\n")
  }


}

class ChainTests(c: Chain,
                 processingUnits: Int,
                 addrWidth: Int,
                 k: Int,
                 memoryReaders: Int,
                 memoryOffsets: List[Int],
                 matrixDimensions: (Int, Int)
  ) extends Tester(c) {

  def posMod(n: Int, a: Int): Int = {
    (n + a) % a
  }

  val N_ITERS = 3
  Random.setSeed(1234)
  val (matHeight, matWidth) = matrixDimensions
  val weights = Stuff.getWeightMatrix(matWidth, matHeight)

  val PUsPerReader = processingUnits / memoryReaders

  val totalSteps = (matWidth * matHeight) / (k * processingUnits)
  val offsetArray =
    List.range(0, matHeight / processingUnits)
      .flatMap((i) => List.range(0, matWidth / k)
                  .map((n) => n * k + i * matWidth * processingUnits))

  def vecToBigInt(vec: Seq[Int]): BigInt = {
    int(Bits(vec.reverse.map((n) => n.toString).fold("b")(_ + _)))
  }


  // How much the addresses should change between each step
  val expectedAddrStep = PUsPerReader * k / 8

  var result = Seq(1)
  // For each input vector
  for (iteration <- 0 until N_ITERS) {
    val xs = Stuff.getInputVector(matWidth)
    val previousRes = result
    result = Stuff.matrixMul(weights, xs, matWidth, matHeight)

    // Stuff.printMat(xs, 1, matWidth);
    // Stuff.printMat(weights, matHeight, matWidth);
    // Stuff.printMat(result, 1, matHeight);

    // One step is reading `k` x values. After matWidth/k
    // steps we get out the first y.
    for (stepNumber <- 0 until totalSteps) {
      // The index along the x axis of the matrix
      val rowPassLength = matWidth / k
      val rowPassIndex = stepNumber % rowPassLength
      val whichRowPass = stepNumber / rowPassLength
      val passHeight = processingUnits
      val numPasses = matHeight / passHeight

      // Check Y outputs from previous cyclepassstep
      if (iteration != 0 && rowPassIndex < processingUnits) {
        val resultToCheckAgainst = if (whichRowPass == 0) previousRes else result
        val whichRowPassToCheck = posMod(whichRowPass - 1, numPasses)
        val i = whichRowPassToCheck * passHeight + rowPassIndex
        expect(c.io.ys(rowPassIndex), resultToCheckAgainst(i))
      }

      // Check that addresses are incremented correctly
      for (mrI <- 0 until memoryReaders) {
        val addr = expectedAddrStep * posMod(stepNumber, totalSteps)
        expect(c.io.addressLines(mrI), addr)
      }

      // Loop over memory reades, and set their data lines.
      for (mrI <- 0 until memoryReaders) {
        var num = new Array[Int](k * PUsPerReader)
        // For each processing unit in the reader,
        // we get the weights coefficients, and merge
        for (puI <- 0 until PUsPerReader){
          val puNumber = puI + mrI * PUsPerReader
          val offsetIndex = posMod(stepNumber - puNumber, totalSteps)
          val weightIndex = offsetArray(offsetIndex) + matWidth * puNumber
          for (i <- 0 until k) {
            num(puI * k + i) = weights(weightIndex + i)
          }
        }
        poke(c.io.dataLines(mrI), vecToBigInt(num))
      }

      val xIndex = k * (stepNumber % (matWidth / k))
      poke(c.io.xs, vecToBigInt(xs.slice(xIndex, xIndex + k)))

      // If we are to start a new row pass, reset the chain
      if (rowPassIndex == 0) {
        poke(c.io.resetIn, true)
      } else if (rowPassIndex == 1) {
        poke(c.io.resetIn, false)
      }

      step(1)
    }
  }
}

object ChainTest {

  def divisors(n: Int): Seq[Int] = {
    List.range(1, n + 1).filter((e) => n % e == 0)
  }

  def main(args: Array[String]): Unit = {
    val margs = Array("--backend", "c", "--genHarness",
                      "--compile", "--test")
    val addrWidth = 64;

    val matHeight = 8
    val matWidth = 32
    for (matHeight <- List(4, 8, 10, 28)) {
      for (matWidth <- List(8, 16, 24, 64)) {
      val possibleReaderWidth = divisors(matWidth / 8)
      for (readerWidth <- possibleReaderWidth) {
        val possibleNumReaders = List.range(1, matWidth / (readerWidth * 8) + 1)
        for (numReaders <- possibleNumReaders) {
          val memoryOffsets = List.fill(numReaders)(0)
          val readingLength = (matWidth * matHeight) / (8 * numReaders)

          val possiblePUsPerReader = divisors(readerWidth * 8)
            .filter((p) => {
              val numPus = p * numReaders
              numPus <= matHeight && matHeight % numPus == 0
            })
          for (PUsPerReader <- possiblePUsPerReader) {
            val k = (8 * readerWidth) / PUsPerReader
            val numPUs = PUsPerReader * numReaders

            println("matHeight", matHeight)
            println("matWidth", matWidth)
            println("numPUs", numPUs)
            println("numReaders", numReaders)
            println("PUsPerReader", PUsPerReader)
            println("readerWidth", readerWidth)
            println("k", k)
            println("memoryOffsets", memoryOffsets)
            println("addrWidth", addrWidth)
            println("readingLength", readingLength)

            chiselMainTest(margs, () =>
                Module(new Chain(numPUs,
                                 addrWidth,
                                 k,
                                 numReaders,
                                 memoryOffsets,
                                 readingLength))) {
              c => new ChainTests(c,
                                  numPUs,
                                  addrWidth,
                                  k,
                                  numReaders,
                                  memoryOffsets,
                                  (matHeight, matWidth))
              }
            }
          }
        }
      }
    }
  }
}
