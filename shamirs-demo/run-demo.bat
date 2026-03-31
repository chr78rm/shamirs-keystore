@echo off

rem defaults
set BULK_SWITCH=ON
set ECHO_SWITCH=OFF
set JLINE_SWITCH=ON
set SUN_MISC_UNSAFE=ON

rem current version
set SHAMIRS_VERSION=1.4.0

rem evaluate arguments
set ARGS=%*
for %%a in (%ARGS%) do (
	if %%a==--no-bulk (
		set BULK_SWITCH=OFF
	)
	if %%a==--echo (
		set ECHO_SWITCH=ON
	)
	if %%a==--jline (
		set JLINE_SWITCH=ON
	)
	if %%a==--base (
		set JLINE_SWITCH=OFF
	)
	if %%a==--sun-misc-unsafe (
		set SUN_MISC_UNSAFE=OFF
	)
)
echo:
echo echo = %ECHO_SWITCH%
echo bulk = %BULK_SWITCH%
echo jline = %JLINE_SWITCH%
echo sun_misc_unsafe = %SUN_MISC_UNSAFE%
if %JLINE_SWITCH%==OFF (
	set CONSOLE_PROPERTY=-Djdk.console=java.base
) else (
    set CONSOLE_PROPERTY=
)
if %SUN_MISC_UNSAFE%==OFF (
	set SUN_MISC_UNSAFE_OPTION=--sun-misc-unsafe-memory-access=allow
) else (
    set SUN_MISC_UNSAFE_OPTION=
)
echo CONSOLE_PROPERTY = %CONSOLE_PROPERTY%
echo SUN_MISC_UNSAFE_OPTION = %SUN_MISC_UNSAFE_OPTION%
echo:

rem checkout JAVA_HOME
if [%JAVA_HOME%]==[] (
	java --version
	if %errorlevel% neq 0 exit /b
	set JAVA_BIN=java
) else (
	%JAVA_HOME%\bin\java --version
	if %errorlevel% neq 0 exit /b
	set JAVA_BIN=%JAVA_HOME%\bin\java
)

%JAVA_BIN% -Dde.christofreichardt.jca.shamirsdemo.console.bulk=%BULK_SWITCH% -Dde.christofreichardt.jca.shamirsdemo.console.echo=%ECHO_SWITCH% %CONSOLE_PROPERTY% %SUN_MISC_UNSAFE_OPTION% -jar target/shamirs-demo-%SHAMIRS_VERSION%.jar
