package logic101.lab._11

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._


class cnt4bTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new cnt4bTop()(p)
  case PinKey => (dut: cnt4bTopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((LED(0), dut.LED), 
          (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
    ) }
})

class cnt16bTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new cnt16bTop()(p)
  case PinKey => (dut: cnt16bTopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((LED(0), dut.LED), (SW(0), dut.SW), 
          (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
    ) }
})

class clkdiv(n: BigInt) extends Module {
  val io = IO(new Bundle{
    val clk = Output(Clock())
  })

  val cnt = RegInit(0.U(log2Ceil(n).W))
  val out = RegInit(false.B)
  io.clk := out.asClock

  when(cnt < n.U) {
    cnt := cnt + 1.U
  } .otherwise {
    cnt := 0.U
    out := ~out
  }
}

class counter4b extends Module {
  val io = IO(new Bundle{
    val Q = Output(UInt(4.W))
    val Rc = Output(Bool())
  })

  val FD = RegInit(VecInit(Seq.fill(4)(0.U(1.W))))

  FD(0) := ~FD(0)
  FD(1) := ~FD(0) & FD(1) | FD(0) & ~FD(1)
  FD(2) := ~FD(0) & FD(2) | ~FD(1) & FD(2) | FD(0) & FD(1) & ~FD(2)
  FD(3) := ~FD(0) & FD(3) | ~FD(1) & FD(3) | ~FD(2) & FD(3) | FD(0) & FD(1) & FD(2) & ~FD(3)
  io.Q := FD.asUInt
  io.Rc := io.Q.andR
}

class cnt4bTopIO extends Bundle {
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
  val LED = Output(Bool())
}

class cnt4bTop(implicit p: Parameters) extends Module {
  val io = IO(new cnt4bTopIO)

  val div = Module(new clkdiv(50000000))
  val cnt = withClock(div.io.clk) { Module(new counter4b) }
  io.LED := cnt.io.Rc

  val display = Module(new logic101.lab._7.DispNum)
  display.io.hexs := Cat(0.U(12.W), cnt.io.Q)
  display.io.points := "b0000".U
  display.io.LES := "b0001".U

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
}

class revcounter(n: Int) extends Module {
  val io = IO(new Bundle{
    val S = Input(Bool())
    val Q = Output(UInt(n.W))
    val Rc = Output(Bool())
  })

  val cnt = RegInit(0.U(n.W))

  when(io.S) {
    cnt := cnt + 1.U
  } .otherwise {
    cnt := cnt - 1.U
  }

  io.Q := cnt
  io.Rc := Mux(io.S, io.Q.andR, ~io.Q.orR)
}

class cnt16bTopIO extends Bundle {
  val SW = Input(Bool())
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
  val LED = Output(Bool())
}

class cnt16bTop(implicit p: Parameters) extends Module {
  val io = IO(new cnt16bTopIO)

  val div = Module(new clkdiv(5000000))
  val cnt = withClock(div.io.clk) { Module(new revcounter(16)) }
  cnt.io.S := io.SW
  io.LED := cnt.io.Rc

  val display = Module(new logic101.lab._7.DispNum)
  display.io.hexs := cnt.io.Q
  display.io.points := "b0000".U
  display.io.LES := "b1111".U

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
}
