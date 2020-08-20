# ########################################################################################
# deploy.tcl																			 #
# This TCL will be called from ANT script by passing required environment variables		 #
# Success/Failure logs will be available in /logs/deployment.log file					 #
# Spinner execution success/falure logs will be available on the Spinner log location	 #
# ########################################################################################

tcl;

eval {

	# **********************************************************************************
	# Procedure to find the .tcl files in the directory and its sub directories
	# **********************************************************************************
	proc findFiles { basedir } {
	set pattern "*.tcl"	
		set basedir [string trimright [file join [file normalize $basedir] { }]]
		set fileList {}
		# Look in the current directory for matching files
		set fileList [glob -nocomplain -type {f r} -path $basedir $pattern]

		return $fileList
	}

	# **********************************************************************************
	# Procedure to execute list of scripts
	# System stops the process if any script fails and returns 1
	# **********************************************************************************
	proc runScripts { fileList } {
		foreach fileWithPath $fileList {
			set return [catch {source $fileWithPath} exception]
			if { $return == 0} { 
				set mqlRet [main stdout]
				if { $mqlRet != 0} {
					puts "Exception in [file tail $fileWithPath]"
					return 1
				} else {
					puts "EXECUTED : [file tail $fileWithPath]"
				}
			} else {
				puts $exception
				return 1
			}
		}
		
		return 0
	}
    
	# **********************************************************************************
	# Procedure to execute scripts based on the directory provided
	# System stops the process if any script fails and returns 1
	# **********************************************************************************
	proc executeScripts { scriptDir } {
		# Finding all the .tcl scripts available
		set scripts [findFiles $scriptDir]	
		set scripts [lsort $scripts]

		# executing all the scripts
		set return [runScripts $scripts]

		if { $return != "0" } {
			return 1
		}
		return 0
	}

	# **********************************************************************************
	# Procedure to execute one time scripts based on the directory provided
	# System stops the process if any script fails and returns 1
	# **********************************************************************************
	proc executeOnetimeScripts { scriptDir } {
		set sCmd {mql list page pwcSystemInformation}
		set pageExists [eval $sCmd]
		if { $pageExists == "" } {
			return [executeScripts $scriptDir]
		} else {
			puts "Customization available, not executing one time scripts."
		}
		return 0
	}

	# **********************************************************************************
	# Procedure to check if spinner execution is successful or not
	# System stops the process if any error in the spinner log file
	# **********************************************************************************
   	proc isSpinnerExecutionSuccessful { spinnerDir } {
		set result 0
		set spinnerErrorLog "$spinnerDir/logs/SpinnerError.log"
		if { [file exists $spinnerErrorLog] == 1} {     	
			set pFile [ open "$spinnerErrorLog" r ]
			set errorFileContent [ read $pFile ]
			close $pFile
			set fileContent [split $errorFileContent "\n"]
			foreach row $fileContent {
				puts $row
				if {[string match "Error:*" $row]} {
					set result 1
				}
			}
		}
		if { $result == 1} {
			puts "Spinner Execution failed. Deployment abort."
			return $result
		}
		return 0
   	}	

	# **********************************************************************************
	# Procedure to execute Spinner
	# SPINNER_PATH : Env variable to define the spinner folder
	# System stops the process if any script fails and returns 1
	# **********************************************************************************
   	proc executeSpinner { spinnerDir } {
		puts "START - Spinner Execution."
		mql set env SPINNERIMPORTPATH $spinnerDir
		set sCmd {mql exec prog SpinnerImport content=schema -f}
		set mqlret [catch {eval $sCmd} exception]
		if { $mqlret !=0 } { 
			puts $exception
			return 1
		} else {
			puts "END - Spinner Execution."
		}
		return [isSpinnerExecutionSuccessful $spinnerDir]
   	}	   

	# ********************************************************************************************************
	# Procedure to execute all the sub folders inside the cleanup folder if below condition is satisfied
	#		1. If build directory name starting with an integer and Greater than the installed build GIT Tag
	# The sub directories contains the script and the spinner folder will be executed in the below order
	#		1. OneTimeScruots
	#		2. PreConfig
	#		3. Spinner
	#		4. PostConfig
	# System stops the process if any error/exeception and returns 1
	# ********************************************************************************************************
	proc executeCleanupDirectory { cleanupDir } {
		set cleanupDir [string trimright [file join [file normalize $cleanupDir] { }]]
		set cleanupDirList {}

		set sCmd {mql list page pwcSystemInformation}
		set pageExists [eval $sCmd]

		if { $pageExists != "" } {

			puts "Comstomization exists, executing Cleanup."
			# Fetching the installed build tag
			set buildGITTag [eval {mql print page pwcSystemInformation select property\[pwc_product_version\].value dump}]

			# Checking if the build installation tag doesnot exit
			if { $buildGITTag != "" && 1 == [string is wideinteger $buildGITTag] } {
				#verifying if the GIT Tag name is integer
				foreach cleanupDirs [glob -nocomplain -type {d  r} -path $cleanupDir *] {	
					# Fetching the cleanup subdirectory to check if it starts with a GIT Tag
					set dirName [lindex [split [file tail $cleanupDirs]  "_"] 0]

					#checking if the directory name is integer
					set isDirInteger [string is wideinteger $dirName]
					# Checking if the cleanup sub directory name (GIT Tag) is greater than the installed build GIT tag
					if { $isDirInteger == 1 && $dirName > $buildGITTag} {
						lappend cleanupDirList $cleanupDirs
					}
				}

				set cleanupDirList [lsort $cleanupDirList]
				foreach dirPath  $cleanupDirList {
					set spinnerDir "$dirPath/spinner"
					# Executing all the .tcl scripts under oneTimeScripts dir
					set return [executeScripts "$dirPath/scripts/OneTimeScripts" ]
					if { $return == 0} {
						# Executing all the .tcl scripts under PreConfig dir
						set return [executeScripts "$dirPath/scripts/PreConfig"]
					}

					if { $return == 0 && [file isdirectory $spinnerDir]} {

						# spinner execution
						set return [executeSpinner $spinnerDir]
					}

					if { $return == 0} {
						# Executing all the .tcl scripts under PostConfig dir
						set return [executeScripts "$dirPath/scripts/PostConfig"]
					}
					if { $return != 0 } {
						puts "Cleanup Scripts execution failed..!"
						return 1
					}
				}
			} else { 
				puts "No PWC Product Version found OR PWC Product Verison is not an integer."
				return 1
			}
		} else {
			puts "No customization available."
		}
	}

	# **********************************************************************************
	# Procedure to compile all the JPOs in the database
	# System stops the process if any error/exeception and returns 1
	# **********************************************************************************
	proc compileJPOs { } {
		#IMB: need to have some flexibility here for saving the 2 hours compilation of the alll JPOs
		#IMB: One use case would be if in a delta build we have only PWC fully owned JPOs.
		#In that case it may be possible to compile only the JPOs from the spinner/SourceFiles directory

   		puts "JPO Compilation started.."
   		#set sCmd {mql compile prog * force update}
   		set sCmd {mql compile prog emxECMBOMPartBase force update}

   		# compiling all the JPOs
   		set mqlret [catch {eval $sCmd} exception]
   		if { $mqlret != 0} { 
			puts "JPO Compilation failed : $exception"
			return 1
   		}
   		puts "JPO Compilation completed successfully.."
	}


	# **********************************************************************************
	# Procedure to execute spinner for 6W Vocabularies
	# USER_NAME				 : User name
	# PASSWORD				 : Password
	# System stops the process if any error/exeception and returns 1
	# **********************************************************************************
	proc configure6WVocabularies { dir6WVocDir } {
		set userName [mql get env USERNAME]
		set password [mql get env PASSWORD]

		puts "6W Vocabularies Execution - Starts"
		
		mql set env SPINNERIMPORTPATH "$dir6WVocDir"
		set sCmd {mql exec prog SpinnerImport content=6wvocabularies username=”$userName” password=”$password”}
		set mqlret [catch {eval $sCmd} exception]
		
		if { $mqlret !=0 } { 
			puts "6W vocabularies configuratio failed : $exception"
			return 1
		}

		puts "6W Vocabularies Execution - Ends"
	}

	# **********************************************************************************
	# Procedure to set the PWC_config.xml to searchindex and start the full indexing
	# CONFIG_FILE : PWC_config.xml file full path
	# NEED_FULL_INDEX				 : User input (Y/N)
	# System stops the process if any error/exeception and returns 1
	# **********************************************************************************
	proc import3DSpaceIndex { } {
		set configPath [mql get env PWC_CONFIG_FILE]
		set needFullIndex [mql get env NEED_FULL_INDEX]

		set ierr [ catch {
			eval "mql set system searchindex file $configPath"
			puts "PWC_Config updated to the searchindex"

			if { $needFullIndex == "Y" } {
				# start searchindex mode full
				puts "Import 3D space Index Start."
				set result [eval "mql start searchindex mode full vault 'eService Production'"]
				puts "Import 3D space Index End."
			}

		} outMsg]
		if {$ierr != 0} {
			puts "3D Space indexing failed : $outMsg"
			return 1
		}
	}

# START **************************************************************************************

	# fetching environment variables
	set tclProcedure [mql get env TCL_PROC]
	set directory [mql get env DIRECTORY]

	set tclProcedure [mql get env TCL_PROC]
	puts "TCL Procedure : $tclProcedure"

	if { $tclProcedure != "" } {
	   set result ""
		if { $directory == "." || $directory == "" } {
			set result [ $tclProcedure ]
		} else {
			set result [ $tclProcedure $directory ]
		}
		puts "TCL result is: $result"

		if { $result == 1 } {
			error "Execution failed. Please check the log file for information."
		}
	}
}
# END **************************************************************************************
