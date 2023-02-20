package logic101.lab.digit._4

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._

class LampCtrlTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new LampCtrl()(p)
  case PinKey => (dut: LampCtrlIO) => Seq((Nexys4Pin.SW(0), dut.S1), 
                                          (Nexys4Pin.SW(1), dut.S2), 
                                          (Nexys4Pin.SW(2), dut.S3), 
                                          (Nexys4Pin.LED(0), dut.F))
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

class LampCtrl(implicit p: Parameters) extends TopModule {
  /* F = S1~S2~S3 + ~S1S2~S3 + ~S1~S2S3 + S1S2S3 */
  val io = IO(new LampCtrlIO)

  io.F :=  io.S1 && !io.S2 && !io.S3 || 
          !io.S1 &&  io.S2 && !io.S3 || 
          !io.S1 && !io.S2 &&  io.S3 || 
           io.S1 &&  io.S2 &&  io.S3
}


class LampCtrlDelay(implicit p: Parameters) extends TopModule {
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
