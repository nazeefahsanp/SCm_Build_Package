# tcl file
#
# Delete test DPs

#tcl;
proc main {outputLog} {
	set sType "Drawing Print"
	puts $outputLog "========================================================================"
	puts $outputLog "INFO: start delete Drawing Print objects"

	set ierr [catch {
		#Find if exists		
		set result [mql temp query bus "$sType" "PWC_TEST_Drawing*" $sRevision]
		if { $result != "" } {
			#delete objects
			mql trigger off
			mql delete bus "$sType" "PWC_TEST_Drawing Print_1" $sRevision
			mql delete bus "$sType" "PWC_TEST_Drawing Print_2" $sRevision
			mql delete bus "$sType" "PWC_TEST_Drawing Print_3" $sRevision
			mql delete bus "$sType" "PWC_TEST_Drawing Print_4" $sRevision
			mql trigger on
			puts $outputLog "Delete : object  to '$sType' 'PWC_TEST_Drawing Print_1' $sRevision"
			puts $outputLog "Delete : object  to '$sType' 'PWC_TEST_Drawing Print_2' $sRevision"
			puts $outputLog "Delete : object  to '$sType' 'PWC_TEST_Drawing Print_3' $sRevision"
			puts $outputLog "Delete : object  to '$sType' 'PWC_TEST_Drawing Print_4' $sRevision"
		} else {
			puts $outputLog "Objects does not exist."
		} 

	} outMsg]
	 if {$ierr != 0} {
		puts $outputLog "ERROR: $outMsg"
	} else {
		puts $outputLog "INFO: end delete Drawing Print objects"
		puts $outputLog "========================================================================"
	}
	return $ierr
}

#main stdout
#exit
