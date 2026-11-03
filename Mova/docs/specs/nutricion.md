# Spec — Módulo de Nutrición

**App:** Mova · **Curso:** DADM 2026-II, grupo 6 · **Estado:** aprobado por el equipo (2026-11-01)

Fuente: documento «Mova — Especificación de Nutrición y Búsqueda» (rev. 41). El módulo de Búsqueda está en [busqueda.md](busqueda.md).

## 1. Resumen

Nutrición es un registro de comidas guardado en Firestore, con una meta calórica adaptativa. Room funciona como caché local, así que la app sigue funcionando sin conexión y sincroniza al recuperarla.

Criterio de alcance: la propuesta del proyecto dice que el plan de Mova no es estático y se adapta al día del usuario. Por eso la meta de calorías se calcula desde el perfil del onboarding y sube cuando el usuario registra ejercicio. Un día por encima de la meta se muestra con un mensaje neutro, no como un error.

Prioridades:

- **P1 — Imprescindible:** sin esto el módulo no existe en la demo.
- **P2 — Recomendable:** lo que diferencia a Mova; se hace si P1 está cerrado.
- **P3 — Opcional/Premium:** solo si sobra tiempo o como funcionalidad bloqueada para usuarios Basic.

El esqueleto del repositorio ya reserva las clases del módulo (pantallas, ViewModels, entidades, DAO, repositorios y casos de uso), pero están vacías. Este spec define qué va dentro de cada una.

## 2. Funcionalidades

Nutrición tiene 9 funcionalidades: 6 imprescindibles, 2 recomendables y 1 opcional. Cada una se apoya en una pantalla que ya existe vacía en `presentation/screen/nutrition/`.

| # | Funcionalidad | Qué hace el usuario | Pantalla / clase | Mockup | Prioridad |
|---|---|---|---|---|---|
| N1 | Catálogo de comidas | Explora comidas con imagen, categoría, kcal y tiempo de preparación | `MealSuggestionScreen` (vista explorar), `meals.json` | p. 1 (Explore) | P1 |
| N2 | Detalle de comida | Ve los macros por porción, ajusta los gramos y pulsa «Add Meal» | `MealDetailScreen` | p. 1 (Avocado Dish) | P1 |
| N3 | Registro manual | Escribe un nombre, elige el tipo de comida e ingresa proteína, carbohidratos y grasa; las kcal se calculan solas | `AddMealScreen` | p. 8 | P1 |
| N4 | Confirmación | Ve «Food Added!» y vuelve a Mis comidas | `FoodAddedScreen` | p. 1 | P1 |
| N5 | Mis comidas | Elige un día en el carrusel, filtra por desayuno, almuerzo, cena o snack y elimina un registro | `MyMealsScreen` | p. 1 (My Meals) | P1 |
| N6 | Ingesta del día | Ve las kcal consumidas, las kcal restantes y los totales de macros del día | `CalorieIntakeScreen` | p. 13 | P1 |
| N7 | Meta adaptativa y gráfica mensual | La meta sube con las kcal quemadas en ejercicio; ve la curva de ingesta del mes | `CalorieIntakeScreen` + `SmoothAreaChart` | p. 13 | P2 |
| N8 | Sugerencias | Recibe comidas filtradas por su dieta y por los macros que le faltan | `MealSuggestionScreen` | p. 1 | P2 |
| N9 | Estadísticas mensuales | Ve el reparto de macros del mes, con una insignia «Premium» en una esquina | `CalorieStatsScreen` | p. 12 | P3 |

## 3. Reglas de negocio

