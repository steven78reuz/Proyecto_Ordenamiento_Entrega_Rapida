const fs = require('fs');
const path = require('path');
const { performance } = require('perf_hooks');

const TAMANOS = [100, 1000, 10000, 100000, 1000000];
const FORMAS = ['ordenados', 'inversos', 'desordenados'];
const PESADOS = new Set(['bubbleSort', 'selectionSort', 'insertionSort']);
const LIMITE_SEGURO = 10000;
const EJECUTAR_TODOS = process.argv.includes('--todos');

const BASE = path.resolve(__dirname, '..');
const DATOS = path.join(BASE, 'Datos');
const RESULTADOS = path.join(BASE, 'Resultados');
fs.mkdirSync(RESULTADOS, { recursive: true });
const CSV_RUTA = path.join(RESULTADOS, 'Benchmark_JavaScript.csv');

function stats() { return { comparaciones: 0, cambios: 0 }; }

function bubbleSort(a, s) {
  for (let i = 0; i < a.length - 1; i++) {
    let cambio = false;
    for (let j = 0; j < a.length - 1 - i; j++) {
      s.comparaciones++;
      if (a[j] > a[j + 1]) {
        [a[j], a[j + 1]] = [a[j + 1], a[j]];
        s.cambios++; cambio = true;
      }
    }
    if (!cambio) break;
  }
}

function selectionSort(a, s) {
  for (let i = 0; i < a.length - 1; i++) {
    let min = i;
    for (let j = i + 1; j < a.length; j++) {
      s.comparaciones++;
      if (a[j] < a[min]) min = j;
    }
    if (min !== i) {
      [a[i], a[min]] = [a[min], a[i]];
      s.cambios++;
    }
  }
}

function insertionSort(a, s) {
  for (let i = 1; i < a.length; i++) {
    const clave = a[i];
    let j = i - 1;
    while (j >= 0) {
      s.comparaciones++;
      if (a[j] <= clave) break;
      a[j + 1] = a[j]; s.cambios++; j--;
    }
    a[j + 1] = clave; s.cambios++;
  }
}

function quickSort(a, s) {
  if (a.length < 2) return;
  const pila = [[0, a.length - 1]];
  while (pila.length) {
    const [izq, der] = pila.pop();
    let i = izq, j = der;
    const pivote = a[(izq + der) >> 1];
    while (i <= j) {
      while (i <= der) { s.comparaciones++; if (a[i] >= pivote) break; i++; }
      while (j >= izq) { s.comparaciones++; if (a[j] <= pivote) break; j--; }
      if (i <= j) {
        if (i !== j) { [a[i], a[j]] = [a[j], a[i]]; s.cambios++; }
        i++; j--;
      }
    }
    if (izq < j) pila.push([izq, j]);
    if (i < der) pila.push([i, der]);
  }
}

function mergeSort(a, s) {
  const n = a.length;
  const tmp = new Array(n);
  for (let ancho = 1; ancho < n; ) {
    for (let izq = 0; izq < n; izq += 2 * ancho) {
      const medio = Math.min(izq + ancho, n);
      const der = Math.min(izq + 2 * ancho, n);
      let i = izq, j = medio, k = izq;
      while (i < medio && j < der) {
        s.comparaciones++;
        tmp[k++] = a[i] <= a[j] ? a[i++] : a[j++];
        s.cambios++;
      }
      while (i < medio) { tmp[k++] = a[i++]; s.cambios++; }
      while (j < der) { tmp[k++] = a[j++]; s.cambios++; }
      for (k = izq; k < der; k++) { a[k] = tmp[k]; s.cambios++; }
    }
    if (ancho > Math.floor(n / 2)) break;
    ancho *= 2;
  }
}

function heapSort(a, s) {
  const n = a.length;
  function hundir(inicio, limite) {
    let i = inicio;
    while (true) {
      let mayor = i;
      const izq = 2 * i + 1, der = izq + 1;
      if (izq < limite) { s.comparaciones++; if (a[izq] > a[mayor]) mayor = izq; }
      if (der < limite) { s.comparaciones++; if (a[der] > a[mayor]) mayor = der; }
      if (mayor === i) return;
      [a[i], a[mayor]] = [a[mayor], a[i]]; s.cambios++; i = mayor;
    }
  }
  for (let i = Math.floor(n / 2) - 1; i >= 0; i--) hundir(i, n);
  for (let fin = n - 1; fin > 0; fin--) {
    [a[0], a[fin]] = [a[fin], a[0]]; s.cambios++;
    hundir(0, fin);
  }
}

function bucketSort(a, s) {
  const n = a.length;
  if (n < 2) return;
  let min = a[0], max = a[0];
  for (let i = 1; i < n; i++) {
    s.comparaciones += 2;
    if (a[i] < min) min = a[i];
    if (a[i] > max) max = a[i];
  }
  if (min === max) return;
  const cantidad = Math.min(n, Math.max(10, Math.floor(n / 100)));
  const cubetas = Array.from({ length: cantidad }, () => []);
  const rango = max - min + 1;
  for (const x of a) {
    const idx = Math.min(cantidad - 1, Math.floor(((x - min) * cantidad) / rango));
    cubetas[idx].push(x);
  }
  let pos = 0;
  for (const b of cubetas) {
    for (let i = 1; i < b.length; i++) {
      const clave = b[i];
      let j = i - 1;
      while (j >= 0) {
        s.comparaciones++;
        if (b[j] <= clave) break;
        b[j + 1] = b[j]; s.cambios++; j--;
      }
      b[j + 1] = clave; s.cambios++;
    }
    for (const x of b) { a[pos++] = x; s.cambios++; }
  }
}

