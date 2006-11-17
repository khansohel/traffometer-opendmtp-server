REM -----------------------------------------------------
REM  Valid Options: (options must be enclosed in quotes)
REM    "-port=<port>"
REM    "-storedir=<directory>"
REM -----------------------------------------------------
REM ---
set CPATH=.\build\lib\mainfile.jar;.\build\lib\util.jar;.\build\lib\server.jar
set MAIN=org.opendmtp.server_file.Main
set ARGS=-start %1 %2 %3 %4
java -classpath %CPATH% %MAIN% %ARGS%
REM ---
