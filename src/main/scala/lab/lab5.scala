package logic101.lab._5

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._


class DecodeTestTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new DecodeTest()(p)
    case PinKey => (dut: DecodeTestIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), 
            (SW(3), dut.SW(3)), (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), 
            (LED(0), dut.LED(0)), (LED(1), dut.LED(1)), (LED(2), dut.LED(2)), (LED(3), dut.LED(3)), 
            (LED(4), dut.LED(4)), (LED(5), dut.LED(5)), (LED(6), dut.LED(6)), (LED(7), dut.LED(7)), 
      ) }
})

class LampCtrlTarget extends Config(
  new DecodeTestTarget().alter((site,here,up) => {
    case TargetKey => (p: Parameters) => new LampCtrl()(p)
    case PinKey => (dut: DecodeTestIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), 
            (LED(0), dut.LED(0)), 
      ) }
  })
)

class DecodeTestIO extends Bundle {
  val SW = Input(Vec(6, Bool()))
  val LED = Output(Vec(8, Bool()))
}

class D_74LS138 extends Module {
  val io = IO(new Bundle {
    val G = Input(Bool())
    val G2A = Input(Bool())
    val G2B = Input(Bool())
    val in = Input(UInt(3.W))
    val out = Output(UInt(8.W))
  })

  io.out := "hFF".U
  when (io.G && !io.G2A && !io.G2B) {
    switch (io.in) {
      is ("b000".U) { io.out := "b1111_1110".U }
      is ("b001".U) { io.out := "b1111_1101".U }
      is ("b010".U) { io.out := "b1111_1011".U }
      is ("b011".U) { io.out := "b1111_0111".U }
      is ("b100".U) { io.out := "b1110_1111".U }
      is ("b101".U) { io.out := "b1101_1111".U }
      is ("b110".U) { io.out := "b1011_1111".U }
      is ("b111".U) { io.out := "b0111_1111".U }
    }
  }
}

class DecodeTest(implicit p: Parameters) extends Module {
  val io = IO(new DecodeTestIO)

  val decode = Module(new D_74LS138)
  decode.io.in := Cat(io.SW(2), io.SW(1), io.SW(0))
  decode.io.G := io.SW(3)
  decode.io.G2A := io.SW(4)
  decode.io.G2B := io.SW(5)
  io.LED := decode.io.out.asTypeOf(io.LED)
}

class LampCtrl(implicit p: Parameters) extends Module {
  /* F = S1~S2~S3 + ~S1S2~S3 + ~S1~S2S3 + S1S2S3 */
  val io = IO(new DecodeTestIO)
  io := DontCare

  val decode = Module(new D_74LS138)
  decode.io.in := Cat(io.SW(2), io.SW(1), io.SW(0))
  decode.io.G := true.B
  decode.io.G2A := false.B
  decode.io.G2B := false.B
  io.LED(0) := !(decode.io.out(1) && decode.io.out(2) && decode.io.out(4) && decode.io.out(7))
}

