@echo off
rmdir build /s /q > nul 2>&1
mkdir build > nul 2>&1
cd source
"C:\Program Files\Java\jdk-11.0.1\bin\javac" *.java -d ../build
cd ../build
"C:\Program Files\Java\jdk-11.0.1\bin\jar" cfm Engine.jar ../source/MANIFEST.mf *.class
"C:\Program Files\Java\jdk-11.0.1\bin\java" -jar Engine.jar
cd ..