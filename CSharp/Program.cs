using System.Diagnostics;
using System.Globalization;
using System.Text;

class Program
{
    class Stats { public long Comparaciones; public long Cambios; }
    delegate void Sorter(int[] a, Stats s);

    static readonly int[] Tamanos = { 100, 1_000, 10_000, 100_000, 1_000_000 };
    static readonly string[] Formas = { "ordenados", "inversos", "desordenados" };
    const int LimiteSeguro = 10_000;

    static void BubbleSort(int[] a, Stats s)
    {
        for (int i = 0; i < a.Length - 1; i++)
        {
            bool cambio = false;
            for (int j = 0; j < a.Length - 1 - i; j++)
            {
                s.Comparaciones++;
                if (a[j] > a[j + 1])
                {
                    (a[j], a[j + 1]) = (a[j + 1], a[j]);
                    s.Cambios++; cambio = true;
                }
            }
            if (!cambio) break;
        }
    }

    static void SelectionSort(int[] a, Stats s)
    {
        for (int i = 0; i < a.Length - 1; i++)
        {
            int min = i;
            for (int j = i + 1; j < a.Length; j++)
            {
                s.Comparaciones++;
                if (a[j] < a[min]) min = j;
            }
            if (min != i)
            {
                (a[i], a[min]) = (a[min], a[i]);
                s.Cambios++;
            }
        }
    }

    static void InsertionSort(int[] a, Stats s)
    {
        for (int i = 1; i < a.Length; i++)
        {
            int clave = a[i], j = i - 1;
            while (j >= 0)
            {
                s.Comparaciones++;
                if (a[j] <= clave) break;
                a[j + 1] = a[j]; s.Cambios++; j--;
            }
            a[j + 1] = clave; s.Cambios++;
        }
    }

    static void QuickSort(int[] a, Stats s)
    {
        if (a.Length < 2) return;
        var pila = new Stack<(int Izq, int Der)>();
        pila.Push((0, a.Length - 1));
        while (pila.Count > 0)
        {
            var (izq, der) = pila.Pop();
            int i = izq, j = der, pivote = a[(izq + der) / 2];
            while (i <= j)
            {
                while (i <= der) { s.Comparaciones++; if (a[i] >= pivote) break; i++; }
                while (j >= izq) { s.Comparaciones++; if (a[j] <= pivote) break; j--; }
                if (i <= j)
                {
                    if (i != j) { (a[i], a[j]) = (a[j], a[i]); s.Cambios++; }
                    i++; j--;
                }
            }
            if (izq < j) pila.Push((izq, j));
            if (i < der) pila.Push((i, der));
        }
    }

    static void MergeSort(int[] a, Stats s)
    {
        int n = a.Length;
        var tmp = new int[n];
        for (int ancho = 1; ancho < n; )
        {
            for (int izq = 0; izq < n; izq += 2 * ancho)
            {
                int medio = Math.Min(izq + ancho, n);
                int der = Math.Min(izq + 2 * ancho, n);
                int i = izq, j = medio, k = izq;
                while (i < medio && j < der)
                {
                    s.Comparaciones++;
                    tmp[k++] = a[i] <= a[j] ? a[i++] : a[j++];
                    s.Cambios++;
                }
                while (i < medio) { tmp[k++] = a[i++]; s.Cambios++; }
                while (j < der) { tmp[k++] = a[j++]; s.Cambios++; }
                for (k = izq; k < der; k++) { a[k] = tmp[k]; s.Cambios++; }
            }
            if (ancho > n / 2) break;
            ancho *= 2;
        }
    }

    static void HeapSort(int[] a, Stats s)
    {
        int n = a.Length;
        for (int i = n / 2 - 1; i >= 0; i--) Hundir(a, i, n, s);
        for (int fin = n - 1; fin > 0; fin--)
        {
            (a[0], a[fin]) = (a[fin], a[0]); s.Cambios++;
            Hundir(a, 0, fin, s);
        }
    }

    static void Hundir(int[] a, int i, int limite, Stats s)
    {
        while (true)
        {
            int mayor = i, izq = 2 * i + 1, der = 2 * i + 2;
            if (izq < limite) { s.Comparaciones++; if (a[izq] > a[mayor]) mayor = izq; }
            if (der < limite) { s.Comparaciones++; if (a[der] > a[mayor]) mayor = der; }
            if (mayor == i) return;
            (a[i], a[mayor]) = (a[mayor], a[i]); s.Cambios++; i = mayor;
        }
    }

    static void BucketSort(int[] a, Stats s)
    {
        int n = a.Length;
        if (n < 2) return;
        int min = a[0], max = a[0];
        for (int i = 1; i < n; i++)
        {
            s.Comparaciones += 2;
            if (a[i] < min) min = a[i];
            if (a[i] > max) max = a[i];
        }
        if (min == max) return;
        int cantidad = Math.Min(n, Math.Max(10, n / 100));
        var cubetas = new List<int>[cantidad];
        for (int i = 0; i < cantidad; i++) cubetas[i] = new List<int>();
        long rango = (long)max - min + 1;
        foreach (int x in a)
        {
            int idx = (int)Math.Min(cantidad - 1L, ((long)(x - min) * cantidad) / rango);
            cubetas[idx].Add(x);
        }
        int pos = 0;
        foreach (var b in cubetas)
        {
            for (int i = 1; i < b.Count; i++)
            {
                int clave = b[i], j = i - 1;
                while (j >= 0)
                {
                    s.Comparaciones++;
                    if (b[j] <= clave) break;
                    b[j + 1] = b[j]; s.Cambios++; j--;
                }
                b[j + 1] = clave; s.Cambios++;
            }
            foreach (int x in b) { a[pos++] = x; s.Cambios++; }
        }
    }

