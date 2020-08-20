# tcl file
#
# Add new command

#tcl;
proc main {outputLog} {
	set sCommandName "SCM_TEST_Command"
	puts $outputLog "========================================================================"
	puts $outputLog "INFO: start create command : $sCommandName"

	set ierr [catch {
		#Find if command exists		
		if { [string compare [mql list command $sCommandName] $sCommandName] != 0 } {
			#Create new command
			mql add command $sCommandName label "SCM Test"
			
			puts $outputLog "Added new command to '$sCommandName'."
		} else {
	
			puts $outputLog "command : '$sCommandName' already exist."
		} 

	} outMsg]
	 if {$ierr != 0} {
		puts $outputLog "ERROR: $outMsg"
	} else {
		puts $outputLog "INFO: end create command : $sCommandName"
		puts $outputLog "========================================================================"
	}
	return $ierr
}

#main stdout
#exit
