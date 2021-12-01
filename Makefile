TOP			:= $(CURDIR)
BUILD		:= $(TOP)/build
SCRIPT		:= $(TOP)/scripts
CHISEL_OUT	:= $(BUILD)/chisel
VIVADO_OUT	:= $(BUILD)/vivado

LAB 		?= 4
PACKAGE		?= logic.system.lab$(LAB)
CONFIG		?= LampCtrlDelayTarget
TOP_MODULE	?= FPGAWrapper


all: compile sythesis

compile_help:
	sbt "runMain logic.system.Generator --help"

compile:
	mkdir -p $(CHISEL_OUT)/LAB$(LAB)
	sbt "runMain logic.system.Generator \
			-T $(PACKAGE).$(TOP_MODULE) \
			-C $(PACKAGE).$(CONFIG) 	\
			-td $(CHISEL_OUT)/LAB$(LAB)"

sythesis:




clean:
	rm -rf build target project/target *.v *.anno.json