# tcl file
#
# Create new command

#tcl;
proc main {outputLog} {
	set sCommandName "PWC_TEST_Command_1"
	puts $outputLog "========================================================================"
	puts $outputLog "INFO: start add command : $sCommandName"

	set ierr [catch {
		#Find if Type exists		
		if { [string compare [mql list type $sCommandName] $sCommandName] != 0 } {
			#adding channel
			mql add command $sCommandName description "SCM Test Command"
			puts $outputLog "added command"
		} else { 
			puts $outputLog "Command : '$sCommandName' already exist."
		} 

	} outMsg]
	 if {$ierr != 0} {
		puts $outputLog "ERROR: $outMsg"
	} else {
		puts $outputLog "INFO: end add command : $sCommandName"
		puts $outputLog "========================================================================"
	}
	return $ierr
}

#main stdout
#exit
