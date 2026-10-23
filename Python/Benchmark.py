from pathlib import Path
from time import perf_counter
import csv
import sys

TAMANOS = [100, 1_000, 10_000, 100_000, 1_000_000]
FORMAS = ["ordenados", "inversos", "desordenados"]
PESADOS = {"bubbleSort", "selectionSort", "insertionSort"}
LIMITE_SEGURO = 10_000
EJECUTAR_TODOS = "--todos" in sys.argv

BASE = Path(__file__).resolve().parent.parent
DATOS = BASE / "Datos"
RESULTADOS = BASE / "Resultados"
RESULTADOS.mkdir(exist_ok=True)
CSV_RUTA = RESULTADOS / "Benchmark_Python.csv"

class Stats:
    __slots__ = ("comparaciones", "cambios")
    def __init__(self):
        self.comparaciones = 0
        self.cambios = 0


def bubble_sort(a, s):
    n = len(a)
    for i in range(n - 1):
        hubo_cambio = False
        for j in range(n - 1 - i):
            s.comparaciones += 1
            if a[j] > a[j + 1]:
                a[j], a[j + 1] = a[j + 1], a[j]
                s.cambios += 1
                hubo_cambio = True
        if not hubo_cambio:
            break


def selection_sort(a, s):
    n = len(a)
    for i in range(n - 1):
        minimo = i
        for j in range(i + 1, n):
            s.comparaciones += 1
            if a[j] < a[minimo]:
                minimo = j
        if minimo != i:
            a[i], a[minimo] = a[minimo], a[i]
            s.cambios += 1


def insertion_sort(a, s):
    for i in range(1, len(a)):
        clave = a[i]
        j = i - 1
        while j >= 0:
            s.comparaciones += 1
            if a[j] <= clave:
                break
            a[j + 1] = a[j]
            s.cambios += 1
            j -= 1
        a[j + 1] = clave
        s.cambios += 1


