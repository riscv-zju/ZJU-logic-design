package logic101.fpga

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._

trait TopModule extends Module {
  val io : Bundle
}

case object TargetKey extends Field[Parameters => TopModule]()
case object PinKey extends Field[Bundle => Seq[Tuple2[Tuple2[String, String], Element]]]()
