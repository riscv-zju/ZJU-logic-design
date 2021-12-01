package logic.lab

import chisel3._
import chisel3.util._
import logic.fpga._
import logic.system.stage._
import logic.system.config._

class FPGAWrapper(implicit p: Parameters) extends Nexys4Shell {

    val dut = withClockAndReset(clk, !rstn) { Module(p(TargetKey)(p)) }

    p(PinKey)(dut.io.asInstanceOf[Bundle]) foreach { case (cons, port) => {
      val io_pin = IOPin(port)
      xdc.addPackagePin(io_pin, cons)
      xdc.addIOStandard(io_pin, "LVCMOS33")
      xdc.addIOB(io_pin)

      val pin = IO(if (io_pin.isOutput) Output(chiselTypeOf(port)) else Input(chiselTypeOf(port)))
      pin.suggestName(io_pin.name)
      pin <> port
    } }

}