package logic.system.stage.phases

import firrtl.AnnotationSeq
import firrtl.options.Viewer.view
import firrtl.annotations.Annotation
import firrtl.options.{Dependency, Phase, PreservesAll, StageOptions}
import firrtl.options.{OptionsException, Phase, PreservesAll, TargetDirAnnotation}

import scala.collection.mutable
import java.io.{File, FileWriter}

import chisel3.RawModule
import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.stage.ChiselOutputFileAnnotation
import logic.system.config.{Config, Field, Parameters}
import logic.system.stage._

/** Checks for the correct type and number of command line arguments */
class Checks extends Phase {
  override def invalidates(a: Phase) = false

  override def transform(annotations: AnnotationSeq): AnnotationSeq = {
    val targetDir, topModule, configNames, outputBaseName = mutable.ListBuffer[Annotation]()

    annotations.foreach {
      case a: TargetDirAnnotation      => a +=: targetDir
      case a: TopModuleAnnotation      => a +=: topModule
      case a: ConfigsAnnotation        => a +=: configNames
      case a: OutputBaseNameAnnotation => a +=: outputBaseName
      case _ =>
    }

    def required(annoList: mutable.ListBuffer[Annotation], option: String): Unit = {
      if (annoList.size != 1) {
        throw new OptionsException(s"Exactly one $option required")
      }
    }

    def optional(annoList: mutable.ListBuffer[Annotation], option: String): Unit = {
      if (annoList.size > 1) {
        throw new OptionsException(s"Too many $option options have been specified")
      }
    }

    required(targetDir, "target directory")
    required(topModule, "top module")
    required(configNames, "configs string (','-delimited)")

    optional(outputBaseName, "output base name")

    annotations
  }
}


case object TargetDirKey extends Field[String](".")

/** Constructs a generator function that returns a top module with given config parameters */
class PreElaboration extends Phase {

  override val prerequisites = Seq(Dependency[Checks])
  override val dependents = Seq(Dependency[chisel3.stage.phases.Elaborate])
  override def invalidates(a: Phase) = false

  override def transform(annotations: AnnotationSeq): AnnotationSeq = {

    val stageOpts = view[StageOptions](annotations)
    val logicOpts = view[Logic101Options](annotations)
    val topMod = logicOpts.topModule.get

    val config = getConfig(logicOpts.configNames.get).alterPartial {
      case TargetDirKey => stageOpts.targetDir
    }

    val gen = () =>
      topMod.getDeclaredConstructor(classOf[Parameters]).newInstance(config) match {
          case a: RawModule => a
        }

    ChiselGeneratorAnnotation(gen) +: annotations
  }

  def getConfig(fullConfigClassNames: Seq[String]): Config = {
    new Config(fullConfigClassNames.foldRight(Parameters.empty) { case (currentName, config) =>
      val currentConfig = try {
        Class.forName(currentName).getConstructor().newInstance().asInstanceOf[Config]
      } catch {
        case e: java.lang.ClassNotFoundException =>
          throw new Exception(s"""Unable to find part "$currentName" from "$fullConfigClassNames", did you misspell it or specify the wrong package path?""", e)
      }
      currentConfig ++ config
    })
  }

}


class TransformAnnotations extends Phase {

  override val prerequisites = Seq(Dependency[Checks])
  override val dependents = Seq(Dependency[chisel3.stage.phases.AddImplicitOutputFile])
  override def invalidates(a: Phase) = false

  override def transform(annotations: AnnotationSeq): AnnotationSeq = {
    /** Construct output file annotation for emission */
    new ChiselOutputFileAnnotation(view[Logic101Options](annotations).longName.get) +: annotations
  }
}
