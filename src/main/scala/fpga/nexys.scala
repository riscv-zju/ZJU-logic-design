package logic101.fpga

import chisel3._
import logic101.system.stage._
import logic101.system.config._

class Nexys4Shell(implicit p: Parameters) extends BasicShell {

  val clk = IO(Input(Clock()))
  val rstn = IO(Input(Bool()))

  ElaborationArtefacts.add("clock.xdc",
    """## Clock and Reset
      |set_property -dict { PACKAGE_PIN E3    IOSTANDARD LVCMOS33 } [get_ports { clk }];
      |create_clock -add -name sys_clk_pin -period 10.00 -waveform {0 5} [get_ports { clk }];
      |set_property -dict { PACKAGE_PIN C12   IOSTANDARD LVCMOS33 } [get_ports { rstn }];
      |""".stripMargin)
}


