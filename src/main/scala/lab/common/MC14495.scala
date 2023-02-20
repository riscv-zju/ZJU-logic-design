package logic101.lab.common

import chisel3._
import chisel3.util._

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
