package logic.lab._4

import chisel3._
import chisel3.util._
import logic.lab._
import logic.fpga._
import logic.system.stage._
import logic.system.config._

class LampCtrlTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new LampCtrl()(p) //"LampCtrl"
  case PinKey => (dut: LampCtrlIO) => Seq(("J15", dut.S1), ("L16", dut.S2), ("M13", dut.S3), ("H17", dut.F))
})

class LampCtrlDelayTarget extends Config(
  new LampCtrlTarget().alter((site,here,up) => {
    case TargetKey => (p: Parameters) => new LampCtrlDelay()(p)
  })
)

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