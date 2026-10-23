param(
    [switch]$Todos
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$extra = @()
if ($Todos) { $extra = @("--todos") }

Write-Host "=== 1/6 Generando datos ===" -ForegroundColor Cyan
if (Get-Command py -ErrorAction SilentlyContinue) {
    py GeneradorDatos.py
} else {
    python GeneradorDatos.py
}

Write-Host "`n=== 2/6 Python ===" -ForegroundColor Cyan
if (Get-Command py -ErrorAction SilentlyContinue) {
    & py Python/Benchmark.py @extra
} else {
    & python Python/Benchmark.py @extra
}

Write-Host "`n=== 3/6 C++ ===" -ForegroundColor Cyan
g++ -std=c++17 -O2 CPP/Benchmark.cpp -o CPP/Benchmark.exe
& .\CPP\Benchmark.exe @extra

Write-Host "`n=== 4/6 C# ===" -ForegroundColor Cyan
if ($Todos) {
    dotnet run --project CSharp -- --todos
} else {
    dotnet run --project CSharp
}

Write-Host "`n=== 5/6 Java ===" -ForegroundColor Cyan
javac Java/Benchmark.java
& java -cp Java Benchmark @extra

Write-Host "`n=== 6/6 JavaScript ===" -ForegroundColor Cyan
& node JavaScript/Benchmark.js @extra

Write-Host "`nLISTO. Revisa la carpeta Resultados." -ForegroundColor Green
