@echo off
rmdir build /s /q > nul 2>&1
mkdir build
"C:\Program Files\Java\jdk-11.0.1\bin\javac" source/*.java -d build/
"C:\Program Files\Java\jdk-11.0.1\bin\java" -cp build Engine