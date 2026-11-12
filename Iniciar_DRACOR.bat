@echo off
chcp 65001 >nul
title DRACOR - Sistema de Gestion y Seguimiento del Avance de Obras
cd /d "%~dp0"
echo ==============================================================
echo   DRACOR - Sistema de Gestion y Seguimiento del Avance de Obras
echo ==============================================================

rem --- 1) MySQL de XAMPP
powershell -NoProfile -Command "if((Test-NetConnection 127.0.0.1 -Port 3306 -WarningAction SilentlyContinue).TcpTestSucceeded){exit 0}else{exit 1}" >nul 2>&1
if errorlevel 1 (
    echo Iniciando MySQL de XAMPP...
    if exist "C:\xampp\mysql\bin\mysqld.exe" (
        start "" /min "C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone
        timeout /t 12 /nobreak >nul
    ) else (
        echo No se encontro XAMPP en C:\xampp. Inicie MySQL desde el panel de XAMPP.
        pause
        exit /b 1
    )
)

rem --- 2) Compilar si no existe el jar
if not exist "target\sistema-obras-dracor.jar" (
    echo Compilando el sistema ^(primera vez, requiere Internet^)...
    where mvn >nul 2>&1
    if errorlevel 1 (
        for /d %%D in ("C:\Program Files\NetBeans*" "C:\Program Files\Apache NetBeans*") do (
            if exist "%%~D\netbeans\java\maven\bin\mvn.cmd" set "MVN=%%~D\netbeans\java\maven\bin\mvn.cmd"
        )
    ) else (
        set "MVN=mvn"
    )
    if not defined MVN (
        echo No se encontro Maven. Use "Clean and Build" desde NetBeans.
        pause
        exit /b 1
    )
    call "%MVN%" -q -DskipTests package
    if errorlevel 1 (
        echo La compilacion fallo.
        pause
        exit /b 1
    )
)

rem --- 3) Iniciar y abrir el navegador
echo Iniciando el sistema... espere unos segundos.
start "" /b cmd /c "timeout /t 20 /nobreak >nul & start http://localhost:8080"
java -jar "target\sistema-obras-dracor.jar"
pause
