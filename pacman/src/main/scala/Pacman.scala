package Pacman

import Chisel._

object PacmanSetup {
  def getLayers : List[LayerData] = {
    val testData = Utils.readDumpFile()
    val parametersList = Array(
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 32,
        AddressWidth = 0,
        NumberOfMS = 16,
        MatrixWidth = 784,
        MatrixHeight = 256,
        NumberOfCores = 4
      ),
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 16,
        AddressWidth = 0,
        NumberOfMS = 8,
        MatrixWidth = 256,
        MatrixHeight = 256,
        NumberOfCores = 3
      ),
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 16,
        AddressWidth = 0,
        NumberOfMS = 8,
        MatrixWidth = 256,
        MatrixHeight = 256,
        NumberOfCores = 3
      ),
      new LayerParameters(
        K = 16,
        BiasWidth = 8,
        AccumulatorWidth = 10,
        NumberOfPUs = 10,
        AddressWidth = 0,
        NumberOfMS = 5,
        MatrixWidth = 256,
        MatrixHeight = 10,
        NumberOfCores = 1
      )
    )
    val layers = Range(0, 4).map(i =>
      new LayerData(
        parameters=parametersList(i),
        weights=testData.matrices(i),
        biases=testData.biases(i)
      )
    ).toList

    return layers
  }
}


class Pacman(
  inDataWordWidth: Int,
  val layers: List[LayerData] = PacmanSetup.getLayers
) extends Module {

  def split(bits: Bits, n: Int) = Vec[Bits] {
    if(bits.getWidth % n != 0) {
      throw new AssertionError("Can't split %d bits in %d parts".format(bits.getWidth, n))
    }
    val wordWidth = bits.getWidth / n
    Vec(
      Range(0, n)
        .map(i => {
               val upper = (i + 1) * wordWidth - 1
               val lower = i * wordWidth
               bits(upper, lower)
             }).toArray
    )
  }

  val lastLayer = layers.last
  val firstLayer = layers(0)
  val inputCores = firstLayer.parameters.NumberOfCores
  val outputCores = lastLayer.parameters.NumberOfCores
  val netAnswerWidth = lastLayer.parameters.MatrixHeight
  val netInputWordWidth = firstLayer.parameters.K * firstLayer.parameters.NumberOfCores
  val netInputWordPerBlock = firstLayer.parameters.MatrixWidth / firstLayer.parameters.K

  val io = new Bundle {
    val inDataStream = Decoupled(Bits(width=inDataWordWidth)).flip
    val digitOut = Decoupled(UInt(width=log2Up(netAnswerWidth)))
  }

  val widthConverter = Module(new AToN(inDataWordWidth, netInputWordWidth))
  val buffer = Module(new CircularPeekBuffer(3, netInputWordPerBlock, netInputWordWidth))
  val net = Module(new Net(layers))
  //val deinterleaver = Module(new Interleaver(lastLayer.parameters))

  io.inDataStream <> widthConverter.io.a

  widthConverter.io.n <> buffer.io.wordIn

  // io.inDataStream <> buffer.io.wordIn

  net.io.xsIn := split(buffer.io.wordOut, net.io.xsIn.length)
  net.io.start := buffer.io.startOut
  buffer.io.pipeReady := net.io.ready

  // deinterleaver.io.doneIn := net.io.done
  // deinterleaver.io.oneBitPerCore.bits := net.io.xsOut.reduceLeft(Cat(_, _))
  // deinterleaver.io.oneBitPerCore.valid := net.io.xsOutValid
  // net.io.pipeReady := deinterleaver.io.oneBitPerCore.ready

  // io.digitOut.bits := OHToUInt(deinterleaver.io.oneHotOut.bits)
  // io.digitOut.valid := deinterleaver.io.oneHotOut.valid
  // deinterleaver.io.oneHotOut.ready := io.digitOut.ready

  val bitBuffer = Module(new BitToWord(10))
  bitBuffer.io.bit := net.io.xsOut(0)
  bitBuffer.io.enable := net.io.xsOutValid

  io.digitOut.bits := OHToUInt(bitBuffer.io.word)
  io.digitOut.valid := Reg(init=Bool(false), next=net.io.done)
  net.io.pipeReady := io.digitOut.ready

}
