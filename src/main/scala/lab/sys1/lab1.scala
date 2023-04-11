package logic101.lab.sys1._1

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.lab.common._
import logic101.system.stage._
import logic101.system.config._

class MUXAndAdderTopTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new MUXAndAdderTop()(p)
  case PinKey => (dut: MUXAndAdderTopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
          (AN(4), dut.AN(4)), (AN(5), dut.AN(5)), (AN(6), dut.AN(6)), (AN(7), dut.AN(7)), 
          (SW(0), dut.I0), (SW(1), dut.I1), (SW(2), dut.I2), (SW(3), dut.I3), (SW(4), dut.I4), 
          (LED(0), dut.O), (LED(1), dut.CO),                                         
    ) }
})

class MUXAndAdderTopIO extends Bundle {
  /* logic101 logo */
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(8, Bool()))

  /* lab related */
  val I0 = Input(Bool())
  val I1 = Input(Bool())
  val I2 = Input(Bool())
  val I3 = Input(Bool())
  val I4 = Input(Bool())
  val O = Output(Bool())
  val CO = Output(Bool())
}

class MUX4T1_1 extends BlackBox {
  val io = IO(new Bundle {
    val I0 = Input(Bool())
    val I1 = Input(Bool())
    val I2 = Input(Bool())
    val I3 = Input(Bool())
    val S = Input(UInt(2.W))
    val O = Output(Bool())
  })
}

class Adder_1 extends BlackBox {
  val io = IO(new Bundle {
    val A = Input(Bool())
    val B = Input(Bool())
    val CI = Input(Bool())
    val S = Output(Bool())
    val CO = Output(Bool())
  })
}

class MUXAndAdderTop(implicit p: Parameters) extends TopModule {
  val io = IO(new MUXAndAdderTopIO)

  val seg_div = Module(new clkdiv(50000))
  val display = withClock(seg_div.io.clk) { Module(new SEGDisplay) }
  display.io.data := "b11100011_00000011_01000001_10011111_01100011_10011111_00000011_10011111".U
  io.SEG := display.io.SEG
  io.AN := display.io.AN

  val mux = Module(new MUX4T1_1)
  val add = Module(new Adder_1)

  mux.io.S := Cat(io.I1, io.I0)

  add.io.A := io.I2
  add.io.B := io.I3
  add.io.CI := io.I4

  mux.io.I0 := add.io.A
  mux.io.I1 := add.io.B
  mux.io.I2 := add.io.CI
  mux.io.I3 := add.io.S

  io.O := mux.io.O
  io.CO := add.io.CO
}

class DecodeTestTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new DispNumber()(p)
    case PinKey => (dut: DispNumberIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
            (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)), 
            (SW(8), dut.SW(8)), (SW(9), dut.SW(9)), (SW(10), dut.SW(10)), (SW(11), dut.SW(11)), 
            (SW(14), dut.SW(14)), (SW(15), dut.SW(15)), 
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
            (AN(4), dut.AN(4)), (AN(5), dut.AN(5)), (AN(6), dut.AN(6)), (AN(7), dut.AN(7)),
      ) }
})

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

class DispNumberIO extends Bundle {
  val SW = Input(Vec(16, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(8, Bool()))
}

class DispNumber(implicit p: Parameters) extends TopModule {
  val io = IO(new DispNumberIO)

  val decoder = Module(new SegDecoder)
  decoder.io.data := Cat(io.SW(3), io.SW(2), io.SW(1), io.SW(0))  
  io.AN := (~Cat(io.SW(11), io.SW(10), io.SW(9), io.SW(8),io.SW(7), io.SW(6), io.SW(5), io.SW(4))).asTypeOf(io.AN)
  decoder.io.point := io.SW(14)
  decoder.io.LE := io.SW(15)

  io.SEG := (Cat(decoder.io.a, decoder.io.b, decoder.io.c, decoder.io.d, decoder.io.e, decoder.io.f, decoder.io.g, decoder.io.p)).asTypeOf(io.SEG)
}