function hashSort(a, s) {
  if (a.length < 2) return;
  let min = a[0], max = a[0];
  for (let i = 1; i < a.length; i++) {
    s.comparaciones += 2;
    if (a[i] < min) min = a[i];
    if (a[i] > max) max = a[i];
  }
  const conteo = new Int32Array(max - min + 1);
  for (const x of a) conteo[x - min]++;
  let pos = 0;
  for (let i = 0; i < conteo.length; i++) {
    for (let k = 0; k < conteo[i]; k++) { a[pos++] = i + min; s.cambios++; }
  }
}

function radixSort(a, s) {
  if (a.length < 2) return;
  let max = a[0];
  for (let i = 1; i < a.length; i++) { s.comparaciones++; if (a[i] > max) max = a[i]; }
  const salida = new Array(a.length);
  for (let exp = 1; Math.floor(max / exp) > 0; exp *= 10) {
    const conteo = new Int32Array(10);
    for (const x of a) conteo[Math.floor(x / exp) % 10]++;
    for (let i = 1; i < 10; i++) conteo[i] += conteo[i - 1];
    for (let i = a.length - 1; i >= 0; i--) {
      const d = Math.floor(a[i] / exp) % 10;
      salida[--conteo[d]] = a[i]; s.cambios++;
    }
    for (let i = 0; i < a.length; i++) { a[i] = salida[i]; s.cambios++; }
    if (exp > Math.floor(max / 10)) break;
  }
}

const ALGORITMOS = [
  ['bubbleSort', bubbleSort], ['selectionSort', selectionSort], ['quickSort', quickSort],
  ['mergeSort', mergeSort], ['heapSort', heapSort], ['bucketSort', bucketSort],
  ['radixSort', radixSort], ['insertionSort', insertionSort], ['hashSort', hashSort]
];

function cargar(ruta) {
  const texto = fs.readFileSync(ruta, 'utf8');
  const datos = [];
  let num = 0, leyendo = false;
  for (let i = 0; i < texto.length; i++) {
    const c = texto.charCodeAt(i);
    if (c >= 48 && c <= 57) { num = num * 10 + c - 48; leyendo = true; }
    else if (leyendo) { datos.push(num); num = 0; leyendo = false; }
  }
  if (leyendo) datos.push(num);
  return datos;
}

function estaOrdenado(a) {
  for (let i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
  return true;
}

function bonita(s) { return s[0].toUpperCase() + s.slice(1); }
function pad(v, n, left = false) { v = String(v); return left ? v.padStart(n) : v.padEnd(n); }

function main() {
  if (!fs.existsSync(path.join(DATOS, 'ordenados_100.txt'))) {
    console.log('Faltan los TXT. Ejecuta primero: python GeneradorDatos.py');
    return;
  }
  const fd = fs.openSync(CSV_RUTA, 'w');
  fs.writeSync(fd, 'Algoritmo,Volumen,Forma,TiempoSegundos,Comparaciones,Cambios,Estado\n');
  console.log(`${pad('Algoritmo',16)} | ${pad('Volumen',9,true)} | ${pad('Forma',13)} | ${pad('Tiempo',12,true)} | ${pad('Comparaciones',15,true)} | ${pad('Cambios',15,true)} | Estado`);
  console.log('-'.repeat(108));

  for (const [nombre, fn] of ALGORITMOS) {
    for (const n of TAMANOS) {
      for (const forma of FORMAS) {
        const fb = bonita(forma);
        if (PESADOS.has(nombre) && n > LIMITE_SEGURO && !EJECUTAR_TODOS) {
          fs.writeSync(fd, `${nombre},${n},${fb},,,,OMITIDO\n`);
          console.log(`${pad(nombre,16)} | ${pad(n,9,true)} | ${pad(fb,13)} | ${pad('-',12,true)} | ${pad('-',15,true)} | ${pad('-',15,true)} | OMITIDO`);
          continue;
        }
        const a = cargar(path.join(DATOS, `${forma}_${n}.txt`));
        const s = stats();
        const t0 = performance.now();
        fn(a, s);
        const tiempo = (performance.now() - t0) / 1000;
        const estado = estaOrdenado(a) ? 'OK' : 'ERROR';
        fs.writeSync(fd, `${nombre},${n},${fb},${tiempo.toFixed(6)},${s.comparaciones},${s.cambios},${estado}\n`);
        console.log(`${pad(nombre,16)} | ${pad(n,9,true)} | ${pad(fb,13)} | ${pad(tiempo.toFixed(6),12,true)} | ${pad(s.comparaciones,15,true)} | ${pad(s.cambios,15,true)} | ${estado}`);
      }
    }
  }
  fs.closeSync(fd);
  console.log(`\nResultados guardados en: ${CSV_RUTA}`);
  if (!EJECUTAR_TODOS) console.log('Modo seguro activo. Usa --todos para forzar los casos cuadraticos gigantes.');
}

if (require.main === module) main();

module.exports = { ALGORITMOS, bubbleSort, selectionSort, quickSort, mergeSort, heapSort, bucketSort, radixSort, insertionSort, hashSort };
