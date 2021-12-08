write_bitstream -force $BUILD_DIR/$TOP.bit

write_sdf -force $BUILD_DIR/$TOP.sdf

write_verilog -mode timesim -force $BUILD_DIR/${TOP}_netlist.v