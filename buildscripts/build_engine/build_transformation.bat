@echo off

set repoBranchName=%1
set targetTag=%2
set sourceDir=%3
set originTag=%4

REM :: CURRENT DIRECTORY LOCATION OF DEPLOY.BAT
set currentDirLocation=%~dp0

REM :: DIRECTORY OF JAR FILES

set jarFilesLocation=%currentDirLocation%\tools\lib

REM :: TRANSFORMING TO TARGET STRUCTURE FROM DISTRIB/DELTA DIRECTORY
echo.
echo -------------------- TRANSFORMATION STARTS ------------------
	for /f "delims=" %%A in ( ' java -jar %jarFilesLocation%/PWCTransformation.jar %repoBranchName% "%originTag%" %targetTag% %sourceDir%\ ' ) do set retvalue=%%A
	set %retvalue%
	echo return value dependencies : %dependencies%
	
	echo %dependencies%> %sourceDir%/distrib/dependencies.txt
echo -------------------- TRANSFORMATION ENDS   ------------------
echo.
