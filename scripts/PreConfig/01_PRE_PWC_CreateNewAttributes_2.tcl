# tcl file
#
# Create new attribute

#tcl;
proc main {outputLog} {
	set sAttributeName "PWC_SCM_TEST_Attribute_2"
	puts $outputLog "========================================================================"
	puts $outputLog "INFO: start create attribute : $sAttributeName"

	set ierr [catch {
		#Find if attribute exists		
		if { [string compare [mql list attribute $sAttributeName] $sAttributeName] != 0 } {
			#Create new attribute
			mql add attribute $sAttributeName description 'PWC attribute - SCM Test 2' type string
			
			puts $outputLog "Added new attribute to '$sAttributeName'."
		} else {
	
			puts $outputLog "Attribute : '$sAttributeName' already exist."
		} 

	} outMsg]
	 if {$ierr != 0} {
		puts $outputLog "ERROR: $outMsg"
	} else {
		puts $outputLog "INFO: end create attribute : $sAttributeName"
		puts $outputLog "========================================================================"
	}
	return $ierr
}

#main stdout
#exit