- **Calorías desde macros.** `kcal = 4 × proteína + 4 × carbohidratos + 9 × grasa` (`CalculateKcalFromMacrosUseCase`). El usuario nunca escribe las kcal.
- **Porciones.** Las comidas del catálogo guardan los macros por porción base (por ejemplo 100 g). Al registrar 150 g, los macros se multiplican por 1,5.
- **Copia al registrar.** Un registro guarda una copia del nombre y de los macros, no solo el id del catálogo. Así el historial no cambia si el catálogo se edita.
- **Fecha del registro.** Se puede registrar en el día seleccionado en Mis comidas, incluidos días pasados, pero no en días futuros.
- **Validación del registro manual.** Nombre obligatorio de 2 a 60 caracteres; cada macro entre 0 y 300 g; al menos un macro mayor que 0.
- **Exceso de meta.** Si la ingesta supera la meta, el texto cambia a «Hoy superaste tu meta por X kcal; mañana se equilibra», en color neutro.
- **Sugerencias (P2).** Se descartan las comidas incompatibles con la dieta: una persona vegana solo ve comidas veganas y una vegetariana ve veganas y vegetarianas; carnívora o sin dieta (`null`) ve todas. Se ordenan por cercanía a lo que falta: primero proteína si falta más del 30 % de su meta y, si no, por kcal restantes. Se muestran 5.
- **Persistencia.** Cada registro se guarda en Room y en Firestore. Si no hay conexión, el registro aparece de inmediato en la app y se sube cuando vuelve la red (ver §5).

## 4. Datos

El módulo usa 2 tablas nuevas en Room (`meals` y `meal_logs`) y un catálogo semilla en JSON. Los registros de comida se guardan además en Firestore, en una subcolección por usuario; el catálogo es solo local.

### 4.1 Tabla `meals` — catálogo (solo lectura)

| Campo | Tipo | Ejemplo | Notas |
|---|---|---|---|
| `id` | String (PK) | `meal_avocado_toast` | estable, viene del JSON |
| `name` | String | Avocado Toast | |
| `category` | enum `MealCategory` | `SALAD_VEGETABLES` | Salad & Vegetables, Protein, Grains, Fruit, Snack, Drink |
| `suitableFor` | Set<MealType> | BREAKFAST, SNACK | para filtrar en sugerencias |
| `dietTags` | Set<Diet> | VEGAN, VEGETARIAN, CARNIVORE | dietas compatibles; reutiliza el enum `Diet` existente |
| `servingGrams` | Int | 100 | porción base |
| `proteinG` / `carbsG` / `fatG` | Double | 12 / 22 / 42 | por porción base |
| `kcal` | Int | 514 | calculado al sembrar con la fórmula 4/4/9; no se escribe a mano |
| `prepMinutes` | Int | 20 | «30min» del mockup |
| `imageRes` | String | `meal_avocado_toast` | drawable local en `res/drawable` |
| `tags` | List<String> | avocado, toast, high-fat | palabras extra para la búsqueda |

### 4.2 Tabla `meal_logs` — lo que el usuario comió (Room y Firestore)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | String (PK, UUID) | se genera en el teléfono; es el mismo id del documento en Firestore |
| `userId` | String | uid de Firebase Auth, para no mezclar cuentas en el mismo teléfono |
| `date` | LocalDate (ISO) | día al que pertenece; índice `(userId, date)` |
| `mealType` | enum `MealType` | BREAKFAST, LUNCH, DINNER, SNACK |
| `source` | enum `MealSource` | CATALOG, MANUAL |
| `mealId` | String? | id del catálogo; null si es manual |
| `name` | String | copia |
| `grams` | Int? | porción registrada; null si es manual |
| `proteinG` / `carbsG` / `fatG` | Double | ya multiplicados por la porción |
| `kcal` | Int | calculado |
| `createdAt` | Instant | hora del registro |
| `updatedAt` | Instant | última modificación; decide qué versión gana al sincronizar |
| `deleted` | Boolean | borrado lógico, para que el borrado también llegue a Firestore |
| `syncState` | enum `SyncState` | PENDING, SYNCED; solo existe en Room, no se sube |

### 4.3 Catálogo semilla `assets/seed/meals.json`

- 40 comidas al inicio: 10 por tipo de comida, con al menos 12 veganas y 20 vegetarianas para que las sugerencias funcionen con cualquier dieta.
- Macros tomados de tablas públicas (USDA FoodData Central o la Tabla de Composición de Alimentos Colombianos del ICBF) y redondeados a 1 decimal.
- Imágenes libres de derechos (Unsplash o Pexels), comprimidas en WebP de máximo 80 KB cada una.
- `DatabaseSeeder` carga el JSON la primera vez que se crea la base.

