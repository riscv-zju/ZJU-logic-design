package logic101.lab._12

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._


class Task1Target extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new Task1Top()(p)
  case PinKey => (dut: TopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
          (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(15), dut.SW(6)), 
          (BTN("LEFT"), dut.BTN(2)), (BTN("CENTER"), dut.BTN(1)), (BTN("RIGHT"), dut.BTN(0)),
          (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
    ) }
})

class Task2Target extends Config(
  new Task1Target().alter((site,here,up) => {
    case TargetKey => (p: Parameters) => new Task2Top()(p)
  })
)

class Task3Target extends Config(
  new Task1Target().alter((site,here,up) => {
    case TargetKey => (p: Parameters) => new Task3Top()(p)
  })
)


class RegControl(n: Int, selfgen: Boolean) extends Module {
  val io = IO(new Bundle{
    val btn = Input(Bool())
    val select = Input(Bool())
    val direct = if (selfgen) Some(Input(Bool())) else None
    val in0 = if (selfgen) None else Some(Input(UInt(n.W)))
    val in1 = Input(UInt(n.W))
    val out = Output(UInt(n.W))
  })

  val reg = RegInit(0.U(n.W))
  val div = Module(new logic101.lab._11.clkdiv(50000))
  val db = withClock(div.io.clk) { Module(new logic101.lab._8.DeBounce) }
  db.io.btn := io.btn
  val load = !RegNext(db.io.dbtn) && db.io.dbtn

  val adder_out = if (selfgen) {
    val m = Module(new logic101.lab._8.Adder(4))
    m.io.A := reg
    m.io.B := 1.U
    m.io.Ctrl := io.direct.get
    m.io.S.asUInt
  } else 0.U

  val in = Mux(io.select, io.in1, 
    if (selfgen) adder_out
    else io.in0.get
  )

  when (load) {
    reg := in
  }

  io.out := reg
}

class TopIO extends Bundle {
  val SW = Input(Vec(7, Bool()))
  val BTN = Input(Vec(3, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
}

class Task1Top(implicit p: Parameters) extends TopModule {
  val io = IO(new TopIO)

  val a = Module(new RegControl(4, true))
  a.io.btn := io.BTN(2)
  a.io.select := io.SW(6)
  a.io.direct.get := io.SW(0)
  a.io.in1 := 0.U

  val display = Module(new logic101.lab._7.DispNum)
  display.io.hexs := Cat(0.U(12.W), a.io.out)
  display.io.points := "b0000".U
  display.io.LES := "b0001".U

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
}

class Task2Top(implicit p: Parameters) extends TopModule {
  val io = IO(new TopIO)

  val tri_select = Wire(UInt(4.W))
  val a = Module(new RegControl(4, true))
  a.io.btn := io.BTN(2)
  a.io.select := io.SW(6)
  a.io.direct.get := io.SW(0)
  a.io.in1 := tri_select

  val b = Module(new RegControl(4, true))
  b.io.btn := io.BTN(1)
  b.io.select := io.SW(6)
  b.io.direct.get := io.SW(1)
  b.io.in1 := tri_select

  val c = Module(new RegControl(4, false))
  c.io.btn := io.BTN(0)
  c.io.select := io.SW(6)
  c.io.in0.get := 0.U
  c.io.in1 := tri_select

  tri_select := c.io.out
  switch (Cat(io.SW(5), io.SW(4))) {
    is ("b00".U) { tri_select := a.io.out }
    is ("b01".U) { tri_select := b.io.out }
    is ("b11".U) { tri_select := c.io.out }
  } 

  val display = Module(new logic101.lab._7.DispNum)
  display.io.hexs := Cat(a.io.out, b.io.out, c.io.out, 0.U(4.W))
  display.io.points := "b0000".U
  display.io.LES := "b1110".U

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
}


class Task3Top(implicit p: Parameters) extends TopModule {
  val io = IO(new TopIO)

  val tri_select = Wire(UInt(4.W))
  val a = Module(new RegControl(4, true))
  a.io.btn := io.BTN(2)
  a.io.select := io.SW(6)
  a.io.direct.get := io.SW(0)
  a.io.in1 := tri_select

  val b = Module(new RegControl(4, true))
  b.io.btn := io.BTN(1)
  b.io.select := io.SW(6)
  b.io.direct.get := io.SW(1)
  b.io.in1 := tri_select

  val alu = Module(new logic101.lab._8.MyALU(4))
  alu.io.A := a.io.out
  alu.io.B := b.io.out
  alu.io.S := Cat(io.SW(3), io.SW(2))

  val c = Module(new RegControl(4, false))
  c.io.btn := io.BTN(0)
  c.io.select := io.SW(6)
  c.io.in0.get := alu.io.C
  c.io.in1 := tri_select

  tri_select := c.io.out
  switch (Cat(io.SW(5), io.SW(4))) {
    is ("b00".U) { tri_select := a.io.out }
    is ("b01".U) { tri_select := b.io.out }
    is ("b11".U) { tri_select := c.io.out }
  } 

  val display = Module(new logic101.lab._7.DispNum)
  display.io.hexs := Cat(a.io.out, b.io.out, c.io.out, alu.io.C)
  display.io.points := "b0000".U
  display.io.LES := "b1111".U

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
}
