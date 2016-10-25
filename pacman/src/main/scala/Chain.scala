package Pacman

import Chisel._

class Chain(numProcessingUnits: Int,
            addrWidth: Int,
            k: Int,
            numMemoryReaders: Int,
            memoryOffsets: List[Int],
            readingLength: Int)
    extends Module {
  val yWidth = 10
  val unitsPerMemoryReader = numProcessingUnits / numMemoryReaders
  val dataLineWidth = k * unitsPerMemoryReader

  val processingUnits =
    List.fill(numProcessingUnits)(Module(new ProcessingUnit(k)))
  val memoryReaders =
    List
      .range(0, numMemoryReaders)
      .map(
        (i) =>
          Module(
            new MemoryReader(unitsPerMemoryReader,
                             k,
                             addrWidth,
                             memoryOffsets(i),
                             readingLength)))

  val io = new Bundle {
    val addressLines =
      Vec.fill(numMemoryReaders) { Bits(width = addrWidth) }.asOutput
    val dataLines =
      Vec.fill(numMemoryReaders) { Bits(width = dataLineWidth) }.asInput
    val resetIn = Bool().asInput
    val xs = Bits(width = k).asInput
    val ys = Vec.fill(numProcessingUnits) { Bits(width = yWidth) }.asOutput
    // Add x_out and reset_out ?
  }

  for (i <- 0 until numProcessingUnits) {
    io.ys(i) := processingUnits(i).io.yOut
  }

  if (processingUnits.length > 1) {
    processingUnits
      .sliding(2)
      .foreach((lst) => {
        val a = lst(0)
        val b = lst(1)
        b.io.resetIn := a.io.resetOut
        b.io.xs := a.io.xOut
      })
  }

  processingUnits(0).io.xs := io.xs
  processingUnits(0).io.resetIn := io.resetIn


  processingUnits
    .grouped(unitsPerMemoryReader)
    .zip(memoryReaders.iterator)
    .foreach({
      case (punits, reader) => {
        punits.zipWithIndex.map({
          case (punit, i) => {
            punit.io.ws := reader.io.weights(i)
          }
        })
      }
    })

  for (i <- 0 until numMemoryReaders) {
    memoryReaders(i).io.data := io.dataLines(i)
    io.addressLines(i) := memoryReaders(i).io.addr
  }
}