### 4.4 Modelos de dominio

- `Macros(proteinG, carbsG, fatG)` con propiedad calculada `kcal`.
- `Meal`, `MealLog`, `MealType`, `MealSource`, `MealCategory`.
- `DailyIntake(date, consumed: Macros, consumedKcal, goalKcal, burnedKcal, remainingKcal, byMealType)`.

### 4.5 Meta calórica diaria

Se usa la ecuación de Mifflin–St Jeor. El perfil actual no tiene altura, así que hay que añadirla (D2); mientras tanto se usa 170 cm para hombres y 160 cm para mujeres.

| Valor del perfil | Factor |
|---|---|
| Nivel BEGINNER / NOVICE / INTERMEDIATE / ADVANCED / ATHLETE | 1,2 / 1,375 / 1,55 / 1,725 / 1,9 |
| Objetivo LOSE_WEIGHT | 0,85 |
| Objetivo GAIN_MUSCLE | 1,10 |
| Objetivo IMPROVE_ENDURANCE, AI_COACH, EXPLORE | 1,00 |

Meta = metabolismo basal × factor de nivel × factor de objetivo + kcal quemadas del día.

Desde el merge #4, el onboarding rellena con valores fijos los datos que el usuario no eligió (19 años, 62 kg, MALE, INTERMEDIATE, AI_COACH, VEGAN). Por eso el perfil casi siempre llega completo y los valores por defecto de Nutrición (25 años, 70 kg) solo se aplican cuando no hay perfil, por ejemplo si falló el guardado en Firestore. La fórmula no cambia.

- La meta se redondea a múltiplos de 10 y nunca baja de 1200 kcal.
- Las metas de macros salen de la meta en kcal: proteína 1,6 g/kg (2,0 g/kg si el objetivo es ganar músculo), grasa 25 % de las kcal y carbohidratos el resto.
- El término de kcal quemadas es la parte P2 (N7); en P1 vale 0.

### 4.6 Base de datos local y dependencias

El proyecto no usaba Room: `MovaDatabase` era una clase común y `UserDao` y `UserProfileDao` son simulaciones que devuelven valores vacíos. Ya están agregadas en `gradle/libs.versions.toml` y `app/build.gradle.kts` estas dependencias:

| Librería | Versión | Uso |
|---|---|---|
| Room (`room-runtime`, `room-ktx`, `room-compiler` vía KSP) | 2.8.5 | tablas `meals` y `meal_logs` |
| KSP (`com.google.devtools.ksp`) | 2.3.13 | procesador de anotaciones de Room |
| WorkManager (`work-runtime-ktx`) | 2.12.0 | `SyncMealLogsWorker` |
| `room-testing`, `work-testing` | las mismas | pruebas |

El esquema de Room se exporta en `app/schemas` (argumento de KSP `room.schemaLocation`).

Cómo se integra:

- `MovaDatabase` pasa a ser `@Database(entities = [MealEntity::class, MealLogEntity::class], version = 1, exportSchema = true)`, con una instancia única `MovaDatabase.getInstance(context)`. Es la versión 1, así que no hace falta migración.
- `userDao()` y `userProfileDao()` se conservan como métodos concretos que devuelven las simulaciones actuales, para no romper Perfil. Si Perfil migra a Room, agrega sus entidades y sube la versión con una migración.
- `DatabaseSeeder` es un `RoomDatabase.Callback` que carga `meals.json` en `onCreate`.
- El proyecto no usa Hilt: los ViewModels se crean con un `ViewModelProvider.Factory` propio, como `ProfileViewModelFactory` y `OnboardingViewModelFactory`. Nutrición sigue el mismo patrón (`NutritionViewModelFactory`).

## 5. Almacenamiento en Firestore

