@echo off
setlocal
color 2
chcp 65001 > nul 2>&1

:: Переходим в папку, где лежит сам .bat файл
cd /d "%~dp0"

:: Генерируем метку времени для имени файла (не зависит от региональных настроек Windows)
for /f %%a in ('powershell -NoProfile -Command "Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'"') do set "TIMESTAMP=%%a"
set "OUTPUT=%TIMESTAMP%_java.txt"

:: Создаём/очищаем выходной файл
type nul > "%OUTPUT%"

echo Поиск и обработка файлов .java...
echo.

:: Рекурсивный обход всех .java файлов
for /r %%F in (*.java) do (
    echo. >> "%OUTPUT%"
    echo ======================================== >> "%OUTPUT%"
    echo Файл: %%~nxF >> "%OUTPUT%"
    echo ======================================== >> "%OUTPUT%"
    type "%%F" >> "%OUTPUT%"
)

echo.
echo Готово! Результат сохранён в: "%OUTPUT%"
pause