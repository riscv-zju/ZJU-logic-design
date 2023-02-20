TOP			:= $(CURDIR)
BUILD		:= $(TOP)/build
SCRIPT		:= $(TOP)/scripts

LAB 		?= 0
TOP_MODULE	?= NexysA7FPGAWrapper
COURSE		?= digit
PACKAGE		?= logic101.lab.$(COURSE)._$(LAB)
CONFIG		?= EmptyTarget
BOARD		?= xc7a100tcsg324-1

CHISEL_OUT	:= $(BUILD)/logic101/$(COURSE)/lab$(LAB).$(CONFIG)
VIVADO_OUT	:= $(BUILD)/vivado/$(COURSE)/lab$(LAB).$(CONFIG)
CHECKPOINT	:= $(VIVADO_OUT)/post_route.dcp

export SCRIPT CHISEL_OUT VIVADO_OUT

all: compile

compile_help:
	sbt "runMain logic101.system.Generator --help"

compile:
	mkdir -p $(CHISEL_OUT)
	sbt "runMain logic101.system.Generator \
			-T logic101.fpga.$(TOP_MODULE) \
			-C $(PACKAGE).$(CONFIG) 	\
			-td $(CHISEL_OUT)"

synthesis:
	mkdir -p $(VIVADO_OUT)
	cd $(VIVADO_OUT); vivado -mode batch -nojournal -source $(SCRIPT)/vivado/main.tcl \
									   -tclargs -top-module $(TOP_MODULE) -board $(BOARD)


$(CHECKPOINT): synthesis

fpga: $(CHECKPOINT)
	mkdir -p $(VIVADO_OUT)
	cd $(VIVADO_OUT); vivado -mode batch -nojournal -source $(SCRIPT)/vivado/fpga.tcl $(CHECKPOINT)


clean:
	rm -rf build target project/target *.v *.anno.json