Cada registro es un documento en `users/{uid}/meal_logs/{id}`, con los mismos campos de la tabla salvo `syncState`. Las fechas se guardan como `Timestamp` y `date` como texto ISO (`2026-11-01`) para poder filtrar por rango. Room es lo que leen las pantallas; Firestore es la copia que persiste entre dispositivos.

1. **Registrar.** `LogMealUseCase` inserta en Room con `syncState = PENDING` y escribe el documento en Firestore. Al confirmarse la escritura, se marca `SYNCED`.
2. **Sin conexión.** Si la escritura falla, el registro queda `PENDING` y un `SyncMealLogsWorker` (WorkManager, con restricción de red) sube todos los pendientes cuando vuelve la conexión.
3. **Eliminar.** Se marca `deleted = true` y `PENDING`; tras subirse, el registro se borra de Room. En Firestore el documento se elimina.
4. **Descargar.** Al iniciar sesión en un teléfono sin datos locales, se descargan los registros del usuario de Firestore a Room. Después se escucha la subcolección con un listener para recibir cambios de otro dispositivo.
5. **Conflictos.** Los registros no se editan, solo se crean o borran, así que los conflictos son raros. Si ocurre uno, gana el `updatedAt` más reciente.
6. **Cerrar sesión.** Se intenta subir los pendientes y luego se borran de Room los registros `SYNCED` de ese usuario. Si no hay conexión, los `PENDING` se conservan en Room: no se mezclan con otras cuentas porque todas las consultas filtran por `userId`, y `SyncMealLogsWorker` solo sube los del usuario con sesión activa (las reglas de seguridad lo exigen). Se suben la próxima vez que ese usuario inicie sesión en el teléfono. El cierre de sesión nunca se bloquea ni pierde datos, y no hace falta cambiar los contratos de §6.

Reglas de seguridad propuestas:

```
match /users/{uid}/meal_logs/{logId} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
}
```

## 6. Contratos públicos

Otros módulos solo deben usar estos contratos; las entidades, los DAO y las pantallas son internos y pueden cambiar sin avisar. Las firmas quedan congeladas (D1).

```kotlin
interface MealRepository {
    fun observeCatalog(): Flow<List<Meal>>
    fun observeMeal(id: String): Flow<Meal?>
    fun observeLogs(userId: String, date: LocalDate): Flow<List<MealLog>>
    fun observeLogs(userId: String, from: LocalDate, to: LocalDate): Flow<List<MealLog>>
    suspend fun log(entry: MealLog): Result<String>
    suspend fun delete(logId: String): Result<Unit>
}

// Ingesta, meta y restante de un día
class ObserveDailyIntakeUseCase { operator fun invoke(date: LocalDate): Flow<DailyIntake> }

// Registrar desde cualquier pantalla (catálogo, manual, Coach)
class LogMealUseCase { suspend operator fun invoke(entry: MealLog): Result<String> }

// kcal = 4P + 4C + 9G
class CalculateKcalFromMacrosUseCase { operator fun invoke(macros: Macros): Int }
```

### Rutas expuestas en `Routes.kt`

| Ruta | Destino | Quién la usa |
|---|---|---|
| `nutrition/my_meals?date={iso}` | Mis comidas del día | Home (DietCard), Notificaciones |
| `nutrition/meal/{mealId}` | Detalle de comida | Home, Búsqueda, Coach |
| `nutrition/add?type={mealType}` | Registro manual | Mis comidas, Coach |
| `nutrition/intake` | Ingesta del día | Home (CalorieGoalCard) |

## 7. Dependencias

La dependencia más crítica es con Actividad / Entrenamiento, porque la meta adaptativa (N7) necesita las kcal quemadas del día.

### Módulos que consumen mis datos

