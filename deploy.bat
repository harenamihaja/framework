@echo off
setlocal

:: ==============================
:: Variables principales
:: ==============================
set "root=E:\ITU\s5\MrNaina\sprint\framework"
set "framework=monframework"
set "web=web"
set "tomcat_dir=C:\apache-tomcat-10.1.28"
set "war_name=framework"

:: Forcer l'utilisation de Java 17
set "JAVAC=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\javac.exe"
set "JAR=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\jar.exe"

:: ==============================
:: 1) Compiler le framework et créer le jar
:: ==============================
echo.
echo === Compilation du framework ===
cd /d "%root%\%framework%"

if not exist out mkdir out
"%JAVAC%"  -parameters  -cp "lib/*" -d out src\main\java\com\monframework\annotations\*.java
"%JAVAC%"  -parameters  -cp "lib/*;out" -d out src\main\java\com\monframework\models\*.java
"%JAVAC%"  -parameters  -cp "lib/*;out" -d out src\main\java\com\monframework\scanner\*.java
"%JAVAC%"  -parameters  -cp "lib/*;out" -d out src\main\java\com\monframework\exceptions\*.java
"%JAVAC%"  -parameters -cp "lib/*;out" -d out src\main\java\com\monframework\*.java

if %errorlevel% neq 0 (
    echo Erreur lors de la compilation du framework
    exit /b 1
)

echo === Création du jar du framework ===
"%JAR%" cf framework.jar -C out .
if %errorlevel% neq 0 (
    echo Erreur lors de la création du jar
    exit /b 1
)

:: ==============================
:: 2) Préparer la webapp et compiler
:: ==============================
echo.
echo === Préparation de la webapp ===
cd /d "%root%\%web%"

if exist build rd /s /q build
mkdir build
mkdir build\WEB-INF
mkdir build\WEB-INF\classes
mkdir build\WEB-INF\lib

:: Copier le jar du framework dans WEB-INF/lib
copy "%root%\%framework%\framework.jar" "build\WEB-INF\lib\" >nul

:: Copier tous les jars présents dans web\lib vers build\WEB-INF\lib
if exist "%root%\%web%\lib" (
    xcopy /y /s /e "%root%\%web%\lib\*" "build\WEB-INF\lib\" >nul
)

:: Étape 1 : Compiler TOUTES les classes Java en UNE SEULE passe
:: C'est plus simple, plus fiable et évite les problèmes de dépendances
"%JAVAC%" -parameters ^
  -cp "lib/*;build\WEB-INF\lib\*" ^
  -d build\WEB-INF\classes ^
  src\main\java\model\*.java ^
  src\main\java\controller\*.java ^
  src\main\java\*.java

:: Vérifier si la compilation a réussi
if %errorlevel% neq 0 (
    echo Erreur lors de la compilation de la webapp
    exit /b 1
)

:: Copier web.xml et les vues
xcopy /y /s /e "src\main\webapp\WEB-INF\web.xml" "build\WEB-INF\" >nul
xcopy /y /s /e "src\main\webapp\*.jsp" "build\" >nul
xcopy /y /s /e "src\main\webapp\*.html" "build\" >nul
xcopy /y /s /e "src\main\webapp\css" "build\css\" >nul 2>nul
xcopy /y /s /e "src\main\webapp\js" "build\js\" >nul 2>nul

:: ==============================
:: 3) Créer le fichier WAR
:: ==============================
echo.
echo === Création du WAR ===
cd build
"%JAR%" cf "%war_name%.war" *
if %errorlevel% neq 0 (
    echo Erreur lors de la création du WAR
    exit /b 1
)

:: ==============================
:: 4) Déploiement dans Tomcat
:: ==============================
echo.
echo === Déploiement dans Tomcat ===
copy "%war_name%.war" "%tomcat_dir%\webapps\" >nul

echo.
echo === Déploiement terminé avec succès ! ===
echo Votre application est prête : %tomcat_dir%\webapps\%war_name%.war

endlocal
pause
