@echo off
REM Use system Maven if available
where mvn >nul 2>&1
if %ERRORLEVEL%==0 (
    mvn %*
) else (
    echo Maven not found. Please install Maven 3.6+ or use IDE to run CateringApplication.
    exit /b 1
)
