# Spec — Módulo de Búsqueda

**App:** Mova · **Curso:** DADM 2026-II, grupo 6 · **Estado:** aprobado por el equipo (2026-11-01)

Fuente: documento «Mova — Especificación de Nutrición y Búsqueda» (rev. 41). El módulo de Nutrición está en [nutricion.md](nutricion.md).

## 1. Resumen

Búsqueda es una pantalla global y offline que consulta comidas y entrenamientos en Room. Se abre desde la barra «Search our food database…» del Home y vive en `SearchScreen` + `SearchViewModel` (`presentation/screen/home/` y `presentation/viewmodel/home/`).

Prioridades:

- **P1 — Imprescindible:** sin esto el módulo no existe en la demo.
- **P2 — Recomendable:** se hace si P1 está cerrado.
- **P3 — Opcional:** solo si sobra tiempo.

## 2. Funcionalidades

| # | Funcionalidad | Qué hace el usuario | Mockup | Prioridad |
|---|---|---|---|---|
| B1 | Búsqueda por texto | Escribe y ve los resultados mientras teclea | p. 21 | P1 |
| B2 | Filtros por tipo | Elige Todo, Comidas o Entrenamientos con chips | p. 18–21 | P1 |
| B3 | Resultados con coincidencia | Ve un ícono por tipo, el nombre, un subtítulo y el «% Match» | p. 21 | P1 |
| B4 | Estados de pantalla | Ve cargando, sin resultados o error | p. 18, 20, 22 | P1 |
| B5 | Abrir resultado | Toca un resultado y va al detalle de la comida o del entrenamiento | — | P1 |
| B6 | Sugerencias al escribir | Ve hasta 6 nombres del catálogo que empiezan por lo que escribió | p. 19 | P2 |
| B7 | Búsquedas recientes | Ve sus últimas 8 búsquedas cuando el campo está vacío y puede borrarlas | — | P2 |
| B8 | Añadir comida desde el resultado | Registra una comida en el día sin abrir el detalle | — | P3 |

## 3. Reglas de negocio

- **Espera antes de buscar.** La consulta se lanza 300 ms después de la última tecla (debounce). Con menos de 2 caracteres no se busca.
- **Normalización.** Se compara sin mayúsculas ni tildes: «piña» encuentra «Pina» y viceversa.
- **Puntaje de coincidencia (0–100).** Se ordena por puntaje y luego alfabéticamente.

  | Puntaje | Regla |
  |---|---|
  | 100 | nombre exacto |
  | 85 | el nombre empieza por la consulta |
  | 70 | una palabra del nombre empieza por la consulta |
  | 55 | el nombre contiene la consulta |
  | 40 | solo la categoría o las etiquetas coinciden |

- **Consultas de varias palabras.** Primero se evalúa la consulta completa con la tabla anterior. Si no coincide, cada palabra se evalúa por separado: todas deben coincidir con el nombre, la categoría o las etiquetas, y el puntaje es el promedio de los puntajes de las palabras, redondeado. Si alguna palabra no coincide, el elemento no aparece. Ejemplo: «toast avocado» encuentra «Avocado Toast» con (70 + 85) / 2 = 78.
- **Límite.** Máximo 30 resultados por consulta.
- **Sin resultados.** Se muestra el estado «Not Found» de la p. 20 con la consulta escrita y 3 sugerencias: los 3 primeros elementos, en orden alfabético, del catálogo que corresponde al filtro activo (con «Todo», comidas y entrenamientos mezclados). Si ese catálogo está vacío, no se muestran sugerencias.
- **Sugerencias al escribir (B6).** Son nombres de comidas y de entrenamientos que respetan el filtro activo y cuyo nombre normalizado empieza por la consulta. Se muestran hasta 6, en orden alfabético. Al tocar una, se rellena el campo y se busca.
- **Recientes (P2).** Se guarda la consulta solo cuando el usuario abre un resultado o pulsa buscar. Sin duplicados; la más nueva arriba.
- **Añadir desde el resultado (B8).** Botón «+» solo en los resultados de tipo comida. Registra con `LogMealUseCase` la porción base (`servingGrams`), con fecha de hoy y `source = CATALOG`. El tipo de comida es el primero de `suitableFor` en el orden BREAKFAST, LUNCH, DINNER, SNACK. Se muestra un snackbar «Añadido a {tipo}» con la acción «Deshacer», que borra el registro con `MealRepository.delete`. Para cambiar la porción o el tipo, el usuario abre el detalle (B5).

## 4. Datos

### 4.1 Qué se busca

| Fuente | Contrato | Campos usados |
|---|---|---|
| Catálogo de comidas (Nutrición, tabla `meals`) | `MealRepository.observeCatalog()` | `name`, `category`, `tags`, `kcal`, `imageRes` |
| Catálogo de rutinas (Entrenamiento) | `observeWorkouts(): Flow<List<Workout>>` | nombre, categoría y duración |

