package logic101.fpga

import chisel3._
import chisel3.util._
import chisel3.experimental.{DataMirror,IO}

import logic101.lab._
import logic101.system.stage._
import logic101.system.config._


class NexysA7FPGAWrapper(implicit p: Parameters) extends NexysShell {

  val dut = withClockAndReset(clk, !rstn) { Module(p(TargetKey)(p)) }
  dut.io := DontCare    
  p(PinKey)(dut.io.asInstanceOf[Bundle]) foreach { case ((name, cons), port) => {
    
    val pin = IO(if (DataMirror.directionOf(port) == ActualDirection.Output) 
                   Output(chiselTypeOf(port)) 
                 else 
                   Input(chiselTypeOf(port)))
    pin.suggestName(name)
    pin <> port
    val dut_port = IOPin(pin)
    xdc.addPackagePin(dut_port, cons)
    xdc.addIOStandard(dut_port, "LVCMOS33")
    xdc.addIOB(dut_port)
  } }

  dontTouch(dut.io)
}