| Módulo | Qué usa | Cómo lo obtiene | Para qué |
|---|---|---|---|
| Home | kcal consumidas, meta, restante; una comida sugerida | `ObserveDailyIntakeUseCase`, `MealRepository.observeCatalog()` | `CalorieGoalCard` («2500 Kilo calories»), `DietCard` («Salad & Egg»), `ObserveHomeSummaryUseCase` |
| Progreso / Uplift Score | % de días dentro de la meta en la semana | `MealRepository.observeLogs(userId, from, to)` | componente de adherencia a nutrición en `ComputeUpliftScoreUseCase` |
| Plan diario | ingesta del día y macros faltantes | `ObserveDailyIntakeUseCase` | `AdaptPlanUseCase` («entreno largo → +proteína»), `GenerateDailyPlanUseCase` |
| Notificaciones | proteína faltante; día sin registros | `ObserveDailyIntakeUseCase` | «Nutrition Upgrade — Take 87g of protein!» (p. 24), `GenerateNotificationsUseCase` |
| Coach IA | resumen de comidas recientes; registrar una comida sugerida | `observeLogs`, `LogMealUseCase` | contexto del chat y acción «agregar comida» |
| Búsqueda | catálogo de comidas | `MealRepository.observeCatalog()` | resultados de tipo comida |

### Datos que necesito de otros módulos

| Módulo | Qué necesito | Para qué | Si no está listo |
|---|---|---|---|
| Auth | userId de la sesión actual | separar registros por usuario | no se puede registrar sin sesión; en pruebas se usa un id fijo `"local"` |
| Onboarding / Perfil | edad, peso, género, nivel, objetivo, dieta (ya existen en `UserProfile`); altura (§13) | meta calórica y filtro de sugerencias | valores por defecto: 25 años, 70 kg, 170 cm, INTERMEDIATE, EXPLORE |

Nota sobre la dieta: desde el merge #4, si el usuario no elige dieta en el onboarding queda como `VEGAN`, y solo vería sugerencias veganas (N8). Se cambia para que quede en `null` (ver §13.2), y N8 trata `null` como «sin restricción»: muestra todas las comidas.
| Actividad / Entrenamiento | kcal quemadas por día: `fun observeBurnedKcal(userId, date): Flow<Int>` | meta adaptativa (N7) | N7 queda en 0 kcal quemadas y la meta es fija |
| Premium / Perfil | nada | — | N9 no se bloquea en el prototipo; solo muestra la insignia «Premium» (ver D6) |

## 8. Qué se implementará

Las piezas marcadas P2 y P3 se hacen después de cerrar todas las P1.

- **Dominio:** `Macros`, `Meal`, `MealLog`, `MealType`, `MealSource`, `MealCategory`, `DailyIntake`; interfaz `MealRepository`; casos de uso `CalculateKcalFromMacrosUseCase`, `LogMealUseCase`, `ObserveDailyIntakeUseCase`.
- **Datos locales:** entidades y DAO de `meals` y `meal_logs`, `MovaDatabase` como base Room versión 1 (§4.6), `meals.json` con 40 comidas e imágenes, carga en `DatabaseSeeder`.
- **Datos remotos:** `FirestoreMealLogDataSource` sobre `users/{uid}/meal_logs`, `SyncMealLogsWorker`, descarga inicial al iniciar sesión, reglas de seguridad.
- **Repositorio:** `MealRepositoryImpl` (Room + Firestore).
- **Pantallas:** catálogo (N1), detalle (N2), registro manual (N3), confirmación (N4), Mis comidas (N5), ingesta del día (N6); después meta adaptativa y gráfica (N7, P2), sugerencias (N8, P2) y estadísticas mensuales con insignia Premium (N9, P3).
- **Altura del usuario (D2):** campo `heightCm` y paso de altura en el onboarding (ver §13).
- **Pruebas:** DAO de `meal_logs`, cálculo de kcal y de la meta calórica, y sincronización de pendientes.

**Criterio de terminado** para cada funcionalidad: funciona sin conexión, los datos aparecen en Firestore al volver la red, tiene estados de carga, vacío y error, y sus casos de uso tienen pruebas unitarias.

## 9. Fuera de alcance

