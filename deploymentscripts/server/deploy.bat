
@echo off
REM setlocal enabledelayedexpansion

echo **************************************************************
echo ######################## deploy.bat #########################

set targetName=$1

REM Check for the deployment directory
if "%originTag%"=="" (
     set buildDirName=%sourceDir%\pwc.%branchName%.build.%targetTag%
     set deploymentType=Full
 ) else (
     set buildDirName=%sourceDir%\pwc.%branchName%.build.%originTag%_%targetTag%
     set deploymentType=Delta
 )

call ant -buildfile %buildDirName%/server/deploymentscripts/deploy_engine/deploy.xml %targetName% -DrepoBranchName="%branchName%" -Ddeployment.type="%deploymentType%" -Dinput.origin.tag="%originTag%" -Dtarget.tag="%targetTag%"