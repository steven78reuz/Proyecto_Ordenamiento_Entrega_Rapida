import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Benchmark {
    static class Stats { long comparaciones = 0, cambios = 0; }
    interface Sorter { void sort(int[] a, Stats s); }

    static final int[] TAMANOS = {100, 1000, 10000, 100000, 1000000};
    static final String[] FORMAS = {"ordenados", "inversos", "desordenados"};
    static final int LIMITE_SEGURO = 10000;

    static void bubbleSort(int[] a, Stats s) {
        for (int i = 0; i < a.length - 1; i++) {
            boolean cambio = false;
            for (int j = 0; j < a.length - 1 - i; j++) {
                s.comparaciones++;
                if (a[j] > a[j + 1]) {
                    int t = a[j]; a[j] = a[j + 1]; a[j + 1] = t;
                    s.cambios++; cambio = true;
                }
            }
            if (!cambio) break;
        }
    }

    static void selectionSort(int[] a, Stats s) {
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) {
                s.comparaciones++;
                if (a[j] < a[min]) min = j;
            }
            if (min != i) {
                int t = a[i]; a[i] = a[min]; a[min] = t;
                s.cambios++;
            }
        }
    }

    static void insertionSort(int[] a, Stats s) {
        for (int i = 1; i < a.length; i++) {
            int clave = a[i], j = i - 1;
            while (j >= 0) {
                s.comparaciones++;
                if (a[j] <= clave) break;
                a[j + 1] = a[j]; s.cambios++; j--;
            }
            a[j + 1] = clave; s.cambios++;
        }
    }

    static void quickSort(int[] a, Stats s) {
        if (a.length < 2) return;
        ArrayDeque<long[]> pila = new ArrayDeque<>();
        pila.push(new long[]{0, a.length - 1});
        while (!pila.isEmpty()) {
            long[] p = pila.pop();
            int izq = (int)p[0], der = (int)p[1];
            int i = izq, j = der, pivote = a[(izq + der) >>> 1];
            while (i <= j) {
                while (i <= der) { s.comparaciones++; if (a[i] >= pivote) break; i++; }
                while (j >= izq) { s.comparaciones++; if (a[j] <= pivote) break; j--; }
                if (i <= j) {
                    if (i != j) { int t = a[i]; a[i] = a[j]; a[j] = t; s.cambios++; }
                    i++; j--;
                }
            }
            if (izq < j) pila.push(new long[]{izq, j});
            if (i < der) pila.push(new long[]{i, der});
        }
    }

    static void mergeSort(int[] a, Stats s) {
        int n = a.length;
        int[] tmp = new int[n];
        for (int ancho = 1; ancho < n; ) {
            for (int izq = 0; izq < n; izq += 2 * ancho) {
                int medio = Math.min(izq + ancho, n);
                int der = Math.min(izq + 2 * ancho, n);
                int i = izq, j = medio, k = izq;
                while (i < medio && j < der) {
                    s.comparaciones++;
                    tmp[k++] = a[i] <= a[j] ? a[i++] : a[j++];
                    s.cambios++;
                }
                while (i < medio) { tmp[k++] = a[i++]; s.cambios++; }
                while (j < der) { tmp[k++] = a[j++]; s.cambios++; }
                for (k = izq; k < der; k++) { a[k] = tmp[k]; s.cambios++; }
            }
            if (ancho > n / 2) break;
            ancho *= 2;
        }
    }

    static void heapSort(int[] a, Stats s) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) hundir(a, i, n, s);
        for (int fin = n - 1; fin > 0; fin--) {
            int t = a[0]; a[0] = a[fin]; a[fin] = t; s.cambios++;
            hundir(a, 0, fin, s);
        }
    }

    static void hundir(int[] a, int i, int limite, Stats s) {
        while (true) {
            int mayor = i, izq = 2 * i + 1, der = izq + 1;
            if (izq < limite) { s.comparaciones++; if (a[izq] > a[mayor]) mayor = izq; }
            if (der < limite) { s.comparaciones++; if (a[der] > a[mayor]) mayor = der; }
            if (mayor == i) return;
            int t = a[i]; a[i] = a[mayor]; a[mayor] = t; s.cambios++; i = mayor;
        }
    }

    @SuppressWarnings("unchecked")
    static void bucketSort(int[] a, Stats s) {
        int n = a.length;
        if (n < 2) return;
        int min = a[0], max = a[0];
        for (int i = 1; i < n; i++) {
            s.comparaciones += 2;
            if (a[i] < min) min = a[i];
            if (a[i] > max) max = a[i];
        }
        if (min == max) return;
        int cantidad = Math.min(n, Math.max(10, n / 100));
        ArrayList<Integer>[] cubetas = new ArrayList[cantidad];
        for (int i = 0; i < cantidad; i++) cubetas[i] = new ArrayList<>();
        long rango = (long)max - min + 1;
        for (int x : a) {
            int idx = (int)Math.min(cantidad - 1L, ((long)(x - min) * cantidad) / rango);
            cubetas[idx].add(x);
        }
        int pos = 0;
        for (ArrayList<Integer> b : cubetas) {
            for (int i = 1; i < b.size(); i++) {
                int clave = b.get(i), j = i - 1;
                while (j >= 0) {
                    s.comparaciones++;
                    if (b.get(j) <= clave) break;
                    b.set(j + 1, b.get(j)); s.cambios++; j--;
                }
                b.set(j + 1, clave); s.cambios++;
            }
            for (int x : b) { a[pos++] = x; s.cambios++; }
        }
    }

    static void hashSort(int[] a, Stats s) {
        if (a.length < 2) return;
        int min = a[0], max = a[0];
        for (int i = 1; i < a.length; i++) {
            s.comparaciones += 2;
            if (a[i] < min) min = a[i];
            if (a[i] > max) max = a[i];
        }
        int[] conteo = new int[max - min + 1];
        for (int x : a) conteo[x - min]++;
        int pos = 0;
        for (int i = 0; i < conteo.length; i++) {
            for (int k = 0; k < conteo[i]; k++) { a[pos++] = i + min; s.cambios++; }
        }
    }

    static void radixSort(int[] a, Stats s) {
        if (a.length < 2) return;
        int max = a[0];
        for (int i = 1; i < a.length; i++) { s.comparaciones++; if (a[i] > max) max = a[i]; }
        int[] salida = new int[a.length];
        for (long exp = 1; max / exp > 0; exp *= 10) {
            int[] conteo = new int[10];
            for (int x : a) conteo[(int)((x / exp) % 10)]++;
            for (int i = 1; i < 10; i++) conteo[i] += conteo[i - 1];
            for (int i = a.length - 1; i >= 0; i--) {
                int d = (int)((a[i] / exp) % 10);
                salida[--conteo[d]] = a[i]; s.cambios++;
            }
            for (int i = 0; i < a.length; i++) { a[i] = salida[i]; s.cambios++; }
            if (exp > max / 10L) break;
        }
    }

    static int[] cargar(Path ruta) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(ruta))) {
            int[] datos = new int[1024];
            int size = 0, c, num = 0; boolean leyendo = false;
            while ((c = in.read()) != -1) {
                if (c >= '0' && c <= '9') { num = num * 10 + (c - '0'); leyendo = true; }
                else if (leyendo) {
                    if (size == datos.length) datos = Arrays.copyOf(datos, datos.length * 2);
                    datos[size++] = num; num = 0; leyendo = false;
                }
            }
            if (leyendo) {
                if (size == datos.length) datos = Arrays.copyOf(datos, datos.length * 2);
                datos[size++] = num;
            }
            return Arrays.copyOf(datos, size);
        }
    }

    static boolean ordenado(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }

    static String bonita(String s) { return Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    public static void main(String[] args) throws Exception {
        boolean todos = Arrays.asList(args).contains("--todos");
        Path base = Paths.get("").toAbsolutePath();
        if (!Files.exists(base.resolve("Datos"))) base = Paths.get("Java").toAbsolutePath().getParent();
        Path datosDir = base.resolve("Datos"), resultadosDir = base.resolve("Resultados");
        Files.createDirectories(resultadosDir);
        if (!Files.exists(datosDir.resolve("ordenados_100.txt"))) {
            System.out.println("Faltan los TXT. Ejecuta primero: python GeneradorDatos.py"); return;
        }

        LinkedHashMap<String, Sorter> algs = new LinkedHashMap<>();
        algs.put("bubbleSort", Benchmark::bubbleSort);
        algs.put("selectionSort", Benchmark::selectionSort);
        algs.put("quickSort", Benchmark::quickSort);
        algs.put("mergeSort", Benchmark::mergeSort);
        algs.put("heapSort", Benchmark::heapSort);
        algs.put("bucketSort", Benchmark::bucketSort);
        algs.put("radixSort", Benchmark::radixSort);
        algs.put("insertionSort", Benchmark::insertionSort);
        algs.put("hashSort", Benchmark::hashSort);

        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(resultadosDir.resolve("Benchmark_Java.csv")))) {
            csv.println("Algoritmo,Volumen,Forma,TiempoSegundos,Comparaciones,Cambios,Estado");
            System.out.printf("%-16s | %9s | %-13s | %12s | %15s | %15s | Estado%n", "Algoritmo", "Volumen", "Forma", "Tiempo", "Comparaciones", "Cambios");
            System.out.println("-".repeat(108));

            for (var e : algs.entrySet()) {
                String nombre = e.getKey();
                boolean pesado = nombre.equals("bubbleSort") || nombre.equals("selectionSort") || nombre.equals("insertionSort");
                for (int n : TAMANOS) for (String forma : FORMAS) {
                    String fb = bonita(forma);
                    if (pesado && n > LIMITE_SEGURO && !todos) {
                        csv.printf("%s,%d,%s,,,,OMITIDO%n", nombre, n, fb); csv.flush();
                        System.out.printf("%-16s | %9d | %-13s | %12s | %15s | %15s | OMITIDO%n", nombre, n, fb, "-", "-", "-");
                        continue;
                    }
                    int[] a = cargar(datosDir.resolve(forma + "_" + n + ".txt"));
                    Stats s = new Stats();
                    long ini = System.nanoTime();
                    e.getValue().sort(a, s);
                    double tiempo = (System.nanoTime() - ini) / 1_000_000_000.0;
                    String estado = ordenado(a) ? "OK" : "ERROR";
                    csv.printf(Locale.US, "%s,%d,%s,%.6f,%d,%d,%s%n", nombre, n, fb, tiempo, s.comparaciones, s.cambios, estado); csv.flush();
                    System.out.printf(Locale.US, "%-16s | %9d | %-13s | %12.6f | %15d | %15d | %s%n", nombre, n, fb, tiempo, s.comparaciones, s.cambios, estado);
                }
            }
        }
        System.out.println("\nResultados guardados en Resultados/Benchmark_Java.csv");
        if (!todos) System.out.println("Modo seguro activo. Usa --todos para forzar los casos cuadraticos gigantes.");
    }
}
