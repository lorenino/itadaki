@echo off
REM ITADAKI - Launch menu (Windows)
REM Double-click or run: run.bat

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0automation\menu.ps1"
pause
