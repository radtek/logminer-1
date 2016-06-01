@echo off

echo Strating...

java_opts="-ms512m -mx512m -Xmn256m -XX:MaxPermSize=128m"

set apphome=%~dp0
set appmainclass=com.logminerplus.gui.Main
set classes=%apphome%\classes
set lib=%apphome%\lib
set classpath=.;%classes%

echo apphome:%~dp0

FOR %%F IN (%lib%\*.jar) DO call :addcp %%F
goto run
:addcp
set classpath=%classpath%;%1
goto :eof

:run

echo %classpath%

java %java_opts% -classpath %classpath% %appmainclass%

@pause