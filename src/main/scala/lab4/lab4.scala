package logic.system.lab4

import chisel3._
import chisel3.util._
import logic.fpga._
import logic.system.stage._
import logic.system.config._
import scala.reflect.internal.Mode

// case object TargetKey extends Field[String]()
case object TargetKey extends Field[Parameters => Module]()
case object PinKey extends Field[Bundle => Seq[Tuple2[String, Element]]]()

class LampCtrlTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new LampCtrl()(p) //"LampCtrl"
  case PinKey => (dut: LampCtrlIO) => Seq(("J15", dut.S1), ("L16", dut.S2), ("M13", dut.S3), ("H17", dut.F))
})

class LampCtrlDelayTarget extends Config(
  new LampCtrlTarget().alter((site,here,up) => {
    case TargetKey => (p: Parameters) => new LampCtrlDelay()(p)
  })
)

/** 实验目的
  * 问题1：某三层楼房的楼梯通道共用一盏灯，每层楼都安装了一只开关并能独立控制该灯，请设计楼道灯的控制电路。
  * 问题2：增加控制要求，灯打开后，延时若干秒自动关闭，请重新设计楼道灯的控制电路。 
  */

class LampCtrlIO extends Bundle {
    val S1 = Input(Bool())
    val S2 = Input(Bool())
    val S3 = Input(Bool())
    val F  = Output(Bool())
}

class LampCtrl(implicit p: Parameters) extends Module {
  /* F = S1~S2~S3 + ~S1S2~S3 + ~S1~S2S3 + S1S2S3 */
  val io = IO(new LampCtrlIO)

  io.F :=  io.S1 && !io.S2 && !io.S3 || 
          !io.S1 &&  io.S2 && !io.S3 || 
          !io.S1 && !io.S2 &&  io.S3 || 
           io.S1 &&  io.S2 &&  io.S3
}


class LampCtrlDelay(implicit p: Parameters) extends Module {
  /* F = S1~S2~S3 + ~S1S2~S3 + ~S1~S2S3 + S1S2S3 */
  val io = IO(new LampCtrlIO)
  val w = Wire(Bool())
  val max = "hFFFFFFF".U
  val count = RegInit(max)
  
  
  w :=  io.S1 && !io.S2 && !io.S3 || 
       !io.S1 &&  io.S2 && !io.S3 || 
       !io.S1 && !io.S2 &&  io.S3 || 
        io.S1 &&  io.S2 &&  io.S3

  when (w) {
    count := 0.U
  } .elsewhen (count < max) {
    count := count + 1.U
  }

  io.F := Mux(count < max, true.B, false.B)
}
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