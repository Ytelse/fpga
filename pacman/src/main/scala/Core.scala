package Pacman

import Chisel._

import org.scalatest.Assertions

class Core(parameters: LayerParameters) extends Module {
  // TODO: Add rest of used parameters?
  // NOTE: for some reason, assert required Chisel.Data
  // types. Maybe its inherited from Module?
  assert(UInt(parameters.MatrixWidth) > UInt(0))

  var chain = Module(new Chain(parameters))
  val io = new Bundle {
    val restart = Bool().asInput
    val ws = Vec.fill(parameters.NumberOfPUs)
                  {Bits(width = parameters.K) }.asInput
    val xs = Bits(width = parameters.K).asInput
    val bias = Bits(width = parameters.BiasWidth).asInput
    val out = Bits(width = parameters.NumberOfPUs).asOutput
  }
  // Needed to set default. How to do this in `Bits`?
  io.out := UInt(0)

  chain.io.weights := io.ws
  chain.io.bias := io.bias
  chain.io.restartIn := io.restart
  chain.io.xs := io.xs

  for (i <- 0 until parameters.NumberOfPUs) {
    // TODO: Handle overflow!
    val tmpWidth = parameters.AccumulatorWidth + 2
    val value = chain.io.ys(i) * SInt(2, width=tmpWidth)
    - SInt(parameters.MatrixWidth)
    // Output is the upper bit
    io.out(i) := value(tmpWidth)
  }
  // Count restarts, and send restart when we're done
}
