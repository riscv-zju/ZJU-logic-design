package logic101.lab.digit._6

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.lab.common._
import logic101.system.stage._
import logic101.system.config._

class DecodeTestTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new DispNumber()(p)
    case PinKey => (dut: DispNumberIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
            (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)), 
            (SW(14), dut.SW(8)), (SW(15), dut.SW(9)), 
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
      ) }
})

class DispNumberIO extends Bundle {
  val SW = Input(Vec(10, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
}

class DispNumber(implicit p: Parameters) extends TopModule {
  val io = IO(new DispNumberIO)

  val my_mc14495 = Module(new MC14495)
  my_mc14495.io.data := Cat(io.SW(3), io.SW(2), io.SW(1), io.SW(0))  
  io.AN := (~Cat(io.SW(7), io.SW(6), io.SW(5), io.SW(4))).asTypeOf(io.AN)
  my_mc14495.io.point := io.SW(8)
  my_mc14495.io.LE := io.SW(9)

  io.SEG := my_mc14495.io.cathode.asTypeOf(io.SEG)
}
