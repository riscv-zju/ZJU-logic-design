package logic101.lab.sys1._2

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.lab.common._
import logic101.system.stage._
import logic101.system.config._

class AdderTestTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new AdderTop()(p)
    case PinKey => (dut: AdderTopIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
            (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)), 
            (SW(8), dut.SW(8)), (SW(9), dut.SW(9)), (SW(10), dut.SW(10)), (SW(11), dut.SW(11)), (SW(14), dut.SW(14)), (SW(15), dut.SW(15)), 
            (BTN("LEFT"), dut.BTN(1)), (BTN("RIGHT"), dut.BTN(0)), 
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
            (AN(4), dut.AN(4)), (AN(5), dut.AN(5)), (AN(6), dut.AN(6)), (AN(7), dut.AN(7)),
            (LED(0), dut.LED),
      ) }
})

class CreateNumber extends Module {
  val io = IO(new Bundle{
    val btn = Input(Vec(2, Bool()))
    val sw = Input(Vec(2, Bool()))
    val num = Output(UInt(64.W))
  })
  val num_a = RegInit(0.U(32.W))
  val num_b = RegInit(0.U(32.W))

  val add_a = Module(new Adder_32())
  add_a.io.a := num_a
  add_a.io.b := 1.U
  add_a.io.a_invert := 0.U
  add_a.io.b_invert := io.sw(1)
  add_a.io.carry_in := io.sw(1) 

  val add_b = Module(new Adder_32())
  add_b.io.a := num_b
  add_b.io.b := 1.U
  add_b.io.a_invert := 0.U
  add_b.io.b_invert := io.sw(0)
  add_b.io.carry_in := io.sw(0) 

  val clkdiv = RegInit(0.U(32.W))
  clkdiv := clkdiv + 1.U

  val db_btn1 = withClock(clkdiv(17).asClock) {Module(new DeBounce)}
  val db_btn0 = withClock(clkdiv(17).asClock) {Module(new DeBounce)}
  db_btn1.io.btn := io.btn(1)
  db_btn0.io.btn := io.btn(0)

  when (!RegNext(db_btn1.io.dbtn) && db_btn1.io.dbtn) {
    num_a := add_a.io.sum.asUInt
  }
  when (!RegNext(db_btn0.io.dbtn) && db_btn0.io.dbtn) {
    num_b := add_b.io.sum.asUInt
  }

  io.num := Cat(num_a, num_b)
}

class DispNum_32 extends Module {
  val io = IO(new Bundle{
    val hexs = Input(UInt(32.W))
    val points = Input(UInt(8.W))
    val LES = Input(UInt(8.W))
    val SEG = Output(Vec(8, Bool()))
    val AN = Output(UInt(8.W))
  })
  val decoder = Module(new SegDecoder)
  val clkdiv = RegInit(0.U(32.W))
  clkdiv := clkdiv + 1.U

  io.AN := "b11111111".U
  decoder.io.data := 0.U
  decoder.io.LE := false.B
  decoder.io.point := true.B
  switch (clkdiv(19, 17)) {
    is ("b000".U) { 
      io.AN := "b11111110".U
      decoder.io.data := io.hexs(3, 0)
      decoder.io.LE := io.LES(0)
      decoder.io.point := io.points(0)
    }
    is ("b001".U) {
      io.AN := "b11111101".U
      decoder.io.data := io.hexs(7, 4)
      decoder.io.LE := io.LES(1)
      decoder.io.point := io.points(1)
    }
    is ("b010".U) {
      io.AN := "b11111011".U
      decoder.io.data := io.hexs(11, 8)
      decoder.io.LE := io.LES(2)
      decoder.io.point := io.points(2)
    }
    is ("b011".U) { 
      io.AN := "b11110111".U
      decoder.io.data := io.hexs(15, 12)
      decoder.io.LE := io.LES(3)
      decoder.io.point := io.points(3)
    }
    is ("b100".U) { 
      io.AN := "b11101111".U
      decoder.io.data := io.hexs(19, 16)
      decoder.io.LE := io.LES(4)
      decoder.io.point := io.points(4)
    }
    is ("b101".U) { 
      io.AN := "b11011111".U
      decoder.io.data := io.hexs(23, 20)
      decoder.io.LE := io.LES(5)
      decoder.io.point := io.points(5)
    }
    is ("b110".U) { 
      io.AN := "b10111111".U
      decoder.io.data := io.hexs(27, 24)
      decoder.io.LE := io.LES(6)
      decoder.io.point := io.points(6)
    }
    is ("b111".U) { 
      io.AN := "b01111111".U
      decoder.io.data := io.hexs(31, 28)
      decoder.io.LE := io.LES(7)
      decoder.io.point := io.points(7)
    }
  }

  io.SEG := (Cat(decoder.io.a, decoder.io.b, decoder.io.c, decoder.io.d, decoder.io.e, decoder.io.f, decoder.io.g, decoder.io.p)).asTypeOf(io.SEG)
}

