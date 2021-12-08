set BUILD_DIR $env(VIVADO_OUT)
set SRC_DIR   $env(CHISEL_OUT)
set TOOL_DIR  $env(SCRIPT)

# Parse command line argument 
while {[llength $argv]} {
  set argv [lassign $argv flag]
  switch -glob $flag {
    -top-module {
      set argv [lassign $argv TOP]
    }
    -board {
      set argv [lassign $argv PART]
    }
    default {
      return -code error [list {unknown option} $flag]
    }
  }
}

if {![info exists TOP]} {
  return -code error [list {-top-module option is required}]
}

if {![info exists PART]} {
  return -code error [list {-board option is required}]
}


# Add source
read_verilog [ glob $SRC_DIR/*.v ]
read_xdc [ glob $SRC_DIR/*.xdc ]


# Synthesis
source [file join $TOOL_DIR vivado synth.tcl]

# Optimize
source [file join $TOOL_DIR vivado opt.tcl]

# Place
source [file join $TOOL_DIR vivado place.tcl]

# Route
source [file join $TOOL_DIR vivado route.tcl]

# Bitstream
source [file join $TOOL_DIR vivado bitstream.tcl]