def quick_sort(a, s):
    if len(a) < 2:
        return
    pila = [(0, len(a) - 1)]
    while pila:
        izq, der = pila.pop()
        i, j = izq, der
        pivote = a[(izq + der) // 2]
        while i <= j:
            while i <= der:
                s.comparaciones += 1
                if a[i] >= pivote:
                    break
                i += 1
            while j >= izq:
                s.comparaciones += 1
                if a[j] <= pivote:
                    break
                j -= 1
            if i <= j:
                if i != j:
                    a[i], a[j] = a[j], a[i]
                    s.cambios += 1
                i += 1
                j -= 1
        if izq < j:
            pila.append((izq, j))
        if i < der:
            pila.append((i, der))


def merge_sort(a, s):
    n = len(a)
    tmp = [0] * n
    ancho = 1
    while ancho < n:
        for izq in range(0, n, 2 * ancho):
            medio = min(izq + ancho, n)
            der = min(izq + 2 * ancho, n)
            i, j, k = izq, medio, izq
            while i < medio and j < der:
                s.comparaciones += 1
                if a[i] <= a[j]:
                    tmp[k] = a[i]
                    i += 1
                else:
                    tmp[k] = a[j]
                    j += 1
                s.cambios += 1
                k += 1
            while i < medio:
                tmp[k] = a[i]
                s.cambios += 1
                i += 1
                k += 1
            while j < der:
                tmp[k] = a[j]
                s.cambios += 1
                j += 1
                k += 1
            for k in range(izq, der):
                a[k] = tmp[k]
                s.cambios += 1
        ancho *= 2


def heap_sort(a, s):
    n = len(a)

    def hundir(i, limite):
        while True:
            mayor = i
            izq = 2 * i + 1
            der = izq + 1
            if izq < limite:
                s.comparaciones += 1
                if a[izq] > a[mayor]:
                    mayor = izq
            if der < limite:
                s.comparaciones += 1
                if a[der] > a[mayor]:
                    mayor = der
            if mayor == i:
                return
            a[i], a[mayor] = a[mayor], a[i]
            s.cambios += 1
            i = mayor

    for i in range(n // 2 - 1, -1, -1):
        hundir(i, n)
    for fin in range(n - 1, 0, -1):
        a[0], a[fin] = a[fin], a[0]
        s.cambios += 1
        hundir(0, fin)


def bucket_sort(a, s):
    n = len(a)
    if n < 2:
        return
    minimo = maximo = a[0]
    for x in a[1:]:
        s.comparaciones += 2
        if x < minimo:
            minimo = x
        if x > maximo:
            maximo = x
    if minimo == maximo:
        return
    cantidad = min(n, max(10, n // 100))
    cubetas = [[] for _ in range(cantidad)]
    rango = maximo - minimo + 1
    for x in a:
        idx = min(cantidad - 1, ((x - minimo) * cantidad) // rango)
        cubetas[idx].append(x)

    pos = 0
    for cubeta in cubetas:
        # Insercion dentro de cada cubeta para conservar contadores propios.
        for i in range(1, len(cubeta)):
            clave = cubeta[i]
            j = i - 1
            while j >= 0:
                s.comparaciones += 1
                if cubeta[j] <= clave:
                    break
                cubeta[j + 1] = cubeta[j]
                s.cambios += 1
                j -= 1
            cubeta[j + 1] = clave
            s.cambios += 1
        for x in cubeta:
            a[pos] = x
            pos += 1
            s.cambios += 1


def hash_sort(a, s):
    if len(a) < 2:
        return
    maximo = a[0]
    minimo = a[0]
    for x in a[1:]:
        s.comparaciones += 2
        if x > maximo:
            maximo = x
        if x < minimo:
            minimo = x
    desplazamiento = -minimo
    conteo = [0] * (maximo - minimo + 1)
    for x in a:
        conteo[x + desplazamiento] += 1
    pos = 0
    for i, veces in enumerate(conteo):
        valor = i - desplazamiento
        for _ in range(veces):
            a[pos] = valor
            pos += 1
            s.cambios += 1


def radix_sort(a, s):
    if len(a) < 2:
        return
    # Los datos generados son positivos.
    maximo = a[0]
    for x in a[1:]:
        s.comparaciones += 1
        if x > maximo:
            maximo = x
    salida = [0] * len(a)
    exp = 1
    while maximo // exp > 0:
        conteo = [0] * 10
        for x in a:
            conteo[(x // exp) % 10] += 1
        for i in range(1, 10):
            conteo[i] += conteo[i - 1]
        for i in range(len(a) - 1, -1, -1):
            dig = (a[i] // exp) % 10
            conteo[dig] -= 1
            salida[conteo[dig]] = a[i]
            s.cambios += 1
        for i in range(len(a)):
            a[i] = salida[i]
            s.cambios += 1
        exp *= 10


ALGORITMOS = [
    ("bubbleSort", bubble_sort),
    ("selectionSort", selection_sort),
    ("quickSort", quick_sort),
    ("mergeSort", merge_sort),
    ("heapSort", heap_sort),
    ("bucketSort", bucket_sort),
    ("radixSort", radix_sort),
    ("insertionSort", insertion_sort),
    ("countingSort", hash_sort),
]


def cargar(ruta):
    with open(ruta, "r", encoding="utf-8") as f:
        return [int(x) for x in f.read().split()]


def esta_ordenado(a):
    return all(a[i - 1] <= a[i] for i in range(1, len(a)))


def main():
    if not (DATOS / "ordenados_100.txt").exists():
        print("Faltan los TXT. Ejecuta primero: python GeneradorDatos.py")
        return

    encabezado = ["Algoritmo", "Volumen", "Forma", "TiempoSegundos", "Comparaciones", "Cambios", "Estado"]
    print(f"{'Algoritmo':<16} | {'Volumen':>9} | {'Forma':<13} | {'Tiempo':>12} | {'Comparaciones':>15} | {'Cambios':>15} | Estado")
    print("-" * 108)

    with CSV_RUTA.open("w", newline="", encoding="utf-8") as archivo:
        csvw = csv.writer(archivo)
        csvw.writerow(encabezado)

        for nombre, funcion in ALGORITMOS:
            for n in TAMANOS:
                for forma in FORMAS:
                    if nombre in PESADOS and n > LIMITE_SEGURO and not EJECUTAR_TODOS:
                        fila = [nombre, n, forma.capitalize(), "", "", "", "OMITIDO"]
                        csvw.writerow(fila)
                        archivo.flush()
                        print(f"{nombre:<16} | {n:>9} | {forma.capitalize():<13} | {'-':>12} | {'-':>15} | {'-':>15} | OMITIDO")
                        continue

                    ruta = DATOS / f"{forma}_{n}.txt"
                    datos = cargar(ruta)
                    stats = Stats()
                    inicio = perf_counter()
                    funcion(datos, stats)
                    tiempo = perf_counter() - inicio
                    estado = "OK" if esta_ordenado(datos) else "ERROR"
                    csvw.writerow([nombre, n, forma.capitalize(), f"{tiempo:.6f}", stats.comparaciones, stats.cambios, estado])
                    archivo.flush()
                    print(f"{nombre:<16} | {n:>9} | {forma.capitalize():<13} | {tiempo:>12.6f} | {stats.comparaciones:>15} | {stats.cambios:>15} | {estado}")

    print(f"\nResultados guardados en: {CSV_RUTA}")
    if not EJECUTAR_TODOS:
        print("Modo seguro activo. Usa --todos si realmente necesitas ejecutar los casos cuadraticos gigantes.")


if __name__ == "__main__":
    main()
