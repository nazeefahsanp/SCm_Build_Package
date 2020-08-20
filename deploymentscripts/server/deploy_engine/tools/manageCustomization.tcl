# ###############################################################################################################
# manageCustomizaton.tcl
# This tcl will be called from Ant script by passing the below env variables
# TCL_PROC				: The name of the procedure to call
# Throws error if any exeception
# ##############################################################################################################

tcl;

eval {
	

	#********************************************************************************************************************
	# procedure to get the value of inivar if exist
	#********************************************************************************************************************
	proc getInivarvalue { variable } {
		set keyValue [mql print system inivar "$variable"]
		set value [string trim [lindex [split $keyValue "="] 1]]
	}


	# ****************************************************************************************
	# Procedure to print OOTB and PWC custom information
	# Returns 1 if the process is and delta and input oriin tag is not equals to the product version
	# ****************************************************************************************
    proc printInforAndCheckForBuild { } {

		set inputOriginGITTag [getInivarvalue originTag]
		set deploymentType [getInivarvalue deploymengType]

		# executing prog eServiceHelpAbout.tcl
		set sCmd {mql exec prog eServiceHelpAbout.tcl}
		set result [eval $sCmd]
		set infoList [split $result "|"]
		set inforListLength [llength $infoList]
		puts "=====================OOTB Stack Informtion========================"

		# Printing the information in user readable format
		for {set i 1} {$i < $inforListLength-1} {incr i} {
			set key [lindex $infoList $i] 
			set value [lindex $infoList [expr {$i+1}]] 
			puts "$key = $value"
			set i [expr {$i+1}]
		}

		#check if the page pwcSystemInformation exist
		set sCmd {mql list page pwcSystemInformation}
		set pageName [eval $sCmd]
		if { $pageName != "" } {
			puts "\n==================P&WC Customization Information===================="
			# Getting the property details from pwcSystemInformation page file
			set sCmd {mql print page pwcSystemInformation select property dump |}
			set result [eval $sCmd]
			set infoList [split $result "|"]
			# Printing the information in user readable format
			foreach  entry $infoList {
				set keyValue [regsub "value" $entry "="]
				puts $keyValue
			}

			# Checking if input origin GIT tag (input to deploy_build.sh) is equal to the GIT tag of the last build ..
			# ..installed (on page pwcSystemInformation, the value of the property pwc_product_version)
			if { $deploymentType == "Delta" } {
				# getting the property details of pwcSystemInformation
				set installedBuildGITTag [eval {mql print page pwcSystemInformation select property\[pwc_product_version\].value dump}]

				if { $installedBuildGITTag == $inputOriginGITTag } { 
					puts "The input origin GIT tag is equal to the GIT tag of the last build"
				}	else {
					puts "The input origin GIT tag is not equal to the GIT tag of the last build. Deployment abort!"
					return 1
 				}
			}

		} else {
			puts "Page pwcSystemInformation does not exist. No customization available."	
		}
	}

	# ********************************************************************************************
	# Procedure to update PWC custom information
	# If page pwcSystemInformation does not exist, then creating and updating custom information
	# ********************************************************************************************
	proc updateCostomizationInfo { } {

		set rootFolderDir [getInivarvalue buildDirName]
		set rootFolderName [file tail $rootFolderDir]

		set pwcProductDepKey "pwc_product_deployment"
		set ierr [ catch {

			set sCmd {mql list page pwcSystemInformation}
			set pageName [eval $sCmd]

			if { $pageName == "" } {
				mql add page pwcSystemInformation description 'PWC System Information'
				puts "Page pwcSystemInformation created."
			} else {

				set result [mql print page pwcSystemInformation select property dump |]
				if { $result != ""} {
					set content [mql print page pwcSystemInformation select content dump]
					# Getting the property details from pwcSystemInformation page file
					set propertyList [split $result "|"]

					# Printing the information in user readable format
					append content "\n"
					foreach  entry $propertyList {
						if { [string match "pwc_product_*" $entry] } {
							set keyValue [regsub "value" $entry "="]
							append content $keyValue "\n"
						}
					}	
					append content "\n======================================================================\n" 
					mql mod page pwcSystemInformation content $content
					puts "Content updated on Page pwcSystemInformation."
				}
			}
	    	# ********************************************************************************************

			set pFile [ open "$rootFolderDir/customization.info" r ]
			set costomInfo [ read $pFile ]
			close $pFile

			set infoList [split $costomInfo "\n"]
			set propertyKeyValue ""

			foreach  entry $infoList {
			    if {![string match "#*" $entry] && $entry != ""} {
					set keyValue [split $entry "="]
					set key [string trim [lindex $keyValue 0]]
					set value [string trim [lindex $keyValue 1]]
					append propertyKeyValue " property '$key' value '$value'"
				}
			}

			# setting the pwc_product_deployment property key value
			if { $rootFolderName != ""} {
				append propertyKeyValue " property $pwcProductDepKey value '$rootFolderName'"
			}

			# updating the properties on pwcSystemInformation
			if { $propertyKeyValue != "" } {
			  set cmd "mql mod page pwcSystemInformation $propertyKeyValue"
			  eval $cmd
			  puts "Customization properties updated on Page pwcSystemInformation."

			}

		} outMsg]
		if {$ierr != 0} {
			puts "Update customization failed : $outMsg"
			return 1
		}
	}
	

# START ********************************************************************************************

	# Taking environment variables
	set tclProcedure [mql get env TCL_PROC]
	set directory [mql get env DIRECTORY]

	puts "TCL Procedure : $tclProcedure"

	if { $tclProcedure != "" } {
		set result [ $tclProcedure ]
		puts "TCL result is: $result"

		if { $result == 1 } {
			error " : Execution failed. Please check the log file for information."
		}
	}
}

# END ********************************************************************************************

