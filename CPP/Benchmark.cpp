#include <algorithm>
#include <chrono>
#include <cmath>
#include <filesystem>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <utility>
#include <vector>
using namespace std;
namespace fs = std::filesystem;

struct Stats {
    unsigned long long comparaciones = 0;
    unsigned long long cambios = 0;
};

const vector<int> TAMANOS = {100, 1000, 10000, 100000, 1000000};
const vector<string> FORMAS = {"ordenados", "inversos", "desordenados"};
const int LIMITE_SEGURO = 10000;

void bubbleSort(vector<int>& a, Stats& s) {
    int n = (int)a.size();
    for (int i = 0; i < n - 1; ++i) {
        bool cambio = false;
        for (int j = 0; j < n - 1 - i; ++j) {
            ++s.comparaciones;
            if (a[j] > a[j + 1]) {
                swap(a[j], a[j + 1]);
                ++s.cambios;
                cambio = true;
            }
        }
        if (!cambio) break;
    }
}

void selectionSort(vector<int>& a, Stats& s) {
    int n = (int)a.size();
    for (int i = 0; i < n - 1; ++i) {
        int minimo = i;
        for (int j = i + 1; j < n; ++j) {
            ++s.comparaciones;
            if (a[j] < a[minimo]) minimo = j;
        }
        if (minimo != i) {
            swap(a[i], a[minimo]);
            ++s.cambios;
        }
    }
}

void insertionSort(vector<int>& a, Stats& s) {
    for (int i = 1; i < (int)a.size(); ++i) {
        int clave = a[i];
        int j = i - 1;
        while (j >= 0) {
            ++s.comparaciones;
            if (a[j] <= clave) break;
            a[j + 1] = a[j];
            ++s.cambios;
            --j;
        }
        a[j + 1] = clave;
        ++s.cambios;
    }
}

void quickSort(vector<int>& a, Stats& s) {
    if (a.size() < 2) return;
    vector<pair<int,int>> pila;
    pila.push_back({0, (int)a.size() - 1});
    while (!pila.empty()) {
        auto [izq, der] = pila.back();
        pila.pop_back();
        int i = izq, j = der;
        int pivote = a[(izq + der) / 2];
        while (i <= j) {
            while (i <= der) {
                ++s.comparaciones;
                if (a[i] >= pivote) break;
                ++i;
            }
            while (j >= izq) {
                ++s.comparaciones;
                if (a[j] <= pivote) break;
                --j;
            }
            if (i <= j) {
                if (i != j) {
                    swap(a[i], a[j]);
                    ++s.cambios;
                }
                ++i;
                --j;
            }
        }
        if (izq < j) pila.push_back({izq, j});
        if (i < der) pila.push_back({i, der});
    }
}

void mergeSort(vector<int>& a, Stats& s) {
    int n = (int)a.size();
    vector<int> tmp(n);
    for (int ancho = 1; ancho < n; ancho *= 2) {
        for (int izq = 0; izq < n; izq += 2 * ancho) {
            int medio = min(izq + ancho, n);
            int der = min(izq + 2 * ancho, n);
            int i = izq, j = medio, k = izq;
            while (i < medio && j < der) {
                ++s.comparaciones;
                if (a[i] <= a[j]) tmp[k++] = a[i++];
                else tmp[k++] = a[j++];
                ++s.cambios;
            }
            while (i < medio) { tmp[k++] = a[i++]; ++s.cambios; }
            while (j < der) { tmp[k++] = a[j++]; ++s.cambios; }
            for (k = izq; k < der; ++k) { a[k] = tmp[k]; ++s.cambios; }
        }
        if (ancho > n / 2) break;
    }
}

void heapSort(vector<int>& a, Stats& s) {
    int n = (int)a.size();
    auto hundir = [&](int inicio, int limite) {
        int i = inicio;
        while (true) {
            int mayor = i;
            int izq = 2 * i + 1;
            int der = izq + 1;
            if (izq < limite) { ++s.comparaciones; if (a[izq] > a[mayor]) mayor = izq; }
            if (der < limite) { ++s.comparaciones; if (a[der] > a[mayor]) mayor = der; }
            if (mayor == i) break;
            swap(a[i], a[mayor]); ++s.cambios; i = mayor;
        }
    };
    for (int i = n / 2 - 1; i >= 0; --i) hundir(i, n);
    for (int fin = n - 1; fin > 0; --fin) {
        swap(a[0], a[fin]); ++s.cambios;
        hundir(0, fin);
    }
}

void bucketSort(vector<int>& a, Stats& s) {
    int n = (int)a.size();
    if (n < 2) return;
    int minimo = a[0], maximo = a[0];
    for (int i = 1; i < n; ++i) {
        s.comparaciones += 2;
        if (a[i] < minimo) minimo = a[i];
        if (a[i] > maximo) maximo = a[i];
    }
    if (minimo == maximo) return;
    int cantidad = min(n, max(10, n / 100));
    vector<vector<int>> cubetas(cantidad);
    long long rango = (long long)maximo - minimo + 1;
    for (int x : a) {
        int idx = (int)min<long long>(cantidad - 1, ((long long)(x - minimo) * cantidad) / rango);
        cubetas[idx].push_back(x);
    }
    int pos = 0;
    for (auto& b : cubetas) {
        for (int i = 1; i < (int)b.size(); ++i) {
            int clave = b[i], j = i - 1;
            while (j >= 0) {
                ++s.comparaciones;
                if (b[j] <= clave) break;
                b[j + 1] = b[j]; ++s.cambios; --j;
            }
            b[j + 1] = clave; ++s.cambios;
        }
        for (int x : b) { a[pos++] = x; ++s.cambios; }
    }
}

