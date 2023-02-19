package logic101.lab._0

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._

import scala.math.pow
import org.json4s.scalap.scalasig.PolyTypeWithCons


class EmptyTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new Top()(p)
  case PinKey => (dut: TopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
          (AN(4), dut.AN(4)), (AN(5), dut.AN(5)), (AN(6), dut.AN(6)), (AN(7), dut.AN(7)), 
    ) }
})

class TopIO extends Bundle {
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(8, Bool()))
}

class Top(implicit p: Parameters) extends TopModule {
  val io = IO(new TopIO)

  val seg_div = Module(new logic101.lab._11.clkdiv(50000))
  val display = withClock(seg_div.io.clk) { Module(new logic101.lab._13.SEGDisplay) }
  display.io.data := "b11100011_00000011_01000001_10011111_01100011_10011111_00000011_10011111".U
  io.SEG := display.io.SEG
  io.AN := display.io.AN
}