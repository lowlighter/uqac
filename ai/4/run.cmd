@echo off
cls
rmdir build /s /q > nul 2>&1
mkdir build > nul 2>&1
cd source
"C:\Program Files\Java\jdk1.8.0_131\bin\javac" *.java -d ../build
cd ../build
"C:\Program Files\Java\jdk1.8.0_131\bin\jar" cfm Engine.jar ../source/MANIFEST.mf *.class
echo Program running...
echo ----
"C:\Program Files\Java\jdk1.8.0_131\bin\java" -jar Engine.jar
cd ..