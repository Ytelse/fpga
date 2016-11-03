package Pacman

import Chisel._

class Chain(parameters: LayerParameters)
    extends Module {

      // TODO REmove
  val numProcessingUnits: Int = parameters.NumberOfPUs
  val addrWidth: Int = parameters.AddressWidth
  val k: Int = parameters.K
  val numMemoryReaders: Int = parameters.NumberOfMS
  val memoryOffsets: List[Int] = parameters.MemoryOffsets
  val readingLength: Int = parameters.ReadingLength
  val yWidth = parameters.AccumulatorWidth



  val unitsPerMemoryReader = numProcessingUnits / numMemoryReaders
  val dataLineWidth = k * unitsPerMemoryReader

  val processingUnits =
    List.fill(numProcessingUnits)(Module(new ProcessingUnit(parameters)))
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
    val bias = UInt(width = parameters.BiasWidth).asInput
    val restartIn = Bool().asInput
    val xs = Bits(width = k).asInput
    val ys = Vec.fill(numProcessingUnits) { Bits(width = yWidth) }.asOutput
    // Add x_out and reset_out ?
  }

  for (i <- 0 until numProcessingUnits) {
    io.ys(i) := processingUnits(i).io.yOut
    processingUnits(i).io.bias := io.bias
  }

  if (processingUnits.length > 1) {
    processingUnits
      .sliding(2)
      .foreach((lst) => {
        val a = lst(0)
        val b = lst(1)
        b.io.restartIn := a.io.restartOut
        b.io.xs := a.io.xOut
      })
  }

  processingUnits(0).io.xs := io.xs
  processingUnits(0).io.restartIn := io.restartIn


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
