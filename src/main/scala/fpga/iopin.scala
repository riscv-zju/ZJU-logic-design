package logic101.fpga

import chisel3._
import chisel3.experimental.{DataMirror, IO}

import logic101.system.config._

case class IOPin(element: Element, index: Int = 0) {
  private val width = DataMirror.widthOf(element)
  require (width.known)
  require (index >= 0 && index < width.get)

  def name = {
    //replace all [#]'s with _# in the pin base name, then append on the final [#] (pindex) if width > 1
    val pin = element.instanceName.split("\\.").map(_.replaceAll("""\[(\d+)\]""", "_$1")).mkString("_")
    pin
    // println(pin)
    // val path = element.parentPathName.split("\\.")
    // println(path.mkString("_"))
    // val pindex = pin + (if (width.get > 1) s"[${index}]" else "")
    // (path.drop(1) :+ pindex).mkString("/")
  }

  def sdcPin = {
    val path = name
    if (path.contains("/")) s"[get_pins {${path}}]" else s"[get_ports {${path}}]"
  }

  def sdcClock = s"[get_clocks -of_objects ${sdcPin}]"

  def isOutput = {
    import chisel3.ActualDirection._
    DataMirror.directionOf(element) match {
      case Output => true
      case Input => false
      case Bidirectional(_) => true
      // case Unspecified => { require(false); false }
      case _ => false
    }
  }

  def isInput = {
    import chisel3.ActualDirection._
    DataMirror.directionOf(element) match {
      case Output => false
      case Input => true
      case Bidirectional(_) => true
      // case Unspecified => { require(false); false }
      case _ => false
    }
  }
}