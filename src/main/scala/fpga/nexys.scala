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

object Nexys4Pin {
  val SW = List (
    ("SW0", "J15"), ("SW1", "L16"), ("SW2", "M13"), ("SW3", "R15"), 
    ("SW4", "R17"), ("SW5", "T18"), ("SW6", "U18"), ("SW7", "R13"), 
    ("SW8", "T8"),  ("SW9", "U8"),  ("SW10","R16"), ("SW11","T13"), 
    ("SW12","H6"),  ("SW13","U12"), ("SW14","U11"), ("SW15","V10"), 
  )
  val LED = List (
    ("LD0", "H17"), ("LD1", "K15"), ("LD2", "J13"), ("LD3", "N14"), 
    ("LD4", "R18"), ("LD5", "V17"), ("LD6", "U17"), ("LD7", "U16"), 
    ("LD8", "V16"), ("LD9", "T15"), ("LD10","U14"), ("LD11","T16"), 
    ("LD12","V15"), ("LD13","V14"), ("LD14","V12"), ("LD15","V11"), 
  )

  val Seg = Map(
    "A" -> ("CA", "T10"), "B" -> ("CB", "R10"), "C" -> ("CC", "K16"), "D" -> ("CD", "K13"),
    "E" -> ("CE", "P15"), "F" -> ("CF", "T11"), "G" -> ("CG", "L18"), "P" -> ("DP", "H15"),
  )

  val AN = List (
    ("AN0", "J17"), ("AN1", "J18"), ("AN2", "T9"),  ("AN3", "J14"),
    ("AN4", "P14"), ("AN5", "T14"), ("AN6", "K2"),  ("AN7", "U13"),
  )

}

