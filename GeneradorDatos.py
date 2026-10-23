from pathlib import Path
import random

TAMANOS = [100, 1_000, 10_000, 100_000, 1_000_000]
BASE = Path(__file__).resolve().parent
DATOS = BASE / "Datos"
DATOS.mkdir(exist_ok=True)


def guardar(ruta: Path, valores):
    # Un valor por linea para que cualquier lenguaje pueda leerlo facilmente.
    with ruta.open("w", encoding="utf-8") as f:
        for v in valores:
            f.write(f"{v}\n")


for n in TAMANOS:
    print(f"Generando {n:,} valores...")

    ordenados = list(range(1, n + 1))
    inversos = list(range(n, 0, -1))
    desordenados = ordenados.copy()
    random.Random(2026 + n).shuffle(desordenados)

    guardar(DATOS / f"ordenados_{n}.txt", ordenados)
    guardar(DATOS / f"inversos_{n}.txt", inversos)
    guardar(DATOS / f"desordenados_{n}.txt", desordenados)

print("\nListo. Los 15 archivos quedaron en la carpeta Datos.")
