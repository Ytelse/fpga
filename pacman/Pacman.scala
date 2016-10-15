//  
//  Entry point of entire application
//

package Pacman

import Chisel._

object Application {

  def test() {
    System.out.println("Running tests");
    val margs = Array("--backend", "c", "--genHarness", "--compile", "--test")
    
    chiselMainTest(margs, () => Module(new Runner())) {
      c => new RunnerTests(c)
    }

    val carN = 4;
    chiselMainTest(margs, () => Module(new Car(carN))) {
      c => new CarTests(c, carN)
    }
  }

  def main(args: Array[String]): Unit = {
    test()
  }
}
