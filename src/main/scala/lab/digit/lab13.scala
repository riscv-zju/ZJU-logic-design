package logic101.lab.digit._13

import chisel3._
import chisel3.util._
import logic101.fpga._
import logic101.lab.common._
import logic101.system.stage._
import logic101.system.config._

class LEDTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new LEDTop()(p)
  case PinKey => (dut: LEDTopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((SW(15), dut.SW),
          (LED(0), dut.LED(0)), (LED(1), dut.LED(1)), (LED(2), dut.LED(2)), (LED(3), dut.LED(3)), 
          (LED(4), dut.LED(4)), (LED(5), dut.LED(5)), (LED(6), dut.LED(6)), (LED(7), dut.LED(7)), 
          (LED(8), dut.LED(8)), (LED(9), dut.LED(9)), (LED(10),dut.LED(10)),(LED(11),dut.LED(11)), 
          (LED(12),dut.LED(12)),(LED(13),dut.LED(13)),(LED(14),dut.LED(14)),(LED(15),dut.LED(15)), 
          (BTN("LEFT"), dut.BTN(3)), (BTN("UP"), dut.BTN(2)), (BTN("RIGHT"), dut.BTN(1)), (BTN("DOWN"), dut.BTN(0)),
          (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
    ) }
})

class SEGTarget extends Config((site, here, up) => {
  case TargetKey => (p: Parameters) => new SEGTop()(p)
  case PinKey => (dut: SEGTopIO) => 
    { import logic101.fpga.Nexys4Pin._
      Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
          (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)),  
          (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
          (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
          (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
          (AN(4), dut.AN(4)), (AN(5), dut.AN(5)), (AN(6), dut.AN(6)), (AN(7), dut.AN(7)), 
    ) }
})

class LEDGenNum extends Module {
  val io = IO(new Bundle{
    val clk_1ms = Input(Clock())
    val ctrl = Input(Vec(4, Bool()))
    val data = Output(UInt(16.W))
  })

  val db = Seq.fill(4)(withClock(io.clk_1ms) { Module(new DeBounce) })
  db(0).io.btn := io.ctrl(0)
  db(1).io.btn := io.ctrl(1)
  db(2).io.btn := io.ctrl(2)
  db(3).io.btn := io.ctrl(3)
  val btn_d = !RegNext(db(0).io.dbtn) && db(0).io.dbtn
  val btn_c = !RegNext(db(1).io.dbtn) && db(1).io.dbtn
  val btn_b = !RegNext(db(2).io.dbtn) && db(2).io.dbtn
  val btn_a = !RegNext(db(3).io.dbtn) && db(3).io.dbtn

  val num_a = RegInit("hA".U)
  val num_b = RegInit("hB".U)
  val num_c = RegInit("hC".U)
  val num_d = RegInit("hD".U)

  when (btn_a) {
    num_a := num_a + 1.U
  }
  when (btn_b) {
    num_b := num_b + 1.U
  }
  when (btn_c) {
    num_c := num_c + 1.U
  }
  when (btn_d) {
    num_d := num_d + 1.U
  }

  io.data := Cat(num_a, num_b, num_c, num_d)
}

class S2P(n: Int) extends Module {
  val io = IO(new Bundle{
    val in = Input(UInt(1.W))
    val out = Output(UInt(n.W))
  })

  val reg = RegInit(0.U(n.W))
  reg := Cat(reg(n-2, 0), io.in)

  io.out := reg
}

class shiftReg(n: Int) extends Module {
  val io = IO(new Bundle{
    val pin = Input(UInt(n.W))
    val sin = Input(UInt(1.W))
    val SL = Input(Bool())
    val pout = Output(UInt(n.W))
    val sout = Output(UInt(1.W))
  })

  val shift = RegInit((-1).S(n.W).asUInt)
  when (io.SL) {
    shift := io.pin
  } .otherwise {
    shift := Cat(shift(n-2, 0), io.sin)
  }

  io.pout := shift
  io.sout := shift(n-1)
}

class P2S(n: Int) extends Module {
  val io = IO(new Bundle{
    val in = Input(UInt(n.W))
    val start = Input(Bool())
    val drv_clk = Output(Clock())
    val drv_do = Output(UInt(1.W))
  })

  val reg = Module(new shiftReg(n+1))
  reg.io.pin := Cat(io.in, 0.U(1.W))
  reg.io.sin := 1.U
  reg.io.SL := !io.start
  dontTouch(reg.io.sin)

  val finish = WireInit(reg.io.pout(n-1, 0).andR)  
  io.drv_do := reg.io.sout
  io.drv_clk := Mux(!finish && io.start, clock.asBool, true.B).asClock
}


class LEDTopIO extends Bundle {
  val SW = Input(Bool())
  val BTN = Input(Vec(4, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
  val LED = Output(Vec(16, Bool()))
}

class LEDTop(implicit p: Parameters) extends TopModule {
  val io = IO(new LEDTopIO)

  val div = Module(new clkdiv(50000))
  val db = withClock(div.io.clk) { Module(new DeBounce) }
  db.io.btn := io.SW

  val num = Module(new LEDGenNum)
  num.io.ctrl := io.BTN
  num.io.clk_1ms := div.io.clk

  val drv = Module(new P2S(16))
  drv.io.in := num.io.data
  drv.io.start := db.io.dbtn
  
  val fake = withClock(drv.io.drv_clk){ Module(new S2P(16)) }
  fake.io.in := ~drv.io.drv_do

  val display = Module(new DispNum)
  display.io.hexs := num.io.data
  display.io.points := "b0000".U
  display.io.LES := "b1111".U

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)

  io.LED := (~fake.io.out).asTypeOf(io.LED)
}


class SEGGenNum extends Module {
  val io = IO(new Bundle{
    val ctrl = Input(Vec(8, Bool()))
    val data = Output(UInt(64.W))
  })

  val num_reg = RegInit((VecInit(Seq.fill(8)(0.U(4.W)))))
  val seg_reg = RegInit((VecInit(Seq.fill(8)(0.U(8.W)))))
  val sw_pos = Wire(Vec(8, Bool()))
  val decode = Seq.fill(8)(Module(new MC14495))
  for (i <- 0 until 8) {
    sw_pos(i) := !RegNext(io.ctrl(i)) && io.ctrl(i)
    when (sw_pos(i)) {
      num_reg(i) := num_reg(i) + 1.U
    }
    decode(i).io.data := num_reg(i)
    decode(i).io.point := 0.U
    decode(i).io.LE := 1.U
    seg_reg(i) := decode(i).io.cathode.asUInt
  }

  io.data := seg_reg.asUInt
}

class SEGTopIO extends Bundle {
  val SW = Input(Vec(8, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(8, Bool()))
}

class SEGTop(implicit p: Parameters) extends TopModule {
  val io = IO(new SEGTopIO)

  val div = Module(new clkdiv(500000))
  val db = Seq.fill(8)(withClock(div.io.clk) { Module(new DeBounce) })
  val sw = Wire(Vec(8, Bool()))
  for (i <- 0 until 8) {
    db(i).io.btn := io.SW(i)
    sw(i) := db(i).io.dbtn
  }

  val sw_neg = Wire(Vec(8, Bool()))
  for (i <- 0 until 8) {
    sw_neg(i) := RegNext(sw(i)) && !sw(i)
  }
  val num = Module(new SEGGenNum)
  num.io.ctrl := sw

  val drv = Module(new P2S(64))
  drv.io.in := num.io.data
  drv.io.start := ~(sw.asUInt.orR)
  
  val fake = withClock(drv.io.drv_clk) { Module(new S2P(64)) }
  fake.io.in := drv.io.drv_do

  val seg_div = Module(new clkdiv(50000))
  val display = withClock(seg_div.io.clk) { Module(new SEGDisplay) }
  display.io.data := fake.io.out
  io.SEG := display.io.SEG
  io.AN := display.io.AN
}