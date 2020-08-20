REM batch file for target Structure tranformation
@echo off

set repoBranchName=%1
set targetTag=%2
set sourceDir=%3
set originTag=%4


set /p dependencies=< %sourceDir%/distrib/dependencies.txt
echo %dependencies%


echo dependencies in compilation : %dependencies%


set fullBuildTargetDir=pwc.%repoBranchName%.build.%targetTag%\
set deltaBuildTargetDir=pwc.%repoBranchName%.build.%originTag%_%targetTag%\

echo Full build target dir	: %fullBuildTargetDir%
echo Delta build target dir	: %deltaBuildTargetDir%


REM :: CURRENT DIRECTORY LOCATION OF DEPLOY.BAT
set currentDirLocation=%~dp0
echo Current Dir Location	: %currentDirLocation%



REM :: DIRECTORY OF JAR FILES
set jarFilesLocation=%currentDirLocation%\tools\lib

REM :: BUILDING THE JAR FILE FOR ALL SOURCE_CODE MODULES
echo -------------------- JAR CREATION STARTS --------------------
echo.

if NOT "%originTag%"=="" (
	echo -------------------- JAR CREATION FOR DELTA BUILD--------------------

	call ant -buildfile %currentDirLocation%/build.xml jar -DjarSrcCodeDir=%sourceDir%/distrib/%targetTag%/sourcecode/java -DjarDestDir=%sourceDir%\%deltaBuildTargetDir%
) else (
	echo -------------------- JAR CREATION FOR FULL BUILD --------------------

	call ant -buildfile %currentDirLocation%/build.xml jar -DjarSrcCodeDir=%sourceDir%/%targetTag%/sourcecode/java -DjarDestDir=%sourceDir%/%fullBuildTargetDir%\server\enovia\WEB-INF\lib

)
echo -------------------- JAR CREATION ENDS   --------------------
echo.
