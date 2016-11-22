set sources_v [glob -nocomplain $env(SRCDIR)/external/*.v $env(SRCDIR)/*.v]
if { [llength $sources_v] == 0 } then {
    return -code error "\nERROR: No verilog files found.\n"
}
read_verilog "${sources_v}"

set sources_vhdl [glob -nocomplain $env(SRCDIR)/external/*.vhdl]
if { [llength $sources_vhdl] == 0 } then {
    return -code error "\nERROR: No VHDL files found.\n"
}
read_vhdl "${sources_vhdl}"

set constraints [glob -nocomplain $env(CONSTRAINTS)]
puts "Reading constraints file ${constraints}"

read_xdc ${constraints}

synth_design -top $env(TOP) -part xc7a35tftg256-2
write_checkpoint $env(DCP) -force
report_utilization -file $env(UTIL_RPT) -hierarchical
report_timing -sort_by group -max_paths 5 -path_type summary -file $env(TIMING_RPT)
report_timing_summary -file $env(TIMING_SUMMARY)
report_drc -file $env(DRC_RPT)

proc latchCheck {} {
    set num_latches [llength [all_latches -quiet]]
    if {${num_latches} > 0} {
        set msg "Design contains $num_latches latch(es). Check the synthesis log."
        create_drc_violation -name {LATCH-1} -msg $msg
        return -code error ${num_latches}
    } else {
        return {}
    }
}

create_drc_check \
    -name {LATCH-1} \
    -severity {Critical Warning} \
    -desc {Check for latches} \
    -rule_body latchCheck

report_drc -append -file $env(DRC_RPT) -checks {LATCH-1}

## Potential other reports:
# open_report
# report_design_analysis
# report_exceptions
# report_methodology
