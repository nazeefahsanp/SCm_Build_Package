@echo off

set targetTag=%1
set sourceDir=%2
set originTag=%3

REM :: CURRENT DIRECTORY LOCATION OF DEPLOY.BAT
set currentDirLocation=%~dp0

REM :: DIRECTORY OF JAR FILES
set jarFilesLocation=%currentDirLocation%\tools\lib

REM :: COPYING CHECKEDOUT CODE TO DISTRIB DIRECTORY EXCEPT SPINNER FOLDER
echo.
call ant -buildfile %currentDirLocation%\build.xml copy
echo.
	
if NOT "%originTag%"=="" (

	
	REM :: NORMALIZING SPINNER FILES & DELTA EXTRACTION OF CHECKEDOUT CODE TO DISTRIB/DELTA DIRECTORY
	echo.
	echo ------------ DELTA EXTRACTION : STARTED ------------
		java -jar %jarFilesLocation%/PWCDeltaExtractor.jar master %originTag% %targetTag% %sourceDir%
	echo ------------ DELTA EXTRACTION : ENDS    ------------
)