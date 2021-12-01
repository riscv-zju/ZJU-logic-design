package logic.lab

import chisel3._
import chisel3.util._
import logic.fpga._
import logic.system.stage._
import logic.system.config._

// case object TargetKey extends Field[String]()
case object TargetKey extends Field[Parameters => Module]()
case object PinKey extends Field[Bundle => Seq[Tuple2[String, Element]]]()


