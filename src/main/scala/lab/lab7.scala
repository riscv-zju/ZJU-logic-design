package logic101.lab._7

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._


class TopTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new Top()(p)
    case PinKey => (dut: TopIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
            (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)), 
            (BTN("LEFT"), dut.BTN(0)),  (BTN("UP"), dut.BTN(1)), 
            (BTN("RIGHT"), dut.BTN(2)), (BTN("DOWN"), dut.BTN(3)),
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
      ) }
})

class DispNum extends Module {
  val io = IO(new Bundle{
    val hexs = Input(UInt(16.W))
    val points = Input(UInt(4.W))
    val LES = Input(UInt(4.W))
    val SEG = Output(Vec(8, Bool()))
    val AN = Output(UInt(4.W))
  })
  val my_mc14495 = Module(new logic101.lab._6.MC14495)
  val clkdiv = RegInit(0.U(32.W))
  clkdiv := clkdiv + 1.U

  io.AN := "b1111".U
  my_mc14495.io.data := 0.U
  my_mc14495.io.LE := false.B
  my_mc14495.io.point := true.B
  switch (clkdiv(26, 25)) {
    is ("b00".U) { 
      io.AN := "b1110".U
      my_mc14495.io.data := io.hexs(3, 0)
      my_mc14495.io.LE := io.LES(0)
      my_mc14495.io.point := io.points(0)
    }
    is ("b01".U) {
      io.AN := "b1101".U
      my_mc14495.io.data := io.hexs(7, 4)
      my_mc14495.io.LE := io.LES(1)
      my_mc14495.io.point := io.points(1)
    }
    is ("b10".U) {
      io.AN := "b1011".U
      my_mc14495.io.data := io.hexs(11, 8)
      my_mc14495.io.LE := io.LES(2)
      my_mc14495.io.point := io.points(2)
    }
    is ("b11".U) { 
      io.AN := "b0111".U
      my_mc14495.io.data := io.hexs(15, 12)
      my_mc14495.io.LE := io.LES(3)
      my_mc14495.io.point := io.points(3)
    }
  }

  io.SEG := my_mc14495.io.cathode.asTypeOf(io.SEG)
}

class TopIO extends Bundle {
  val SW = Input(Vec(8, Bool()))
  val BTN = Input(Vec(4, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
}

class Top(implicit p: Parameters) extends Module {
  val io = IO(new TopIO)

  val a = io.BTN(3)
  val b = io.BTN(2)
  val c = io.BTN(1)
  val d = io.BTN(0)

  val num_a = RegInit("hA".U)
  val num_b = RegInit("hB".U)
  val num_c = RegInit("hC".U)
  val num_d = RegInit("hD".U)

  when (!RegNext(a) && a) {
    num_a := num_a + 1.U
  }
  when (!RegNext(b) && b) {
    num_b := num_b + 1.U
  }
  when (!RegNext(c) && c) {
    num_c := num_c + 1.U
  }
  when (!RegNext(d) && d) {
    num_d := num_d + 1.U
  }

  val d0 = Module(new DispNum)
  d0.io.hexs := Cat(num_a, num_b, num_c, num_d)
  d0.io.points := Cat(io.SW(3), io.SW(2), io.SW(1), io.SW(0)) 
  d0.io.LES := Cat(io.SW(7), io.SW(6), io.SW(5), io.SW(4)) 

  io.SEG := d0.io.SEG
  io.AN := d0.io.AN.asTypeOf(io.AN)
}