void hashSort(vector<int>& a, Stats& s) {
    if (a.size() < 2) return;
    int minimo = a[0], maximo = a[0];
    for (size_t i = 1; i < a.size(); ++i) {
        s.comparaciones += 2;
        if (a[i] < minimo) minimo = a[i];
        if (a[i] > maximo) maximo = a[i];
    }
    vector<int> conteo((size_t)((long long)maximo - minimo + 1), 0);
    for (int x : a) ++conteo[x - minimo];
    size_t pos = 0;
    for (size_t i = 0; i < conteo.size(); ++i) {
        for (int k = 0; k < conteo[i]; ++k) { a[pos++] = (int)i + minimo; ++s.cambios; }
    }
}

void radixSort(vector<int>& a, Stats& s) {
    if (a.size() < 2) return;
    int maximo = a[0];
    for (size_t i = 1; i < a.size(); ++i) { ++s.comparaciones; if (a[i] > maximo) maximo = a[i]; }
    vector<int> salida(a.size());
    for (long long exp = 1; maximo / exp > 0; exp *= 10) {
        int conteo[10] = {0};
        for (int x : a) ++conteo[(x / exp) % 10];
        for (int i = 1; i < 10; ++i) conteo[i] += conteo[i - 1];
        for (int i = (int)a.size() - 1; i >= 0; --i) {
            int d = (a[i] / exp) % 10;
            salida[--conteo[d]] = a[i]; ++s.cambios;
        }
        for (size_t i = 0; i < a.size(); ++i) { a[i] = salida[i]; ++s.cambios; }
        if (exp > maximo / 10) break;
    }
}

vector<int> cargar(const fs::path& ruta) {
    ifstream f(ruta);
    vector<int> datos;
    int x;
    while (f >> x) datos.push_back(x);
    return datos;
}

bool ordenado(const vector<int>& a) {
    for (size_t i = 1; i < a.size(); ++i) if (a[i - 1] > a[i]) return false;
    return true;
}

int main(int argc, char** argv) {
    bool todos = argc > 1 && string(argv[1]) == "--todos";
    fs::path base = fs::current_path();
    if (!fs::exists(base / "Datos")) base = fs::path(argv[0]).parent_path().parent_path();
    fs::path datosDir = base / "Datos";
    fs::path resultadosDir = base / "Resultados";
    fs::create_directories(resultadosDir);
    if (!fs::exists(datosDir / "ordenados_100.txt")) {
        cerr << "Faltan los TXT. Ejecuta primero: python GeneradorDatos.py\n";
        return 1;
    }

    using Fn = void(*)(vector<int>&, Stats&);
    vector<pair<string, Fn>> algs = {
        {"bubbleSort", bubbleSort}, {"selectionSort", selectionSort}, {"quickSort", quickSort},
        {"mergeSort", mergeSort}, {"heapSort", heapSort}, {"bucketSort", bucketSort},
        {"radixSort", radixSort}, {"insertionSort", insertionSort}, {"hashSort", hashSort}
    };

    ofstream csv(resultadosDir / "Benchmark_CPP.csv");
    csv << "Algoritmo,Volumen,Forma,TiempoSegundos,Comparaciones,Cambios,Estado\n";

    cout << left << setw(16) << "Algoritmo" << " | " << right << setw(9) << "Volumen" << " | "
         << left << setw(13) << "Forma" << " | " << right << setw(12) << "Tiempo" << " | "
         << setw(15) << "Comparaciones" << " | " << setw(15) << "Cambios" << " | Estado\n";
    cout << string(108, '-') << '\n';

    for (auto& [nombre, fn] : algs) {
        bool pesado = nombre == "bubbleSort" || nombre == "selectionSort" || nombre == "insertionSort";
        for (int n : TAMANOS) {
            for (const string& forma : FORMAS) {
                string formaBonita = forma;
                formaBonita[0] = (char)toupper(formaBonita[0]);
                if (pesado && n > LIMITE_SEGURO && !todos) {
                    csv << nombre << ',' << n << ',' << formaBonita << ",,,,OMITIDO\n";
                    csv.flush();
                    cout << left << setw(16) << nombre << " | " << right << setw(9) << n << " | "
                         << left << setw(13) << formaBonita << " | " << right << setw(12) << "-" << " | "
                         << setw(15) << "-" << " | " << setw(15) << "-" << " | OMITIDO\n";
                    continue;
                }
                vector<int> a = cargar(datosDir / (forma + "_" + to_string(n) + ".txt"));
                Stats s;
                auto t0 = chrono::high_resolution_clock::now();
                fn(a, s);
                auto t1 = chrono::high_resolution_clock::now();
                double tiempo = chrono::duration<double>(t1 - t0).count();
                string estado = ordenado(a) ? "OK" : "ERROR";
                csv << nombre << ',' << n << ',' << formaBonita << ',' << fixed << setprecision(6) << tiempo << ','
                    << s.comparaciones << ',' << s.cambios << ',' << estado << '\n';
                csv.flush();
                cout << left << setw(16) << nombre << " | " << right << setw(9) << n << " | "
                     << left << setw(13) << formaBonita << " | " << right << setw(12) << fixed << setprecision(6) << tiempo << " | "
                     << setw(15) << s.comparaciones << " | " << setw(15) << s.cambios << " | " << estado << '\n';
            }
        }
    }
    cout << "\nResultados guardados en Resultados/Benchmark_CPP.csv\n";
    if (!todos) cout << "Modo seguro activo. Usa --todos para forzar los casos cuadraticos gigantes.\n";
    return 0;
}
