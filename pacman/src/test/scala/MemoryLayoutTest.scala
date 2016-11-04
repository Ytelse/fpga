package Pacman

import org.scalatest._
import Assertions._

class ExampleSpec extends FlatSpec with Matchers {
  "MemoryLayout" should "lol" in {
    val parameters = new LayerParameters(
      K = 2,
      NumberOfPUs = 4,
      NumberOfMS = 2,
      MatrixWidth = 12,
      MatrixHeight = 8,
      BiasWidth = 8)
    val weights = Array(
      Array(0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1),
      Array(1, 1, 0, 0, 1, 1, 1, 0, 1, 0, 1, 0),
      Array(1, 0, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0),
      Array(0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 1),
      Array(0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0),
      Array(1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0),
      Array(0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1),
      Array(1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0))
    val bias = Array(0x14, 0xA0, 0xFF, 0xCA, 0x82, 0x45, 0x80, 0x01)
    val (weightStreams, biasStream) = MemoryLayout.getStreams(parameters, weights, bias)

    val correctWeightStreams = Array(
      Array("b0110",
            "b0111",
            "b1000",
            "b1011",
            "b0110",
            "b1110",
            "b0110",
            "b1011",
            "b1010",
            "b1001",
            "b0101",
            "b1010"),
      Array("b0110",
            "b1111",
            "b1000",
            "b1100",
            "b0001",
            "b0101",
            "b1100",
            "b0010",
            "b0111",
            "b1010",
            "b0101",
            "b1110"))
    val correctBiasStream = Array("b00010100", "b10100000", "b11111111", "b11001010",
                                  "b00000000", "b00000000",
                                  "b10000010", "b01000101", "b10000000", "b00000001",
                                  "b00000000", "b00000000")

    weightStreams
      .zip(correctWeightStreams)
      .foreach({
        case (ws, cws) => {
          assert(ws.deep == cws.deep)
        }
      })

    assert(biasStream.deep == correctBiasStream.deep)
  }

}
