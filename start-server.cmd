@echo off
REM Build the project, then start the TimeZoneServer in a new terminal window.
cd /d %~dp0
echo Building project...
call mvn -DskipTests package
echo Starting TimeZoneServer (in new window)...
start "TimeZoneServer" cmd /k "java -Djava.rmi.server.hostname=127.0.0.1 -cp target/classes server.TimeZoneServer"
echo Done.
pause