- Escáner de códigos de barras y bases de datos externas de alimentos (Open Food Facts, Edamam).
- Micronutrientes (vitaminas, sodio, fibra) y planes de comida semanales.
- Recetas paso a paso; solo se muestra el tiempo de preparación.
- Escaneo de comidas con IA (pestaña «AI Scan» de la p. 8 y pantalla «Analysis» de la p. 1).

## 10. Errores en los mockups que se corregirán al implementar

| Pantalla | Mockup | Problema | Corrección |
|---|---|---|---|
| Add New Meal | p. 8 | El tercer control dice «Total Protein» otra vez | Debe ser «Total Fat» |
| Add New Meal | p. 8 | Los controles van de 10 a 30 g | Rango de 0 a 300 g con campo numérico; un control deslizante es impreciso |
| Add New Meal | p. 8 | Tiene pestañas Manual / AI Scan, dice «Manaul» y falta «Lunch» | Sin pestañas, solo registro manual, con cuatro tipos (Breakfast, Lunch, Dinner, Snack) |
| Avocado Dish | p. 1 | 457 kcal con 22 g P, 1,8 g C y 19 g G; esos macros dan 266 kcal | kcal siempre calculadas desde los macros |
| Calorie Stats | p. 12 | «Macro» aparece como un cuarto nutriente y los porcentajes suman 85 % | Tres barras (proteína, carbohidratos, grasa) que suman 100 % |
| Calorie Intake | p. 13 | 1745 kcal consumidas y «158 left» no cuadran con la meta de 2500 del Home | Ambos valores salen de `ObserveDailyIntakeUseCase` |

## 11. Decisiones aprobadas

| # | Decisión | Afecta a |
|---|---|---|
| D1 | Congelar las firmas de `MealRepository`, `ObserveDailyIntakeUseCase`, `LogMealUseCase` y `SearchRepository` | Home, Progreso, Plan, Notificaciones, Coach |
| D2 | Añadir `heightCm` a `UserProfile` y un paso de altura al onboarding | Onboarding / Perfil |
| D3 | Actividad expone `observeBurnedKcal(userId, date): Flow<Int>` | Actividad / Entrenamiento |
| D5 | Los registros de comida se guardan en Firestore (`users/{uid}/meal_logs`) con Room como caché local, y se publican las reglas de seguridad propuestas | Todos |
| D6 | Las estadísticas mensuales (N9) son una función Premium y el escaneo con IA queda fuera del alcance. En el prototipo N9 no se bloquea: solo se marca visualmente con una insignia «Premium» en una esquina | Premium / Perfil |
| D8 | Catálogo de 40 comidas con imágenes WebP locales (unos 3 MB en el APK) | Todos |

D4 y D7 corresponden a Búsqueda.

## 12. Limpieza del esqueleto

Como el escaneo con IA (D6) y el plan semanal quedaron fuera, estos stubs no se usan y ningún archivo los referencia: `data/remote/NutritionVisionApi.kt`, `data/remote/dto/NutritionScanDto.kt`, `domain/repository/NutritionScanRepository.kt`, `data/repository/NutritionScanRepositoryImpl.kt`, `presentation/screen/nutrition/MealScanScreen.kt` (y su ViewModel) y `domain/model/PlannedMeal.kt`. Se eliminan al empezar la implementación.

## 13. Altura del usuario (D2)

La meta calórica (§4.5) necesita la altura, pero `UserProfile` no la tiene. Lo implementa el responsable de Nutrición (confirmado por el equipo), aunque toca código de Onboarding y Perfil. Sigue el mismo patrón que el peso tras el merge #4.

### 13.1 Modelo y persistencia

| Archivo | Cambio |
|---|---|
| `domain/model/UserProfile.kt` | `val heightCm: Int? = null` |
| `domain/model/OnboardingDraft.kt` | `val heightCm: Int? = null` |
| `data/remote/FirestoreAssessmentDataSource.kt` | guardar `"heightCm" to draft.heightCm` |
| `domain/usecase/SaveUserProfileUseCase.kt` | guardar `"heightCm" to profile.heightCm` |
| `data/remote/FirestoreUserDataSource.kt` | leer `heightCm = getLong("heightCm")?.toInt()` |
| `data/entity/UserProfileEntity.kt` | `val heightCm: Int? = null` |

