# tcl file
#
# Create new attribute

#tcl;
proc main {outputLog} {
	set sAttributeName "PWC_SCM_TEST_Attribute_1"
	puts $outputLog "========================================================================"
	puts $outputLog "INFO: start remove attribute range from attribute: $sAttributeName"

	set ierr [catch {
		#Find if attribute exists		
		if { [string compare [mql list attribute $sAttributeName] $sAttributeName] == 0 } {
			#Create new attribute
			mql mod attribute $sAttributeName remove range value Test1
			puts $outputLog "Removed attribute range from $sAttributeName."
		} else {
	
			puts $outputLog "Attribute : '$sAttributeName' does not exist."
		} 

	} outMsg]
	 if {$ierr != 0} {
		puts $outputLog "ERROR: $outMsg"
	} else {
		puts $outputLog "INFO: end remove attribute range from attribute: $sAttributeName"
		puts $outputLog "========================================================================"
	}
	return $ierr
}

#main stdout
#exit
