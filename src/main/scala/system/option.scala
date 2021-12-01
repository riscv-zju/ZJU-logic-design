package logic.system.stage

import firrtl.options.Shell


trait Logic101Cli { this: Shell =>

  parser.note("Logic Design 101 Compile Options")
  Seq(
    TopModuleAnnotation,
    ConfigsAnnotation,
    OutputBaseNameAnnotation
  )
    .foreach(_.addOptions(parser))
}

class Logic101Options private[stage] (val topModule: Option[Class[_ <: Any]] = None,
                                      val configNames: Option[Seq[String]] = None,
                                      val outputBaseName: Option[String] = None) {
  private[stage] def copy(topModule: Option[Class[_ <: Any]] = topModule,
                          configNames: Option[Seq[String]] = configNames,
                          outputBaseName: Option[String] = outputBaseName): Logic101Options = {
    new Logic101Options(topModule=topModule, configNames=configNames, outputBaseName=outputBaseName)
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

  lazy val longName: Option[String] = outputBaseName match {
    case Some(name) => Some(name)
    case _ =>
      if (!topPackage.isEmpty) Some(s"${topPackage.get}") else None
  }
}