class SegDecoder extends BlackBox {
  val io = IO(new Bundle {
    val data = Input(UInt(4.W))
    val point = Input(Bool())
    val LE = Input(Bool())

    val a = Output(Bool())
    val b = Output(Bool())
    val c = Output(Bool())
    val d = Output(Bool())
    val e = Output(Bool())
    val f = Output(Bool())
    val g = Output(Bool())
    val p = Output(Bool())
  })
}

class Adder_32 extends BlackBox {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val a_invert = Input(Bool())
    val b_invert = Input(Bool())
    val carry_in = Input(Bool())

    val carry_out = Output(Bool())
    val sum = Output(UInt(32.W))
  })
}

class AdderTopIO extends Bundle {
  val SW = Input(Vec(16, Bool()))
  val BTN = Input(Vec(2, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(8, Bool()))
  val LED = Output(Bool())
}

class AdderTop(implicit p: Parameters) extends TopModule {
  val io = IO(new AdderTopIO)

  val gen_num = Module(new CreateNumber)
  gen_num.io.btn := io.BTN
  gen_num.io.sw := Cat(io.SW(15), io.SW(14)).asTypeOf(gen_num.io.sw)

  val adder = Module(new Adder_32())
  adder.io.a := gen_num.io.num(63, 32)
  adder.io.b := gen_num.io.num(31, 0)
  adder.io.a_invert := 0.U
  adder.io.b_invert := io.SW(2)
  adder.io.carry_in := io.SW(2)

  val display = Module(new DispNum_32)
  val num = Cat(io.SW(1), io.SW(0))
  display.io.hexs := "h00000000".U
  display.io.points := "b11111111".U
  display.io.LES := "b00000000".U
  switch (num)  {
    is ("b00".U) {
      display.io.hexs := adder.io.a.asTypeOf(display.io.hexs)
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
    is ("b01".U) {
      display.io.hexs := adder.io.b.asTypeOf(display.io.hexs)
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
    is ("b10".U) {
      display.io.hexs := adder.io.sum.asTypeOf(display.io.hexs)
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
    is ("b11".U) {
      display.io.hexs := "h00000000".U
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
  }
  

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
  io.LED := adder.io.carry_out ^ adder.io.carry_in
}

class MultiplierTestTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new MultiplierTop()(p)
    case PinKey => (dut: MultiplierTopIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
            (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)), 
            (SW(8), dut.SW(8)), (SW(9), dut.SW(9)), (SW(10), dut.SW(10)), (SW(11), dut.SW(11)), (SW(14), dut.SW(14)), (SW(15), dut.SW(15)), 
            (BTN("UP"), dut.BTN(2)), (BTN("LEFT"), dut.BTN(1)), (BTN("RIGHT"), dut.BTN(0)),
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
            (AN(4), dut.AN(4)), (AN(5), dut.AN(5)), (AN(6), dut.AN(6)), (AN(7), dut.AN(7)),
            (LED(0), dut.LED),
      ) }
})

class Multiplier_32 extends BlackBox {
  val io = IO(new Bundle {
    val multiplicand = Input(UInt(32.W))
    val multiplier = Input(UInt(32.W))
    val clk = Input(Clock())
    val rst = Input(Bool())
    val start = Input(Bool())

    val finish = Output(Bool())
    val product = Output(UInt(64.W))
  })
}

class MultiplierTopIO extends Bundle {
  val SW = Input(Vec(16, Bool()))
  val BTN = Input(Vec(3, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(8, Bool()))
  val LED = Output(Bool())
}

class MultiplierTop(implicit p: Parameters) extends TopModule {
  val io = IO(new MultiplierTopIO)

  val gen_num = Module(new CreateNumber)
  gen_num.io.btn := Cat(io.BTN(1), io.BTN(0)).asTypeOf(gen_num.io.btn)
  gen_num.io.sw := Cat(io.SW(15), io.SW(14)).asTypeOf(gen_num.io.sw)

  val multiplier = Module(new Multiplier_32())
  multiplier.io.multiplicand := gen_num.io.num(63, 32)
  multiplier.io.multiplier := gen_num.io.num(31, 0)
  multiplier.io.clk := clock
  multiplier.io.rst := reset

  val clkdiv = RegInit(0.U(32.W))
  clkdiv := clkdiv + 1.U

  val db_btn2 = withClock(clkdiv(17).asClock) {Module(new DeBounce)}
  db_btn2.io.btn := io.BTN(2)
  multiplier.io.start := db_btn2.io.dbtn

  val display = Module(new DispNum_32)
  val num = Cat(io.SW(1), io.SW(0))
  display.io.hexs := "h00000000".U
  display.io.points := "b11111111".U
  display.io.LES := "b00000000".U
  switch (num)  {
    is ("b00".U) {
      display.io.hexs := multiplier.io.multiplicand.asTypeOf(display.io.hexs)
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
    is ("b01".U) {
      display.io.hexs := multiplier.io.multiplier.asTypeOf(display.io.hexs)
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
    is ("b10".U) {
      display.io.hexs := multiplier.io.product(31,0).asTypeOf(display.io.hexs)
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
    is ("b11".U) {
      display.io.hexs := multiplier.io.product(63,32).asTypeOf(display.io.hexs)
      display.io.points := "b11111111".U
      display.io.LES := "b00000000".U
    }
  }

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
  io.LED := multiplier.io.finish
}