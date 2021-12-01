TOP			:= $(CURDIR)
BUILD		:= $(TOP)/build
SCRIPT		:= $(TOP)/scripts
CHISEL_OUT	:= $(BUILD)/chisel
VIVADO_OUT	:= $(BUILD)/vivado

LAB 		?= 4
TOP_MODULE	?= FPGAWrapper
PACKAGE		?= logic.lab._$(LAB)
CONFIG		?= LampCtrlDelayTarget



all: compile sythesis

compile_help:
	sbt "runMain logic.system.Generator --help"

compile:
	mkdir -p $(CHISEL_OUT)/LAB$(LAB)
	sbt "runMain logic.system.Generator \
			-T logic.lab.$(TOP_MODULE) \
			-C $(PACKAGE).$(CONFIG) 	\
			-td $(CHISEL_OUT)/LAB$(LAB)"

sythesis:




clean:
	rm -rf build target project/target *.v *.anno.json