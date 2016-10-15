//
//  Entry point of entire application
//

package Pacman

import Chisel._

object Application {

  def test() {
    System.out.println("Running tests");
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")

    chiselMainTest(margs, () => Module(new ColumnRunner(1))) {
      c => new ColumnRunnerTests(c)
    }

    val carN = 8;
    val carK = 1;
    chiselMainTest(margs, () => Module(new Car(carN, carK))) {
      c => new CarTests(c, carN, carK)
    }
  }

  def main(args: Array[String]): Unit = {
    test()
  }
}
