package logic101.system

import chisel3.stage.{ChiselCli, ChiselStage}
import firrtl.AnnotationSeq
import firrtl.options.PhaseManager.PhaseDependency
import firrtl.options.{Dependency, Phase, PhaseManager, PreservesAll, Shell, Stage, StageMain}
import firrtl.stage.FirrtlCli
import logic101.system.stage._

package object stage {
    import firrtl.AnnotationSeq
    import firrtl.options.OptionsView
    
    implicit object Logic101OptionsView extends OptionsView[Logic101Options] {
        def view(annotations: AnnotationSeq): Logic101Options = annotations
        .collect { case a: Logic101Option => a }
        .foldLeft(new Logic101Options()){ (c, x) =>
            x match {
            case TopModuleAnnotation(a)         => c.copy(topModule = Some(a))
            case ConfigsAnnotation(a)           => c.copy(configNames = Some(a))
            }
        }
  }
}

class Logic101Stage extends Stage {

  override val shell = new Shell("logic101") with Logic101Cli with ChiselCli with FirrtlCli
  val targets: Seq[PhaseDependency] = Seq(
    Dependency[logic101.system.stage.phases.Checks],
    Dependency[logic101.system.stage.phases.PreElaboration],
    Dependency[chisel3.stage.phases.Checks],
    Dependency[chisel3.stage.phases.Elaborate],
    Dependency[chisel3.stage.phases.AddImplicitOutputFile],
    Dependency[chisel3.stage.phases.AddImplicitOutputAnnotationFile],
    Dependency[chisel3.stage.phases.MaybeAspectPhase],
    Dependency[chisel3.stage.phases.Convert],
    Dependency[firrtl.stage.phases.Compiler],
    Dependency[logic101.system.stage.phases.GenerateArtefacts]
  )

  private val pm = new PhaseManager(targets)

  override def run(annotations: AnnotationSeq): AnnotationSeq = pm.transform(annotations)

}

object Generator extends StageMain(new Logic101Stage)