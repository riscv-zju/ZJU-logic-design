package logic101.lab.digit._7

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.lab.common._
import logic101.system.stage._
import logic101.system.config._


class TopTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new Top()(p)
    case PinKey => (dut: TopIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
            (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)), 
            (BTN("LEFT"), dut.BTN(3)),  (BTN("UP"), dut.BTN(2)), 
            (BTN("RIGHT"), dut.BTN(1)), (BTN("DOWN"), dut.BTN(0)),
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
      ) }
})

class TopIO extends Bundle {
  val SW = Input(Vec(8, Bool()))
  val BTN = Input(Vec(4, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
}

class Top(implicit p: Parameters) extends TopModule {
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
