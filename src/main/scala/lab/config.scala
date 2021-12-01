package logic101.lab

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._

// case object TargetKey extends Field[String]()
case object TargetKey extends Field[Parameters => Module]()
case object PinKey extends Field[Bundle => Seq[Tuple2[String, Element]]]()


