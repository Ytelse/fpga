package Pacman

class LayerParameters(
    val K: Int = 0,
    val BiasWidth: Int = 0,
    val AccumulatorWidth: Int = 0,
    val NumberOfPUs: Int = 0,
    val AddressWidth: Int = 0,
    val NumberOfMS: Int = 0,
    val MatrixWidth: Int = 0,
    val MatrixHeight: Int = 0,
    val NumberOfCores: Int = 0
  ) {
  override def toString(): String = {
    ("K: %d\n" +
    "BiasWidth: %d\n" +
    "AccumulatorWidth: %d\n" +
    "NumberOfPUs: %d\n" +
    "AddressWidth: %d\n" +
    "NumberOfMS: %d\n" +
    "MatrixWidth: %d\n" +
    "MatrixHeight: %d\n" +
    "NumberOfCores: %d\n").format(
  K,
  BiasWidth,
  AccumulatorWidth,
  NumberOfPUs,
  AddressWidth,
  NumberOfMS,
  MatrixWidth,
  MatrixHeight,
  NumberOfCores
    )
  }
}

class GearBoxParameters(
  val First: LayerParameters,
  val Second: LayerParameters) {
    override def toString(): String = {
      "First:\n" + First.toString() + "\nSecond:\n" + Second.toString()
    }
}
