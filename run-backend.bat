@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo      Lancement du Backend Ladafura (Spring Boot)
echo ======================================================

cd /d "%~dp0"

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERREUR] Java n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Java 21 (JDK 21) et configurer JAVA_HOME.
    pause
    exit /b 1
)

echo [1/2] Verification de la version Java...
java -version

if exist ".env" (
    echo [2/2] Chargement des variables depuis .env...
    for /f "usebackq tokens=1* delims==" %%a in (".env") do (
        set "line=%%a"
        if not "!line:~0,1!"=="#" (
            set "%%a=%%b"
        )
    )
)

echo Demarrage du serveur Spring Boot...
echo Swagger UI sera accessible sur : http://localhost:8080/swagger-ui/index.html
echo Actuator Health : http://localhost:8080/actuator/health
echo.

call mvnw.cmd spring-boot:run
if %errorlevel% neq 0 (
    echo.
    echo [ERREUR] Echec du demarrage du backend.
    pause
)