    static void HashSort(int[] a, Stats s)
    {
        if (a.Length < 2) return;
        int min = a[0], max = a[0];
        for (int i = 1; i < a.Length; i++)
        {
            s.Comparaciones += 2;
            if (a[i] < min) min = a[i];
            if (a[i] > max) max = a[i];
        }
        int[] conteo = new int[max - min + 1];
        foreach (int x in a) conteo[x - min]++;
        int pos = 0;
        for (int i = 0; i < conteo.Length; i++)
            for (int k = 0; k < conteo[i]; k++) { a[pos++] = i + min; s.Cambios++; }
    }

    static void RadixSort(int[] a, Stats s)
    {
        if (a.Length < 2) return;
        int max = a[0];
        for (int i = 1; i < a.Length; i++) { s.Comparaciones++; if (a[i] > max) max = a[i]; }
        int[] salida = new int[a.Length];
        for (long exp = 1; max / exp > 0; exp *= 10)
        {
            int[] conteo = new int[10];
            foreach (int x in a) conteo[(int)((x / exp) % 10)]++;
            for (int i = 1; i < 10; i++) conteo[i] += conteo[i - 1];
            for (int i = a.Length - 1; i >= 0; i--)
            {
                int d = (int)((a[i] / exp) % 10);
                salida[--conteo[d]] = a[i]; s.Cambios++;
            }
            for (int i = 0; i < a.Length; i++) { a[i] = salida[i]; s.Cambios++; }
            if (exp > max / 10L) break;
        }
    }

    static int[] Cargar(string ruta)
    {
        var lista = new List<int>();
        using var fs = new FileStream(ruta, FileMode.Open, FileAccess.Read, FileShare.Read, 1 << 20);
        int c, num = 0; bool leyendo = false;
        while ((c = fs.ReadByte()) != -1)
        {
            if (c >= '0' && c <= '9') { num = num * 10 + c - '0'; leyendo = true; }
            else if (leyendo) { lista.Add(num); num = 0; leyendo = false; }
        }
        if (leyendo) lista.Add(num);
        return lista.ToArray();
    }

    static bool EstaOrdenado(int[] a)
    {
        for (int i = 1; i < a.Length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }

    static string Bonita(string s) => char.ToUpperInvariant(s[0]) + s[1..];

    static void Main(string[] args)
    {
        bool todos = args.Contains("--todos");
        string baseDir = Directory.GetCurrentDirectory();
        if (!Directory.Exists(Path.Combine(baseDir, "Datos")))
            baseDir = Path.GetFullPath(Path.Combine(baseDir, ".."));
        string datosDir = Path.Combine(baseDir, "Datos");
        string resultadosDir = Path.Combine(baseDir, "Resultados");
        Directory.CreateDirectory(resultadosDir);
        if (!File.Exists(Path.Combine(datosDir, "ordenados_100.txt")))
        {
            Console.WriteLine("Faltan los TXT. Ejecuta primero: python GeneradorDatos.py");
            return;
        }

        var algs = new List<(string Nombre, Sorter Fn)>
        {
            ("bubbleSort", BubbleSort), ("selectionSort", SelectionSort), ("quickSort", QuickSort),
            ("mergeSort", MergeSort), ("heapSort", HeapSort), ("bucketSort", BucketSort),
            ("radixSort", RadixSort), ("insertionSort", InsertionSort), ("countingSort", HashSort)
        };

        string csvPath = Path.Combine(resultadosDir, "Benchmark_CSharp.csv");
        using var csv = new StreamWriter(csvPath, false, Encoding.UTF8);
        csv.WriteLine("Algoritmo,Volumen,Forma,TiempoSegundos,Comparaciones,Cambios,Estado");
        Console.WriteLine($"{"Algoritmo",-16} | {"Volumen",9} | {"Forma",-13} | {"Tiempo",12} | {"Comparaciones",15} | {"Cambios",15} | Estado");
        Console.WriteLine(new string('-', 108));

        foreach (var (nombre, fn) in algs)
        {
            bool pesado = nombre is "bubbleSort" or "selectionSort" or "insertionSort";
            foreach (int n in Tamanos)
            foreach (string forma in Formas)
            {
                string fb = Bonita(forma);
                if (pesado && n > LimiteSeguro && !todos)
                {
                    csv.WriteLine($"{nombre},{n},{fb},,,,OMITIDO"); csv.Flush();
                    Console.WriteLine($"{nombre,-16} | {n,9} | {fb,-13} | {"-",12} | {"-",15} | {"-",15} | OMITIDO");
                    continue;
                }

                int[] a = Cargar(Path.Combine(datosDir, $"{forma}_{n}.txt"));
                var s = new Stats();
                var sw = Stopwatch.StartNew();
                fn(a, s);
                sw.Stop();
                double tiempo = sw.Elapsed.TotalSeconds;
                string estado = EstaOrdenado(a) ? "OK" : "ERROR";
                csv.WriteLine(string.Create(CultureInfo.InvariantCulture, $"{nombre},{n},{fb},{tiempo:F6},{s.Comparaciones},{s.Cambios},{estado}"));
                csv.Flush();
                Console.WriteLine(string.Create(CultureInfo.InvariantCulture, $"{nombre,-16} | {n,9} | {fb,-13} | {tiempo,12:F6} | {s.Comparaciones,15} | {s.Cambios,15} | {estado}"));
            }
        }

        Console.WriteLine($"\nResultados guardados en: {csvPath}");
        if (!todos) Console.WriteLine("Modo seguro activo. Usa --todos para forzar los casos cuadraticos gigantes.");
    }
}
