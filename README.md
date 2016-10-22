# FPGA

This is the implementation of PACMAN, written in Chisel.

### Requirements

You'll need `sbt`, and that's pretty much it.

### Running

To run the thing, `cd` to `pacman/`, and run 

```bash
$ sbt "test:run-main Pacman.<MODULENAME>Test"
$ # Eg.
$ sbt "test:run-main Pacman.ProcessingUnitTest"
```

Alternatively, you can start `sbt` first, and then only run

```
> test:run-main Pacman.ProcessingUnitTest
```

This way you don't have to wait for `sbt` to start each time, which actually takes some time.
