package logic101.fpga

import chisel3._
import logic101.system.stage._
import logic101.system.config._


class XDC(val name: String) {
  private var constraints: Seq[() => String] = Nil
  protected def addConstraint(command: => String): Unit = { constraints = (() => command) +: constraints }
  ElaborationArtefacts.add(name, constraints.map(_()).reverse.mkString("\n") + "\n")

  def addBoardPin(io: IOPin, pin: String): Unit = {
    addConstraint(s"set_property BOARD_PIN {${pin}} ${io.sdcPin}")
  }
  def addPackagePin(io: IOPin, pin: String): Unit = {
    addConstraint(s"set_property PACKAGE_PIN {${pin}} ${io.sdcPin}")
  }
  def addIOStandard(io: IOPin, standard: String): Unit = {
    addConstraint(s"set_property IOSTANDARD {${standard}} ${io.sdcPin}")
  }
  def addPullup(io: IOPin): Unit = {
    addConstraint(s"set_property PULLUP {TRUE} ${io.sdcPin}")
  }
  def addIOB(io: IOPin): Unit = {
    if (io.isOutput) {
      addConstraint(s"set_property IOB {TRUE} [ get_cells -of_objects [ all_fanin -flat -startpoints_only ${io.sdcPin}]]")
    } else {
      addConstraint(s"set_property IOB {TRUE} [ get_cells -of_objects [ all_fanout -flat -endpoints_only ${io.sdcPin}]]")
    }
  }
  def addSlew(io: IOPin, speed: String): Unit = {
    addConstraint(s"set_property SLEW {${speed}} ${io.sdcPin}")
  }
  def addTermination(io: IOPin, kind: String): Unit = {
    addConstraint(s"set_property OFFCHIP_TERM {${kind}} ${io.sdcPin}")
  }
  def clockDedicatedRouteFalse(io: IOPin): Unit = {
    addConstraint(s"set_property CLOCK_DEDICATED_ROUTE {FALSE} [get_nets ${io.sdcPin}]")
  }
  def addDriveStrength(io: IOPin, drive: String): Unit = {
    addConstraint(s"set_property DRIVE {${drive}} ${io.sdcPin}")
  }
  def addIbufLowPower(io: IOPin, value: String): Unit = {
    addConstraint(s"set_property IBUF_LOW_PWR ${value} ${io.sdcPin}")
  }
}

abstract class BasicShell()(implicit p: Parameters) extends RawModule {
  val xdc = new XDC("shell.xdc")

  ElaborationArtefacts.add("shell.vivado.tcl",
    """set shell_vivado_tcl [file normalize [info script]]
      |set shell_vivado_idx [string last ".shell.vivado.tcl" $shell_vivado_tcl]
      |add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".shell.sdc"]
      |add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".shell.xdc"]
      |set extra_constr [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".extra.shell.xdc"]
      |if [file exist $extra_constr] {
      |  add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".extra.shell.xdc"]
      |}
      |""".stripMargin)

    //Including the extra .xdc file in this way is a bit of a hack since ElaborationArtefacts can't append, and this tcl will only read specific
    // files. The long term solution is to make an overlay that does nothing but include .xdc constraints
}

