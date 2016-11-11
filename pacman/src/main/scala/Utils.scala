package Pacman

import scala.io.Source

class TestData(
  // 10 Matrices
  matrices: Array[Array[Array[Int]]],
  // 10 sets of 5 vectors of result data
  vectors: Array[Array[Array[Int]]]
)

object Utils {

  def stringToVector(string: String): Array[Int] = {
    string.split(" ")
          .filter(s => s.length != 0)
          .map(s => {
            val n = Integer.parseInt(s, 10)
            if (n == 1) 1
            else if (n == -1) 0
            else 2
          })
          .toArray
  }

  def sliceToMatrix(slice: Seq[String]): Array[Array[Int]] = {
    slice.map(stringToVector).toArray
  }

  def printMatrix(mat: Array[Array[Int]]) {
    mat.foreach(line => {
      line.foreach(n => {
        print("%1d ".format(n))
      })
      println()
    })
  }

  def fromOneHot(arr: Array[Int]): Int = {
    arr.indexWhere(_ > 0)
  }

  def readDumpFile() {
    // This function only works for the `net_dump` file.
    // Please do not use it for anything else :)
    val lines = Source.fromFile("net_dump").getLines.toArray
    var i = 1
    val firstMatrix = sliceToMatrix(lines.slice(i, i + 256))
    i += 257
    val secondMatrix = sliceToMatrix(lines.slice(i, i + 256))
    i += 257
    val thirdMatrix = sliceToMatrix(lines.slice(i, i + 256))
    i += 257
    val fourthMatrix = sliceToMatrix(lines.slice(i, i + 10))
    i += 10

    i += 1
    val NImages = 10
    val NResults = 5
    val results = List.range(0, NImages).map(n => {
      List.range(0, NResults).map(m => {
        val v = stringToVector(lines(i))
        i += 2
        v
      }).toArray
    }).toArray

    // Input vector is results(i)(0)
    // results.foreach(r => {
    //   printMatrix(r(0).grouped(28).map(_.toArray).toArray)
    //   println("%d".format(fromOneHot(r(4))))
    // })

    new TestData(
      Array(firstMatrix, secondMatrix, thirdMatrix, fourthMatrix),
      results
    )
  }
}
