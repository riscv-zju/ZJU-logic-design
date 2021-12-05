![logo](./scripts/logic101.png)
Digital Logic Design 101 [Chisel Version]
=======================
This repository contains a chisel version of the laboratory of the Zhejiang University Computer and Logic Design Fundamentals course.

**Notice**: The board used here is [Nexys A7](https://digilent.com/reference/programmable-logic/nexys-a7/start), not the [SWORD](https://digilent.com/reference/programmable-logic/sword/start) board used in the class.

## How to Compile
Currently, we only support compile process, which converts chisel to verilog.
To compile the code, you need install sbt from [here](https://www.scala-sbt.org/1.x/docs/Setup.html).

You can select your target lab and task by using `LAB` and `CONFIG` variables.
```bash
# After install sbt
make LAB=4 CONFIG=LampCtrlTarget
```

## How to Sythesis
You need to create the vivado project manually and add the files generated under `build` directory to the project.
We will provide synthesis scripts in the future.


## How to Simulation
Using chisel-test is not a good choice, I recommend you use [iverilog](https://command-not-found.com/iverilog), vivado and other EDA(e.g. synopsys vcs) to simulate your design.
We will provide testbench in the future.


## *In the End*
Using chisel to complete labs will lose some gate-level details, and I do not recommend you use the content of this repository directly, so I do not provide a submittable implementation (although porting to the SWORD board is easy).

I hope these examples will help you get started with chisel. 

For students interested in computer architecture in class, feel free to contact me.