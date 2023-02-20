package logic101.lab.common

import chisel3._
import chisel3.util._

class DispNum extends Module {
  val io = IO(new Bundle{
    val hexs = Input(UInt(16.W))
    val points = Input(UInt(4.W))
    val LES = Input(UInt(4.W))
    val SEG = Output(Vec(8, Bool()))
    val AN = Output(UInt(4.W))
  })
  val my_mc14495 = Module(new MC14495)
  val clkdiv = RegInit(0.U(32.W))
  clkdiv := clkdiv + 1.U

  io.AN := "b1111".U
  my_mc14495.io.data := 0.U
  my_mc14495.io.LE := false.B
  my_mc14495.io.point := true.B
  switch (clkdiv(18, 17)) {
    is ("b00".U) { 
      io.AN := "b1110".U
      my_mc14495.io.data := io.hexs(3, 0)
      my_mc14495.io.LE := io.LES(0)
      my_mc14495.io.point := io.points(0)
    }
    is ("b01".U) {
      io.AN := "b1101".U
      my_mc14495.io.data := io.hexs(7, 4)
      my_mc14495.io.LE := io.LES(1)
      my_mc14495.io.point := io.points(1)
    }
    is ("b10".U) {
      io.AN := "b1011".U
      my_mc14495.io.data := io.hexs(11, 8)
      my_mc14495.io.LE := io.LES(2)
      my_mc14495.io.point := io.points(2)
    }
    is ("b11".U) { 
      io.AN := "b0111".U
      my_mc14495.io.data := io.hexs(15, 12)
      my_mc14495.io.LE := io.LES(3)
      my_mc14495.io.point := io.points(3)
    }
  }

  io.SEG := my_mc14495.io.cathode.asTypeOf(io.SEG)
}