Los campos de la tabla `meals` están definidos en [nutricion.md §4.1](nutricion.md#41-tabla-meals--catálogo-solo-lectura).

### 4.2 Preferencias en DataStore

- `recent_searches`: lista de hasta 8 cadenas (B7).

### 4.3 Modelo de dominio

- `SearchResult(id, type: MEAL | WORKOUT, title, subtitle, score, imageRes)`.

## 5. Contratos públicos

La firma de `SearchRepository` queda congelada (D1).

```kotlin
interface SearchRepository {
    fun search(query: String, types: Set<SearchResult.Type>): Flow<List<SearchResult>>
    fun suggestions(prefix: String): Flow<List<String>>
}

class SearchCatalogUseCase {
    operator fun invoke(query: String, types: Set<SearchResult.Type>): Flow<List<SearchResult>>
}
```

### Ruta expuesta en `Routes.kt`

| Ruta | Destino | Quién la usa |
|---|---|---|
| `search?q={texto}` | Búsqueda | Home (barra de búsqueda) |

## 6. Dependencias

### Módulos que consumen Búsqueda

| Módulo | Qué usa | Cómo lo obtiene | Para qué |
|---|---|---|---|
| Home (barra de búsqueda) | pantalla de búsqueda | ruta `search?q=` | entrada a Búsqueda |

### Datos que necesito de otros módulos

| Módulo | Qué necesito | Para qué | Si no está listo |
|---|---|---|---|
| Nutrición | `MealRepository.observeCatalog()`, ruta `nutrition/meal/{mealId}` , `LogMealUseCase` y `MealRepository.delete` (B8) | resultados de tipo comida, abrir el detalle y añadir desde el resultado | — (mismo responsable) |
| Entrenamiento | catálogo de rutinas: `fun observeWorkouts(): Flow<List<Workout>>` con nombre, categoría y duración (D4) | resultados de tipo Workout | Búsqueda arranca solo con comidas (ver §12) |

B5 también necesita una ruta al detalle de un entrenamiento; queda pendiente junto con D4 (ver §12).

## 7. Qué se implementará

Las piezas marcadas P2 y P3 se hacen después de cerrar todas las P1.

- **Dominio:** `SearchResult`; interfaz `SearchRepository`; caso de uso `SearchCatalogUseCase`.
- **Repositorio:** `SearchRepositoryImpl` (Room).
- **Pantalla de Búsqueda:** texto, filtros, resultados con % Match, estados y navegación (B1–B5); después sugerencias al escribir y recientes (B6–B7, P2) y añadir desde el resultado (B8, P3).
- **Pruebas:** puntaje de búsqueda.

**Criterio de terminado** para cada funcionalidad: funciona sin conexión, tiene estados de carga, vacío y error, y sus casos de uso tienen pruebas unitarias.

## 8. Fuera de alcance

- Chip «Community» de la búsqueda: ningún módulo produce contenido de comunidad.
- Búsqueda remota o en Firestore; todo es local (ver §9).

## 9. Por qué la búsqueda no se hace en Firestore

Firestore no tiene búsqueda de texto: solo admite igualdad, rangos, prefijo sobre un campo y un `array-contains` por consulta. No puede encontrar una palabra a mitad del nombre, ignorar tildes sin un campo extra, calcular el «% Match» ni buscar en comidas y entrenamientos a la vez. La alternativa oficial (extensiones de Algolia, Typesense o Elastic) requiere el plan de pago Blaze. Además, los catálogos son estáticos y ya están en el teléfono: buscar en local es instantáneo, funciona sin conexión y no gasta lecturas facturables.

## 10. Errores en los mockups que se corregirán al implementar

| Pantalla | Mockup | Problema | Corrección |
|---|---|---|---|
| Search | p. 21 | Resultados como «Coach Farness» con barra de progreso y casilla, sin significado | Fila uniforme: ícono por tipo, nombre, subtítulo, % Match |
| Not Found | p. 20, 22 | Texto «Whoops Coach U can'yt fin this page» | «No encontramos resultados para "{consulta}"» |

## 11. Decisiones aprobadas

| # | Decisión | Afecta a |
|---|---|---|
| D1 | Congelar las firmas de `MealRepository`, `ObserveDailyIntakeUseCase`, `LogMealUseCase` y `SearchRepository` | Home, Progreso, Plan, Notificaciones, Coach |
| D4 | Entrenamiento expone `observeWorkouts()` para incluir rutinas en Búsqueda | Entrenamiento |
| D7 | Se quita el chip «Community» de la búsqueda | Diseño |

D2, D3, D5, D6 y D8 corresponden a Nutrición.

## 12. Entrenamientos: pendiente con Entrenamiento

El modelo `Workout` y la ruta a `WorkoutDetailScreen` todavía no existen, así que **Búsqueda se implementa solo con comidas** y los entrenamientos se agregan cuando Entrenamiento los entregue.

Mientras tanto:

- `SearchRepositoryImpl` toma los entrenamientos de una fuente que devuelve una lista vacía (`flowOf(emptyList())`). Cuando exista `observeWorkouts()`, solo se cambia esa fuente.
- Los chips muestran solo «Todo» y «Comidas»; el chip «Entrenamientos» aparece cuando la fuente esté conectada.
- `SearchResult.Type.WORKOUT` y la firma de `SearchRepository` se mantienen, así que el contrato congelado (D1) no cambia.
- B5 solo navega al detalle de comidas.

Lo mínimo que necesita Búsqueda cuando se defina (propuesta para Entrenamiento):

| Pendiente | Propuesta |
|---|---|
| Modelo `Workout` | `id`, `name`, `category: WorkoutCategory`, `durationMin: Int`, `imageRes: String?` |
| Ruta al detalle | `workout/detail/{workoutId}` |
