set VAR=
set binpath=%~dp0
:Loop
IF "%1"=="" GOTO Continue
   set VAR=%VAR% %1
ENDIF
SHIFT
GOTO Loop
:Continue
for /f "delims=" %%i in ('cd') do set cwd=%%i
cd /d %binpath%
cd ..
cd lib
for /f "delims=" %%i in ('cd') do set libcwd=%%i
set CLASSPATH=%CLASSPATH%;%libcwd%\jalbs.jar;%libcwd%\xstream-1.3.1.jar
cd %cwd%
java -Ddebugflag=false com.cpfj.JBSLProcessor %VAR%
