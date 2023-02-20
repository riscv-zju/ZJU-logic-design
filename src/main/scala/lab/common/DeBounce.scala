package logic101.lab.common

import chisel3._
import chisel3.util._

class DeBounce extends Module {
  val io = IO(new Bundle{
    val btn = Input(Bool())
    val dbtn = Output(Bool())
  })
  val pbshift = RegInit(0.U(8.W))
  pbshift := Cat(pbshift, io.btn)
  io.dbtn := pbshift.andR
}
