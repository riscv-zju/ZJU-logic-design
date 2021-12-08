![logo](./img/logic101.png)
Digital Logic Design 101 [Chisel Version]
=======================
This repository contains a chisel version of the laboratory of the Zhejiang University Logic and Computer Design Fundamentals course.

**Notice**: The board used here is [Nexys A7](https://digilent.com/reference/programmable-logic/nexys-a7/start), not the [SWORD](https://digilent.com/reference/programmable-logic/sword/start) board used in the class.

## How to Compile
Currently, we only support compile process, which converts chisel to verilog.
To compile the code, you need install sbt from [here](https://www.scala-sbt.org/1.x/docs/Setup.html).

You can select your target lab and task by using `LAB` and `CONFIG` variables.
```bash
# After install sbt
make LAB=4 CONFIG=LampCtrlTarget
```

Labs List:
- Lab4: LampCtrlTarget, LampCtrlDelayTarget
- Lab5: DecodeTestTarget, LampCtrlTarget
- Lab6: DecodeTestTarget
- Lab7: TopTarget
- Lab8: TopTarget
- Lab11: cnt4bTarget, cnt16bTarget
- Lab12: Task1Target, Task2Target, Task3Target
- Lab13: LEDTarget, SEGTarget
- Lab14: ClockTarget

## How to Sythesis
Use `synthesis` target to generate the bitstream.
```bash
make synthesis LAB=4 CONFIG=LampCtrlTarget
```
Use `fpga` target to program your board.
```bash
make fpga LAB=4 CONFIG=LampCtrlTarget
```
In vivado IDE, select `Flow - Open Hardware Manager` to load bit file under `build/vivado/lab?` into your board.


## How to Simulation
Using chisel-test is not a good choice, I recommend you use [iverilog](https://command-not-found.com/iverilog), vivado and other EDA (e.g. synopsys vcs) to simulate your design.
We will provide testbench in the future.


## *In the End*
Using chisel to complete labs will lose some gate-level details, and I do not recommend you use the content of this repository directly, so I do not provide a submittable implementation (although porting to the SWORD board is easy).

I hope these examples will help you get started with chisel. 

For **students** interested in computer architecture **in the class**, feel free to contact me.

<p align="center">
  <img width="200" src="./img/1.0.jpg">
</p>

<p align="right">
  phantom <br>
  2021.12.5
</p>
