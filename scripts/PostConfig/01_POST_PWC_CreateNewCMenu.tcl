# tcl file
#
# Create new Menu

#tcl;
proc main {outputLog} {
	set sMenuName "SCM_TEST_MENU"
	puts $outputLog "========================================================================"
	puts $outputLog "INFO: start add Menu : $sMenuName"

	set ierr [catch {
		#Find if Type exists		
		if { [string compare [mql list menu $sMenuName] $sMenuName] != 0 } {
			#adding menu
			mql add menu $sMenuName label "SCM Test Menu"
			mql mod menu $sMenuName add command SCM_TEST_Command

			mql mod menu Actions add menu $sMenuName
			puts $outputLog "added Menu"
		} else { 
			puts $outputLog "Menu : '$sMenuName' already exist."
		} 

	} outMsg]
	 if {$ierr != 0} {
		puts $outputLog "ERROR: $outMsg"
	} else {
		puts $outputLog "INFO: end add Menu : $sMenuName"
		puts $outputLog "========================================================================"
	}
	return $ierr
}

#main stdout
#exit
