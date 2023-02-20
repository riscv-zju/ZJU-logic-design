package logic101.lab.common

import chisel3._
import chisel3.util._

class clkdiv(n: BigInt) extends Module {
  val io = IO(new Bundle{
    val clk = Output(Clock())
  })

  val cnt = RegInit(0.U(log2Ceil(n).W))
  val out = RegInit(false.B)
  io.clk := out.asClock

  when(cnt < n.U) {
    cnt := cnt + 1.U
  } .otherwise {
    cnt := 0.U
    out := ~out
  }
}
