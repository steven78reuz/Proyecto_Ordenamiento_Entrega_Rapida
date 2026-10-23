INICIO RAPIDO (ENTREGA)
=======================
1) Abre esta carpeta completa en VS Code.
2) En la terminal ejecuta: py GeneradorDatos.py
3) Luego: powershell -ExecutionPolicy Bypass -File .\EjecutarTodo.ps1

El modo normal SALTA Bubble, Insertion y Selection para 100000 y 1000000.
No uses -Todos si tienes poco tiempo.

PROYECTO DE ORDENAMIENTO - ESTRUCTURA DE DATOS
==============================================

Algoritmos incluidos:
1. Bubble Sort
2. Selection Sort
3. Quick Sort
4. Merge Sort
5. Heap Sort
6. Bucket Sort
7. Radix Sort
8. Insertion Sort
9. Hash Sort (conteo por rango)

Volumenes:
100, 1,000, 10,000, 100,000 y 1,000,000

Formas:
Ordenados, Inversos y Desordenados

Metricas:
Tiempo de ejecucion, Comparaciones y Cambios/Movimientos

PASO 1 - GENERAR LOS DATOS UNA SOLA VEZ
---------------------------------------
Desde la carpeta principal:

    python GeneradorDatos.py

o en Windows, si python no responde:

    py GeneradorDatos.py

PASO 2 - EJECUTAR CADA LENGUAJE
--------------------------------
IMPORTANTE: por defecto, Bubble/Selection/Insertion se omiten por encima de
10,000 elementos para evitar ejecuciones de horas. Para forzar TODAS las
pruebas, agrega --todos.

Python:
    python Python/Benchmark.py
    python Python/Benchmark.py --todos

C++:
    g++ -O2 CPP/Benchmark.cpp -o CPP/Benchmark
    ./CPP/Benchmark
    ./CPP/Benchmark --todos

Windows PowerShell:
    g++ -O2 CPP/Benchmark.cpp -o CPP/Benchmark.exe
    .\CPP\Benchmark.exe
    .\CPP\Benchmark.exe --todos

C#:
    dotnet run --project CSharp
    dotnet run --project CSharp -- --todos

Java:
    javac Java/Benchmark.java
    java -cp Java Benchmark
    java -cp Java Benchmark --todos

JavaScript:
    node JavaScript/Benchmark.js
    node JavaScript/Benchmark.js --todos

RESULTADOS
----------
Cada lenguaje crea un CSV dentro de Resultados/ y tambien imprime la tabla
en la terminal.

NOTA
----
Los mismos archivos TXT se usan en los 5 lenguajes, asi la comparacion es mas
justa. El tiempo medido corresponde SOLO al algoritmo de ordenamiento, no a
la lectura del archivo.