En Firestore el campo vive en el mismo documento del usuario (`users/{uid}`), junto a `weightKg`. No hace falta migración: los documentos sin el campo se leen como `null`.

### 13.2 Onboarding

El onboarding ya no valida campos: al terminar, `Complete` rellena lo que falte con valores fijos. La altura sigue esa misma regla.

- **Evento:** `OnboardingEvent.SetHeight(val heightCm: Int)` y su rama en `OnboardingViewModel.onEvent` (`draft.copy(heightCm = event.heightCm)`).
- **Valor por defecto en `Complete`:** `heightCm = draft.heightCm ?: alturaPorDefecto(gender)`, con 170 cm (MALE), 160 cm (FEMALE) y 165 cm (PREFER_NOT_TO_SAY), los mismos valores de §13.4. Se calcula después de rellenar el género.
- **Dieta sin elegir:** en el mismo cambio, `Complete` deja de asignar `Diet.VEGAN` por defecto y conserva `draft.diet` (puede quedar `null`). Así, un usuario que no eligió dieta ve todas las comidas en N8 en lugar de solo las veganas. `UserProfile.diet` ya es nullable, así que no cambia el modelo.
- **Pantalla:** `HeightStep.kt` nuevo, basado en `WeightStep.kt`: mismo `RulerPicker` y selector de unidad (cm / ft-in en lugar de kg / lb), rango de 100 a 250 cm, valor inicial 170 cm. Título «What's your height?».
- **Orden:** el paso va justo después del peso. En `AssessmentScreen` el `when` queda así: 0 edad, 1 peso, **2 altura**, 3 nivel, 4 género, 5 voz, 6 objetivo, 7 avatar y `else` dieta.
- **Numeración visible:** cada paso pasa `stepIndex` a `AssessmentStep(...)`, y hoy los valores no son consecutivos (`AgeStep` y `DietStep` usan 1; `GoalStep` y `AvatarStep` usan 6), mientras que `totalSteps` vale 6 aunque hay 8 pasos. Con la altura quedan 9 pasos: se numeran de 1 a 9 en el orden anterior (edad 1, peso 2, altura 3, nivel 4, género 5, voz 6, objetivo 7, avatar 8, dieta 9) y se pasa `totalSteps = 9`.

### 13.3 Perfil

- `PersonalInfoScreen` agrega un campo «Height» junto a «Current Weight», con el mismo formato (`"170 cm"`), y `ProfileViewModel.updatePersonalInfo(...)` recibe un parámetro `heightCm: Int?`.
- Esto permite completar la altura a los usuarios que hicieron el onboarding antes de este cambio, que la tienen en `null`.

### 13.4 Uso en Nutrición

- Si `heightCm` existe, se usa en la fórmula sin importar el género.
- Si es `null` (usuarios anteriores a este cambio o perfil no disponible), se usa el valor por defecto: 170 cm (MALE), 160 cm (FEMALE) y 165 cm (PREFER_NOT_TO_SAY o sin género).
- La constante de género de Mifflin–St Jeor es +5 (MALE) y −161 (FEMALE). Para PREFER_NOT_TO_SAY o sin género se usa el promedio, −78.

Los valores de PREFER_NOT_TO_SAY (165 cm y −78) fueron confirmados por el equipo.

### 13.5 Pruebas

- `OnboardingViewModel`: `Complete` guarda la altura elegida y, si no se eligió, la altura por defecto según el género; sin dieta elegida guarda `diet = null`.
- Lectura de un usuario sin `heightCm` devuelve `null` sin errores.
- Meta calórica con altura real, sin altura y con cada género.

## 14. Pendientes con otros módulos

No hay pendientes. Resueltos: D2 lo implementa Nutrición (§13); valores de PREFER_NOT_TO_SAY confirmados (§13.4); Premium solo se marca visualmente (D6); dieta por defecto en `null` (§13.2).
