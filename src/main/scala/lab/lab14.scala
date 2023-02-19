package logic101.lab._14

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._

import scala.math.pow
import org.json4s.scalap.scalasig.PolyTypeWithCons


class ClockTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new Top()(p)
  case PinKey => (dut: TopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
          (AN(4), dut.AN(4)), (AN(5), dut.AN(5)), (AN(6), dut.AN(6)), (AN(7), dut.AN(7)), 
    ) }
})

class My74LS161 extends RawModule {
  val io = IO(new Bundle{
    val CP = Input(Clock())
    val CRn = Input(Bool())
    val LDn = Input(Bool())
    val CTp = Input(Bool())
    val CTt = Input(Bool())
    val D = Input(UInt(4.W))
    val Q = Output(UInt(4.W))
    val CO = Output(Bool())
  })
  val CR = WireInit((!io.CRn).asAsyncReset)
  val cnt = withClockAndReset(io.CP, CR) { RegInit(0.U(4.W)) }
  when (!io.LDn) {
    cnt := io.D
  } .otherwise {
    when (io.CTp && io.CTt) {
      cnt := cnt + 1.U
    } .otherwise {
      cnt := cnt
    }
  }

  io.Q := cnt
  io.CO := io.Q.andR & io.CTt
}

class CounterN(n: Int) extends Module {
  val io = IO(new Bundle{
    val Q = Output(UInt(8.W))
    val C = Output(Clock())
  })

  val low_reset = Wire(Bool())
  val low = Module(new My74LS161)
  low.io.CP := clock
  low.io.CRn := !(reset.asBool)
  low.io.LDn := !low_reset
  low.io.CTp := true.B
  low.io.CTt := true.B
  low.io.D := 0.U

  val low_9 = Wire(Bool())
  low_9 := low.io.Q === 9.U

  val high_reset = Wire(Bool())
  val high = Module(new My74LS161)
  high.io.CP := clock
  high.io.CRn := !(reset.asBool)
  high.io.LDn := !high_reset
  high.io.CTp := low_9
  high.io.CTt := low_9
  high.io.D := 0.U

  val all_reset = Wire(Bool())
  all_reset := high.io.Q === ((n-1)/10).U && low.io.Q === ((n-1)%10).U

  low_reset := low_9 | all_reset
  high_reset := all_reset

  io.Q := Cat(high.io.Q, low.io.Q)
  io.C := (RegNext(all_reset)).asClock
}

class ClockGen extends Module {
  val io = IO(new Bundle{
    val data = Output(UInt(64.W))
  })

  val num = Wire((Vec(8, UInt(4.W))))
  val seg_reg = RegInit((VecInit(Seq.fill(8)(0.U(8.W)))))
  val decode = Seq.fill(8)(Module(new logic101.lab._6.MC14495))
  for (i <- 0 until 8) {
    num(i) := 0.U
    decode(i).io.data := num(i)
    decode(i).io.point := 0.U
    decode(i).io.LE := 1.U
    seg_reg(i) := decode(i).io.cathode.asUInt
  }

  val sec = Module(new CounterN(60))
  num(0) := sec.io.Q(3, 0)
  num(1) := sec.io.Q(7, 4)

  val min = withClock(sec.io.C) { Module(new CounterN(60)) }
  num(2) := min.io.Q(3, 0)
  num(3) := min.io.Q(7, 4)

  val hour = withClock(min.io.C) { Module(new CounterN(24)) }
  num(4) := hour.io.Q(3, 0)
  num(5) := hour.io.Q(7, 4)

  io.data := seg_reg.asUInt
}

class TopIO extends Bundle {
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(8, Bool()))
}

class Top(implicit p: Parameters) extends TopModule {
  val io = IO(new TopIO)

  val clock_clk = Module(new logic101.lab._11.clkdiv(5000000))
  val num = withClock(clock_clk.io.clk) { Module(new ClockGen) }

  val seg_div = Module(new logic101.lab._11.clkdiv(50000))
  val display = withClock(seg_div.io.clk) { Module(new logic101.lab._13.SEGDisplay) }
  display.io.data := num.io.data
  io.SEG := display.io.SEG
  io.AN := display.io.AN
}