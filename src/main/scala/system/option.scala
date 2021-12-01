package logic101.system.stage

import firrtl.options.Shell
import java.io.{File, FileWriter}

trait Logic101Cli { this: Shell =>

  parser.note("Logic Design 101 Compile Options")
  Seq(
    TopModuleAnnotation,
    ConfigsAnnotation
  )
    .foreach(_.addOptions(parser))
}

class Logic101Options private[stage] (val topModule: Option[Class[_ <: Any]] = None,
                                      val configNames: Option[Seq[String]] = None) {
  private[stage] def copy(topModule: Option[Class[_ <: Any]] = topModule,
                          configNames: Option[Seq[String]] = configNames): Logic101Options = {
    new Logic101Options(topModule=topModule, configNames=configNames)
  }

  lazy val topPackage: Option[String] = topModule match {
    case Some(a) => Some(a.getPackage.getName)
    case _ => None
  }

  lazy val configClass: Option[String] = configNames match {
    case Some(names) =>
      val classNames = names.map{ n => n.split('.').last }
      Some(classNames.mkString("_"))
    case _ => None
  }
}


object ElaborationArtefacts {
  var files: Seq[(String, () => String)] = Nil

  def add(extension: String, contents: => String): Unit = {
    files = (extension, () => contents) +: files
  }

  def contains(extension: String): Boolean = {
    files.foldLeft(false)((t, s) => {s._1 == extension | t})
  }

  def writeOutputFile(targetDir: String, fname: String, contents: String): File = {
    val f = new File(targetDir, fname)
    val fw = new FileWriter(f)
    fw.write(contents)
    fw.close
    f
  }
}
