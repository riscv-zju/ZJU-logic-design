package logic101.lab.common

import chisel3._
import chisel3.util._

class SEGDisplay extends Module {
  val io = IO(new Bundle{
    val data = Input(UInt(64.W))
    val SEG = Output(Vec(8, Bool()))
    val AN = Output(Vec(8, Bool()))
  })

  val cnt = RegInit(0.U(3.W))
  cnt := cnt + 1.U
  io.AN := (~UIntToOH(cnt)).asTypeOf(io.AN)
  io.SEG := PriorityMux(Seq(
    UIntToOH(cnt)(0) -> io.data(7, 0),
    UIntToOH(cnt)(1) -> io.data(15, 8),
    UIntToOH(cnt)(2) -> io.data(23, 16),
    UIntToOH(cnt)(3) -> io.data(31, 24),
    UIntToOH(cnt)(4) -> io.data(39, 32),
    UIntToOH(cnt)(5) -> io.data(47, 40),
    UIntToOH(cnt)(6) -> io.data(55, 48),
    UIntToOH(cnt)(7) -> io.data(63, 56))
  ).asTypeOf(io.SEG)
}
