TOP			:= $(CURDIR)
BUILD		:= $(TOP)/build
SCRIPT		:= $(TOP)/scripts
CHISEL_OUT	:= $(BUILD)/chisel
VIVADO_OUT	:= $(BUILD)/vivado

LAB 		?= 12
TOP_MODULE	?= NexysA7FPGAWrapper
PACKAGE		?= logic101.lab._$(LAB)
CONFIG		?= Task3Target

all: compile sythesis

compile_help:
	sbt "runMain logic101.system.Generator --help"

compile:
	mkdir -p $(CHISEL_OUT)/LAB$(LAB)
	sbt "runMain logic101.system.Generator \
			-T logic101.fpga.$(TOP_MODULE) \
			-C $(PACKAGE).$(CONFIG) 	\
			-td $(CHISEL_OUT)/LAB$(LAB)"

sythesis:
	@echo "Now add the verilog and tcl files under \`build/LAB$(LAB)\` to your vivado project :P"

clean:
	rm -rf build target project/target *.v *.anno.json