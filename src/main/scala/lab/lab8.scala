package logic101.lab._8

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._


class TopTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new Top()(p)
    case PinKey => (dut: TopIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(14), dut.SW(2)), (SW(15), dut.SW(3)), 
            (BTN("LEFT"), dut.BTN(1)), (BTN("RIGHT"), dut.BTN(0)),
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
      ) }
})

class Adder1b extends Module {
  val io = IO(new Bundle {
    val A = Input(UInt(1.W))
    val B = Input(UInt(1.W))
    val Ci = Input(UInt(1.W))
    val S = Output(UInt(1.W))
    val Co = Output(UInt(1.W))
  })

  io.S := io.A ^ io.B ^ io.Ci
  io.Co := io.A & io.B | io.A & io.Ci | io.B & io.Ci
}

class AddSub1b extends Module {
  val io = IO(new Bundle {
    val A = Input(UInt(1.W))
    val B = Input(UInt(1.W))
    val Ci = Input(UInt(1.W))
    val Ctrl = Input(Bool())
    val S = Output(UInt(1.W))
    val Co = Output(UInt(1.W))
  })

  val adder = Module(new Adder1b)
  adder.io.A := io.A
  adder.io.B := io.B ^ io.Ctrl.asUInt
  adder.io.Ci := io.Ci
  io.S := adder.io.S
  io.Co := adder.io.Co
}

class Adder(n: Int) extends Module {
  val io = IO(new Bundle{
    val A = Input(UInt(n.W))
    val B = Input(UInt(n.W))
    val Ctrl = Input(Bool())
    val S = Output(Vec(n, UInt(1.W)))
    val Co = Output(UInt(1.W))
  })
  val adder_chain = Seq.fill(n)(Module(new AddSub1b))
  adder_chain.foldLeft(io.Ctrl.asUInt) {
    (carry, adder) =>
      adder.io.Ci := carry
      adder.io.Co
  }
  (0 until n).foreach { i =>
    adder_chain(i).io.A := io.A(i)
    adder_chain(i).io.B := io.B(i)
    adder_chain(i).io.Ctrl := io.Ctrl
    io.S(i) := adder_chain(i).io.S
  }
  io.Co := adder_chain(n-1).io.Co
}

class MyALU(n: Int) extends Module {
  val io = IO(new Bundle{
    val A = Input(UInt(n.W))
    val B = Input(UInt(n.W))
    val S = Input(UInt(2.W))
    val C = Output(UInt(n.W))
    val Co = Output(UInt(1.W))
  })

  val addsub = Module(new Adder(n))
  addsub.io.A := io.A
  addsub.io.B := io.B
  addsub.io.Ctrl := io.S(0)

  io.C := addsub.io.S.asUInt
  io.Co := addsub.io.Co
  switch (io.S) {
    is ("b10".U) {  /* AND */
      io.C := io.A & io.B
      io.Co := 0.U
    }
    is ("b11".U) {  /* OR */
      io.C := io.A | io.B
      io.Co := 0.U
    }
  }
}

class DeBounce extends Module {
  val io = IO(new Bundle{
    val btn = Input(Bool())
    val dbtn = Output(Bool())
  })
  val pbshift = RegInit(0.U(8.W))
  pbshift := Cat(pbshift, io.btn)
  io.dbtn := pbshift.andR
}

class CreateNumber extends Module {
  val io = IO(new Bundle{
    val btn = Input(Vec(2, Bool()))
    val sw = Input(Vec(2, Bool()))
    val num = Output(UInt(8.W))
  })
  val num_a = RegInit("hA".U)
  val num_b = RegInit("hB".U)

  val add_a = Module(new Adder(4))
  add_a.io.A := num_a
  add_a.io.B := 1.U
  add_a.io.Ctrl := io.sw(1)

  val add_b = Module(new Adder(4))
  add_b.io.A := num_b
  add_b.io.B := 1.U
  add_b.io.Ctrl := io.sw(0)

  val clkdiv = RegInit(0.U(32.W))
  clkdiv := clkdiv + 1.U

  val db_btn1 = withClock(clkdiv(17).asClock) {Module(new DeBounce)}
  val db_btn0 = withClock(clkdiv(17).asClock) {Module(new DeBounce)}
  db_btn1.io.btn := io.btn(1)
  db_btn0.io.btn := io.btn(0)

  when (!RegNext(db_btn1.io.dbtn) && db_btn1.io.dbtn) {
    num_a := add_a.io.S.asUInt
  }
  when (!RegNext(db_btn0.io.dbtn) && db_btn0.io.dbtn) {
    num_b := add_b.io.S.asUInt
  }

  io.num := Cat(num_a, num_b)
}

class TopIO extends Bundle {
  val SW = Input(Vec(4, Bool()))
  val BTN = Input(Vec(2, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
}

class Top(implicit p: Parameters) extends Module {
  val io = IO(new TopIO)

  val gen_num = Module(new CreateNumber)
  gen_num.io.btn := io.BTN
  gen_num.io.sw := Cat(io.SW(1), io.SW(0)).asTypeOf(gen_num.io.sw)

  val alu = Module(new MyALU(4))
  alu.io.A := gen_num.io.num(7, 4)
  alu.io.B := gen_num.io.num(3, 0)
  alu.io.S := Cat(io.SW(3), io.SW(2))

  val display = Module(new logic101.lab._7.DispNum)
  display.io.hexs := Cat(gen_num.io.num(7, 4), gen_num.io.num(3, 0), Cat(0.U(3.W), alu.io.Co), alu.io.C)
  display.io.points := "b0000".U
  display.io.LES := "b1111".U

  io.SEG := display.io.SEG
  io.AN := display.io.AN.asTypeOf(io.AN)
}
