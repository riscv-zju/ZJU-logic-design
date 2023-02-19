package logic101.lab._6

import chisel3._
import chisel3.util._
import logic101.lab._
import logic101.fpga._
import logic101.system.stage._
import logic101.system.config._


class DecodeTestTarget extends Config((site, here, up) => {
    case TargetKey => (p: Parameters) => new DispNumber()(p)
    case PinKey => (dut: DispNumberIO) => 
      { import logic101.fpga.Nexys4Pin._
        Seq((SW(0), dut.SW(0)), (SW(1), dut.SW(1)), (SW(2), dut.SW(2)), (SW(3), dut.SW(3)), 
            (SW(4), dut.SW(4)), (SW(5), dut.SW(5)), (SW(6), dut.SW(6)), (SW(7), dut.SW(7)), 
            (SW(14), dut.SW(8)), (SW(15), dut.SW(9)), 
            (SEG("P"), dut.SEG(0)), (SEG("G"), dut.SEG(1)), (SEG("F"), dut.SEG(2)), (SEG("E"), dut.SEG(3)), 
            (SEG("D"), dut.SEG(4)), (SEG("C"), dut.SEG(5)), (SEG("B"), dut.SEG(6)), (SEG("A"), dut.SEG(7)), 
            (AN(0), dut.AN(0)), (AN(1), dut.AN(1)), (AN(2), dut.AN(2)), (AN(3), dut.AN(3)), 
      ) }
})

class Cathode extends Bundle {
    val a = Output(Bool())
    val b = Output(Bool())
    val c = Output(Bool())
    val d = Output(Bool())
    val e = Output(Bool())
    val f = Output(Bool())
    val g = Output(Bool())
    val p = Output(Bool())
}

class MC14495 extends Module {
  val io = IO(new Bundle {
    val data = Input(UInt(4.W))
    val point = Input(Bool())
    val LE = Input(Bool())
    val cathode = Output(new Cathode)
  })
    val Y = false.B
    val N = true.B

    val decode =
      ListLookup(io.data,    List(N,  N,  N,  N,  N,  N,  N),
        Array(                 /* a   b   c   d   e   f   g */
          BitPat("b0000") -> List(Y,  Y,  Y,  Y,  Y,  Y,  N),
          BitPat("b0001") -> List(N,  Y,  Y,  N,  N,  N,  N),
          BitPat("b0010") -> List(Y,  Y,  N,  Y,  Y,  N,  Y),
          BitPat("b0011") -> List(Y,  Y,  Y,  Y,  N,  N,  Y),
          BitPat("b0100") -> List(N,  Y,  Y,  N,  N,  Y,  Y),
          BitPat("b0101") -> List(Y,  N,  Y,  Y,  N,  Y,  Y),
          BitPat("b0110") -> List(Y,  N,  Y,  Y,  Y,  Y,  Y),
          BitPat("b0111") -> List(Y,  Y,  Y,  N,  N,  N,  N),
          BitPat("b1000") -> List(Y,  Y,  Y,  Y,  Y,  Y,  Y),
          BitPat("b1001") -> List(Y,  Y,  Y,  Y,  N,  Y,  Y),
          BitPat("b1010") -> List(Y,  Y,  Y,  N,  Y,  Y,  Y),
          BitPat("b1011") -> List(N,  N,  Y,  Y,  Y,  Y,  Y),
          BitPat("b1100") -> List(Y,  N,  N,  Y,  Y,  Y,  N),
          BitPat("b1101") -> List(N,  Y,  Y,  Y,  Y,  N,  Y),
          BitPat("b1110") -> List(Y,  N,  N,  Y,  Y,  Y,  Y),
          BitPat("b1111") -> List(Y,  N,  N,  N,  Y,  Y,  Y),
        ))
    
    val a :: b :: c :: d :: e :: f :: g :: Nil = decode
    io.cathode.a := Mux(io.LE, a, N)
    io.cathode.b := Mux(io.LE, b, N)
    io.cathode.c := Mux(io.LE, c, N)
    io.cathode.d := Mux(io.LE, d, N)
    io.cathode.e := Mux(io.LE, e, N)
    io.cathode.f := Mux(io.LE, f, N)
    io.cathode.g := Mux(io.LE, g, N)
    io.cathode.p := Mux(io.LE, ~io.point, N)
}

class DispNumberIO extends Bundle {
  val SW = Input(Vec(10, Bool()))
  val SEG = Output(Vec(8, Bool()))
  val AN = Output(Vec(4, Bool()))
}

class DispNumber(implicit p: Parameters) extends TopModule {
  val io = IO(new DispNumberIO)

  val my_mc14495 = Module(new MC14495)
  my_mc14495.io.data := Cat(io.SW(3), io.SW(2), io.SW(1), io.SW(0))  
  io.AN := (~Cat(io.SW(7), io.SW(6), io.SW(5), io.SW(4))).asTypeOf(io.AN)
  my_mc14495.io.point := io.SW(8)
  my_mc14495.io.LE := io.SW(9)

  io.SEG := my_mc14495.io.cathode.asTypeOf(io.SEG)
}
