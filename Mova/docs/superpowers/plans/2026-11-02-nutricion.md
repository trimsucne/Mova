# Módulo de Nutrición — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar el registro de comidas de Mova (catálogo, registro manual, Mis comidas, ingesta del día, meta adaptativa, sugerencias y estadísticas) con Room como caché y Firestore como copia remota, más la altura del usuario (D2).

**Architecture:** Clean Architecture ya presente en el repo: `domain` (modelos, contratos, casos de uso en Kotlin puro), `data` (Room, Firestore, mappers, repositorio), `presentation` (Compose + ViewModels con `StateFlow`). Las pantallas solo leen Room; el repositorio sube a Firestore en segundo plano y, si falla, `SyncMealLogsWorker` reintenta. No hay Hilt: las dependencias se arman a mano en `NutritionContainer` y los ViewModels se crean con un `ViewModelProvider.Factory`, igual que el resto del proyecto.

**Tech Stack:** Kotlin 2.4, Jetpack Compose (Material 3), Room 2.8.5 + KSP 2.3.13, WorkManager 2.12.0, Firebase Auth + Firestore, DataStore, JUnit 4, kotlinx-coroutines-test.

**Spec:** `docs/specs/nutricion.md` (léelo completo antes de empezar; este plan cita sus secciones como §N).

## Global Constraints

- Todas las rutas de este plan son relativas a la carpeta del proyecto Gradle `Mova/` (donde está `gradlew`). Abreviaturas:
  - `PKG` = `app/src/main/java/co/edu/unal/dadm/mova`
  - `TEST` = `app/src/test/java/co/edu/unal/dadm/mova`
  - `ATEST` = `app/src/androidTest/java/co/edu/unal/dadm/mova`
- Para compilar hace falta `app/google-services.json` (no está en el repo; pídelo al equipo) y el SDK de Android (`local.properties` con `sdk.dir=...` o la variable `ANDROID_HOME`).
- Pruebas unitarias: `./gradlew :app:testDebugUnitTest --tests "<clase>"`. Pruebas instrumentadas (necesitan emulador o teléfono): `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=<clase>`.
- `minSdk = 24`: `java.time` (`LocalDate`, `Instant`) requiere core library desugaring (Task 1).
- kcal siempre = `4 × proteína + 4 × carbohidratos + 9 × grasa`, redondeado al entero más cercano. El usuario nunca escribe kcal.
- `Result` es `co.edu.unal.dadm.mova.core.common.Result` (`Success`, `Error`, `Loading`), no `kotlin.Result`.
- Firmas congeladas (D1), copiadas de §6: `MealRepository`, `ObserveDailyIntakeUseCase`, `LogMealUseCase`, `CalculateKcalFromMacrosUseCase`. No cambiarlas.
- Firestore: `users/{uid}/meal_logs/{id}`; `date` como texto ISO; `createdAt`/`updatedAt` como `Timestamp`; `syncState` nunca se sube.
- Usuario sin sesión: `userId = "local"` (`MealLog.LOCAL_USER_ID`); sus registros nunca se suben.
- Rutas de §6: se respetan las cuatro aprobadas y se les agrega un parámetro opcional `date` (`nutrition/meal/{mealId}?date=`, `nutrition/add?type=&date=`) para registrar en el día elegido en Mis comidas (§3). Quien navegue sin `date` obtiene el comportamiento aprobado (hoy). Además se agregan rutas internas: `nutrition/added`, `nutrition/explore` y `nutrition/stats`.
- Textos de la UI: los que el spec da literalmente se copian tal cual (por ejemplo «Hoy superaste tu meta por X kcal; mañana se equilibra», «Food Added!»); el resto en inglés, como las pantallas existentes.
- No eliminar los stubs del escaneo con IA (`MealScan*`, `NutritionScan*`, `NutritionVisionApi`): el equipo puede implementarlo después.
- Mensajes de commit con el estilo del repo: `[Add] ...`, `[Update] ...`, `[Fix] ...`.

## Mapa de archivos

| Archivo | Responsabilidad |
|---|---|
| `PKG/domain/model/Macros.kt`, `MealType.kt`, `MealSource.kt`, `MealCategory.kt`, `Meal.kt`, `MealLog.kt`, `DailyIntake.kt` | Modelos de dominio |
| `PKG/domain/model/BodyDefaults.kt` (nuevo) | Altura por defecto según género (§13.4) |
| `PKG/domain/repository/MealRepository.kt` | Contrato público (§6) |
| `PKG/domain/repository/BurnedKcalProvider.kt` (nuevo) | Puerto hacia Actividad (D3); por ahora devuelve 0 |
| `PKG/domain/usecase/CalculateKcalFromMacrosUseCase.kt`, `LogMealUseCase.kt`, `ObserveDailyIntakeUseCase.kt` | Casos de uso públicos |
| `PKG/domain/usecase/CalculateCalorieGoalUseCase.kt`, `SuggestMealsUseCase.kt` (nuevos) | Meta calórica (§4.5) y sugerencias (§3), internos del módulo |
| `PKG/data/entity/MealEntity.kt`, `MealLogEntity.kt`, `SyncState.kt` (nuevo) | Tablas Room |
| `PKG/data/dao/MealDao.kt`, `MealLogDao.kt` | Consultas Room |
| `PKG/data/mapper/MealMapper.kt`, `MealLogMapper.kt` | Entidad ↔ dominio |
| `PKG/data/database/MovaDatabase.kt`, `DatabaseSeeder.kt`, `MealSeedParser.kt` (nuevo) | Base Room v1 y carga del catálogo |
| `app/src/main/assets/seed/meals.json` | Catálogo de 40 comidas |
| `PKG/data/remote/MealLogRemoteDataSource.kt`, `FirestoreMealLogDataSource.kt`, `FirebaseAuthUserIds.kt` (nuevos) | Firestore y estado de sesión |
| `PKG/data/repository/MealRepositoryImpl.kt` | Room + Firestore + reglas de sincronización (§5) |
| `PKG/data/repository/MealSyncCoordinator.kt`, `PKG/core/work/SyncMealLogsWorker.kt`, `PKG/core/work/MealLogSyncScheduler.kt` (nuevos) | Descarga, listener, cierre de sesión, reintentos |
| `PKG/core/di/NutritionContainer.kt` (nuevo) | Ensamblado manual de dependencias |
| `PKG/presentation/viewmodel/nutrition/*` | ViewModels, UiState y Event de cada pantalla |
| `PKG/presentation/viewmodel/nutrition/NutritionViewModelFactory.kt` (nuevo) | Factory genérica |
| `PKG/presentation/screen/nutrition/*Screen.kt` | Pantallas N1–N9 |
| `PKG/presentation/screen/nutrition/components/MealImage.kt`, `MacroBar.kt`, `MealTypeChips.kt` (nuevos) | Piezas de UI del módulo |
| `PKG/presentation/charts/SmoothAreaChart.kt` | Gráfica mensual (N7) |
| `PKG/presentation/navigation/Routes.kt`, `NutritionNavGraph.kt` (nuevo), `MovaNavHost.kt` | Rutas |
| Onboarding / Perfil (Task 3) | Altura D2 y dieta sin valor por defecto |
| `firestore.rules` (nuevo, en `Mova/`) | Reglas de seguridad (§5) |

---

### Task 1: Desugaring de `java.time` y dependencias de prueba

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Produces: `java.time` disponible en API 24+; `kotlinx-coroutines-test` disponible en `androidTest`.

- [ ] **Step 1: Agregar la versión y la librería al catálogo**

En `gradle/libs.versions.toml`, debajo de `workManager = "2.12.0"` agrega:

```toml
desugarJdkLibs = "2.1.5"
```

y debajo de la línea `androidx-work-testing = ...` agrega:

```toml
desugar-jdk-libs = { group = "com.android.tools", name = "desugar_jdk_libs", version.ref = "desugarJdkLibs" }
```

- [ ] **Step 2: Activar desugaring y la dependencia de pruebas instrumentadas**

En `app/build.gradle.kts`, reemplaza:

```kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
```

por:

```kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
```

y en `dependencies { ... }`, debajo de `implementation(libs.androidx.work.runtime.ktx)`, agrega:

```kotlin
    coreLibraryDesugaring(libs.desugar.jdk.libs)
```

y debajo de `androidTestImplementation(libs.androidx.work.testing)`:

```kotlin
    androidTestImplementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 3: Compilar**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "[Add] java.time desugaring and androidTest coroutines"
```

---

### Task 2: Modelos de dominio y cálculo de kcal

**Files:**
- Modify (hoy son stubs `// TODO`): `PKG/domain/model/Macros.kt`, `MealType.kt`, `MealSource.kt`, `Meal.kt`, `MealLog.kt`
- Create: `PKG/domain/model/MealCategory.kt`, `PKG/domain/model/DailyIntake.kt`
- Modify: `PKG/domain/usecase/CalculateKcalFromMacrosUseCase.kt`
- Delete: `PKG/domain/model/PlannedMeal.kt` (stub del plan semanal, sin referencias)
- Test: `TEST/domain/usecase/CalculateKcalFromMacrosUseCaseTest.kt` (hoy es stub), `TEST/domain/model/MealLogTest.kt`

**Interfaces:**
- Produces:
  - `data class Macros(proteinG: Double, carbsG: Double, fatG: Double)` con `val kcal: Int`, `operator fun plus(Macros)`, `fun scaled(factor: Double): Macros`, `Macros.ZERO`
  - `enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }` con `val label: String`
  - `enum class MealSource { CATALOG, MANUAL }`
  - `enum class MealCategory(val label: String) { SALAD_VEGETABLES, PROTEIN, GRAINS, FRUIT, SNACK, DRINK }`
  - `data class Meal(id, name, category: MealCategory, suitableFor: Set<MealType>, dietTags: Set<Diet>, servingGrams: Int, macros: Macros, kcal: Int, prepMinutes: Int, imageRes: String, tags: List<String>)`
  - `data class MealLog(id, userId, date: LocalDate, mealType, source, mealId: String?, name, grams: Int?, macros: Macros, kcal: Int, createdAt: Instant, updatedAt: Instant)` con `MealLog.fromMeal(...)`, `MealLog.manual(...)`, `MealLog.LOCAL_USER_ID`
  - `data class DailyIntake(date, consumed: Macros, consumedKcal: Int, goalKcal: Int, burnedKcal: Int, remainingKcal: Int, goalMacros: Macros, byMealType: Map<MealType, List<MealLog>>)`
  - `class CalculateKcalFromMacrosUseCase { operator fun invoke(macros: Macros): Int }`

- [ ] **Step 1: Escribir las pruebas que fallan**

`TEST/domain/usecase/CalculateKcalFromMacrosUseCaseTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.Macros
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateKcalFromMacrosUseCaseTest {
    private val calculate = CalculateKcalFromMacrosUseCase()

    @Test
    fun `uses 4 4 9 formula`() {
        assertEquals(514, calculate(Macros(proteinG = 12.0, carbsG = 22.0, fatG = 42.0)))
    }

    @Test
    fun `rounds to nearest integer`() {
        // 4*22 + 4*1.8 + 9*19 = 266.2 (macros del mockup Avocado Dish)
        assertEquals(266, calculate(Macros(22.0, 1.8, 19.0)))
    }

    @Test
    fun `zero macros give zero kcal`() {
        assertEquals(0, calculate(Macros.ZERO))
    }
}
```

`TEST/domain/model/MealLogTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealLogTest {
    private val meal = Meal(
        id = "meal_x", name = "X", category = MealCategory.PROTEIN,
        suitableFor = setOf(MealType.LUNCH), dietTags = setOf(Diet.CARNIVORE),
        servingGrams = 100, macros = Macros(10.0, 20.0, 5.0), kcal = 165,
        prepMinutes = 10, imageRes = "meal_x", tags = emptyList()
    )
    private val now = Instant.parse("2026-11-02T12:00:00Z")
    private val date = LocalDate.of(2026, 11, 2)

    @Test
    fun `fromMeal scales macros by grams and copies name`() {
        val log = MealLog.fromMeal(meal, grams = 150, userId = "u1", date = date, mealType = MealType.LUNCH, now = now, id = "id1")
        assertEquals(Macros(15.0, 30.0, 7.5), log.macros)
        assertEquals(248, log.kcal) // 60 + 120 + 67.5 = 247.5 -> 248
        assertEquals("X", log.name)
        assertEquals("meal_x", log.mealId)
        assertEquals(150, log.grams)
        assertEquals(MealSource.CATALOG, log.source)
    }

    @Test
    fun `manual has no meal id nor grams and trims name`() {
        val log = MealLog.manual("  Arepa  ", Macros(5.0, 30.0, 8.0), "u1", date, MealType.BREAKFAST, now, id = "id2")
        assertEquals("Arepa", log.name)
        assertNull(log.mealId)
        assertNull(log.grams)
        assertEquals(212, log.kcal)
        assertEquals(MealSource.MANUAL, log.source)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que fallan**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.domain.*"`
Expected: FAIL de compilación (`Unresolved reference 'Macros'`).

- [ ] **Step 3: Implementar los modelos**

`PKG/domain/model/Macros.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

import kotlin.math.roundToInt

data class Macros(
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
) {
    val kcal: Int get() = (4 * proteinG + 4 * carbsG + 9 * fatG).roundToInt()

    operator fun plus(other: Macros) =
        Macros(proteinG + other.proteinG, carbsG + other.carbsG, fatG + other.fatG)

    fun scaled(factor: Double) = Macros(proteinG * factor, carbsG * factor, fatG * factor)

    companion object {
        val ZERO = Macros(0.0, 0.0, 0.0)
    }
}
```

`PKG/domain/model/MealType.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

enum class MealType(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}
```

`PKG/domain/model/MealSource.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

enum class MealSource {
    CATALOG,
    MANUAL
}
```

`PKG/domain/model/MealCategory.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

enum class MealCategory(val label: String) {
    SALAD_VEGETABLES("Salad & Vegetables"),
    PROTEIN("Protein"),
    GRAINS("Grains"),
    FRUIT("Fruit"),
    SNACK("Snack"),
    DRINK("Drink")
}
```

`PKG/domain/model/Meal.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

data class Meal(
    val id: String,
    val name: String,
    val category: MealCategory,
    val suitableFor: Set<MealType>,
    val dietTags: Set<Diet>,
    val servingGrams: Int,
    val macros: Macros,
    val kcal: Int,
    val prepMinutes: Int,
    val imageRes: String,
    val tags: List<String>
)
```

`PKG/domain/model/MealLog.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class MealLog(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val mealType: MealType,
    val source: MealSource,
    val mealId: String?,
    val name: String,
    val grams: Int?,
    val macros: Macros,
    val kcal: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        /** userId usado cuando no hay sesión; esos registros nunca se suben a Firestore. */
        const val LOCAL_USER_ID = "local"

        fun fromMeal(
            meal: Meal,
            grams: Int,
            userId: String,
            date: LocalDate,
            mealType: MealType,
            now: Instant,
            id: String = UUID.randomUUID().toString()
        ): MealLog {
            val macros = meal.macros.scaled(grams.toDouble() / meal.servingGrams)
            return MealLog(id, userId, date, mealType, MealSource.CATALOG, meal.id, meal.name, grams, macros, macros.kcal, now, now)
        }

        fun manual(
            name: String,
            macros: Macros,
            userId: String,
            date: LocalDate,
            mealType: MealType,
            now: Instant,
            id: String = UUID.randomUUID().toString()
        ) = MealLog(id, userId, date, mealType, MealSource.MANUAL, null, name.trim(), null, macros, macros.kcal, now, now)
    }
}
```

`PKG/domain/model/DailyIntake.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

import java.time.LocalDate

data class DailyIntake(
    val date: LocalDate,
    val consumed: Macros,
    val consumedKcal: Int,
    val goalKcal: Int,
    val burnedKcal: Int,
    val remainingKcal: Int,
    val goalMacros: Macros,
    val byMealType: Map<MealType, List<MealLog>>
)
```

`PKG/domain/usecase/CalculateKcalFromMacrosUseCase.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.Macros

// kcal = 4P + 4C + 9G
class CalculateKcalFromMacrosUseCase {
    operator fun invoke(macros: Macros): Int = macros.kcal
}
```

Borra `PKG/domain/model/PlannedMeal.kt`.

- [ ] **Step 4: Ejecutar las pruebas**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.domain.*"`
Expected: PASS (5 pruebas).

- [ ] **Step 5: Commit**

```bash
git add -A app/src/main/java/co/edu/unal/dadm/mova/domain app/src/test/java/co/edu/unal/dadm/mova/domain
git commit -m "[Add] Nutrition domain models and kcal calculation"
```

---

### Task 3: Altura del usuario (D2) y dieta sin valor por defecto

Implementa §13 completo. Toca código de Onboarding y Perfil; el equipo confirmó que lo hace Nutrición.

**Files:**
- Create: `PKG/domain/model/BodyDefaults.kt`, `PKG/presentation/screen/onboarding/HeightStep.kt`
- Modify: `PKG/domain/model/UserProfile.kt`, `PKG/domain/model/OnboardingDraft.kt`, `PKG/data/entity/UserProfileEntity.kt`, `PKG/data/remote/FirestoreAssessmentDataSource.kt`, `PKG/data/remote/FirestoreUserDataSource.kt`, `PKG/domain/usecase/SaveUserProfileUseCase.kt`, `PKG/presentation/viewmodel/onboarding/OnboardingEvent.kt`, `PKG/presentation/viewmodel/onboarding/OnboardingViewModel.kt`, `PKG/presentation/screen/onboarding/AssessmentScreen.kt`, los 8 `*Step.kt` de onboarding, `PKG/presentation/viewmodel/profile/ProfileViewModel.kt`, `PKG/presentation/screen/profile/PersonalInfoScreen.kt`
- Test: `TEST/presentation/viewmodel/onboarding/OnboardingViewModelTest.kt`

**Interfaces:**
- Produces: `UserProfile.heightCm: Int?`; `fun defaultHeightCm(gender: Gender?): Int`; `OnboardingEvent.SetHeight(heightCm: Int)`.

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/presentation/viewmodel/onboarding/OnboardingViewModelTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingViewModelTest {
    // Sin SaveAssessmentUseCase ni userId: Complete no lanza corrutinas.
    private fun complete(vararg events: OnboardingEvent) = OnboardingViewModel().run {
        events.forEach(::onEvent)
        onEvent(OnboardingEvent.Complete)
        uiState.value.draft
    }

    @Test
    fun `keeps the chosen height`() {
        assertEquals(182, complete(OnboardingEvent.SetHeight(182)).heightCm)
    }

    @Test
    fun `default height depends on gender`() {
        assertEquals(160, complete(OnboardingEvent.SetGender(Gender.FEMALE)).heightCm)
        assertEquals(165, complete(OnboardingEvent.SetGender(Gender.PREFER_NOT_TO_SAY)).heightCm)
        // Sin género, el onboarding asigna MALE -> 170
        assertEquals(170, complete().heightCm)
    }

    @Test
    fun `diet stays null when not chosen`() {
        assertNull(complete().diet)
    }

    @Test
    fun `chosen diet is kept`() {
        assertEquals(Diet.VEGETARIAN, complete(OnboardingEvent.SetDiet(Diet.VEGETARIAN)).diet)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingViewModelTest"`
Expected: FAIL de compilación (`Unresolved reference 'SetHeight'`).

- [ ] **Step 3: Modelo y valores por defecto**

`PKG/domain/model/BodyDefaults.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.model

/** Altura usada cuando el usuario no la ingresó (spec de Nutrición §13.4). */
fun defaultHeightCm(gender: Gender?): Int = when (gender) {
    Gender.MALE -> 170
    Gender.FEMALE -> 160
    Gender.PREFER_NOT_TO_SAY, null -> 165
}
```

En `PKG/domain/model/UserProfile.kt`, debajo de `val weightKg: Double? = null,` agrega:

```kotlin
    val heightCm: Int? = null,
```

En `PKG/domain/model/OnboardingDraft.kt`, debajo de `val weightKg: Double? = null,` agrega:

```kotlin
    val heightCm: Int? = null,
```

En `PKG/data/entity/UserProfileEntity.kt`, debajo de `val weightKg: Double? = null,` agrega:

```kotlin
    val heightCm: Int? = null,
```

- [ ] **Step 4: Persistencia en Firestore**

`PKG/data/remote/FirestoreAssessmentDataSource.kt`: debajo de `"weightKg" to draft.weightKg,` agrega:

```kotlin
            "heightCm" to draft.heightCm,
```

`PKG/data/remote/FirestoreUserDataSource.kt`: debajo de `weightKg = getDouble("weightKg"),` agrega:

```kotlin
            heightCm = getLong("heightCm")?.toInt(),
```

`PKG/domain/usecase/SaveUserProfileUseCase.kt`: debajo de `"weightKg" to profile.weightKg,` agrega:

```kotlin
                "heightCm" to profile.heightCm,
```

- [ ] **Step 5: Evento y ViewModel del onboarding**

`PKG/presentation/viewmodel/onboarding/OnboardingEvent.kt`: debajo de `data class SetWeight(...)` agrega:

```kotlin
    data class SetHeight(val heightCm: Int) : OnboardingEvent
```

`PKG/presentation/viewmodel/onboarding/OnboardingViewModel.kt`:

1. Agrega el import `import co.edu.unal.dadm.mova.domain.model.defaultHeightCm`.
2. Debajo de `is OnboardingEvent.SetWeight -> draft.copy(weightKg = event.weightKg)` agrega:

```kotlin
            is OnboardingEvent.SetHeight -> draft.copy(heightCm = event.heightCm)
```

3. Reemplaza el bloque `val completedDraft = draft.copy( ... )` por:

```kotlin
                val gender = draft.gender ?: Gender.MALE
                val completedDraft = draft.copy(
                    age = draft.age ?: 19,
                    weightKg = draft.weightKg ?: 62.0,
                    heightCm = draft.heightCm ?: defaultHeightCm(gender),
                    gender = gender,
                    fitnessLevel = draft.fitnessLevel ?: FitnessLevel.INTERMEDIATE,
                    goal = draft.goal ?: Goal.AI_COACH,
                    avatarId = draft.avatarId ?: "avatar_2"
                )
```

(La dieta ya no recibe valor por defecto: queda `draft.diet`, que puede ser `null`. Si el import de `Diet` queda sin uso, bórralo.)

- [ ] **Step 6: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingViewModelTest"`
Expected: PASS (4 pruebas).

- [ ] **Step 7: Pantalla de altura**

`PKG/presentation/screen/onboarding/HeightStep.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.charts.RulerPicker
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import kotlin.math.roundToInt

@Composable
fun HeightStep(
    heightCm: Int?,
    onHeightChanged: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val height = (heightCm ?: 170).toFloat()
    var isCm by remember { mutableStateOf(true) }

    AssessmentStep(
        stepIndex = 3,
        totalSteps = 9,
        title = "What's your height?",
        canContinue = height in 100f..250f,
        onContinue = onContinue,
        onBack = onBack,
        accent = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                UnitPill("Cm", selected = isCm) { isCm = true }
                Spacer(modifier = Modifier.width(16.dp))
                UnitPill("Ft", selected = !isCm) { isCm = false }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = if (isCm) "${height.roundToInt()} cm" else feetAndInches(height),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            RulerPicker(
                value = height,
                onValueChange = { onHeightChanged(it.roundToInt()) },
                range = 100f..250f,
                lineColor = Color.White,
                centerIndicatorColor = Color.White
            )
        }
    }
}

@Composable
private fun RowScope.UnitPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MovaOrange else Color.White
        )
    }
}

private fun feetAndInches(cm: Float): String {
    val totalInches = (cm / 2.54f).roundToInt()
    return "${totalInches / 12} ft ${totalInches % 12} in"
}
```

- [ ] **Step 8: Insertar el paso y renumerar**

En `PKG/presentation/screen/onboarding/AssessmentScreen.kt`, reemplaza el `when (step) { ... }` completo por:

```kotlin
        when (step) {
            0 -> AgeStep(
                age = state.draft.age,
                onAgeChanged = { viewModel.onEvent(OnboardingEvent.SetAge(it)) },
                onContinue = { step++ }
            )
            1 -> WeightStep(
                weightKg = state.draft.weightKg,
                onWeightChanged = { viewModel.onEvent(OnboardingEvent.SetWeight(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            2 -> HeightStep(
                heightCm = state.draft.heightCm,
                onHeightChanged = { viewModel.onEvent(OnboardingEvent.SetHeight(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            3 -> FitnessLevelStep(
                selected = state.draft.fitnessLevel,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetFitnessLevel(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            4 -> GenderStep(
                selected = state.draft.gender,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetGender(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            5 -> VoiceStep(
                onContinue = {
                    viewModel.onEvent(OnboardingEvent.SetVoiceNote(null))
                    step++
                },
                onBack = { step-- }
            )
            6 -> GoalStep(
                selected = state.draft.goal,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetGoal(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            7 -> AvatarStep(
                selected = state.draft.avatarId,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetAvatar(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            else -> DietStep(
                selected = state.draft.diet,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetDiet(it)) },
                onContinue = { viewModel.onEvent(OnboardingEvent.Complete) },
                onBack = { step-- }
            )
        }
```

En cada archivo de paso, cambia las dos líneas `stepIndex = …,` / `totalSteps = 6,` por estos valores (`totalSteps = 9` en todos):

| Archivo | `stepIndex` actual | `stepIndex` nuevo |
|---|---|---|
| `AgeStep.kt` | 1 | 1 |
| `WeightStep.kt` | 2 | 2 |
| `HeightStep.kt` | — | 3 (ya escrito) |
| `FitnessLevelStep.kt` | 3 | 4 |
| `GenderStep.kt` | 4 | 5 |
| `VoiceStep.kt` | 5 | 6 |
| `GoalStep.kt` | 6 | 7 |
| `AvatarStep.kt` | 6 | 8 |
| `DietStep.kt` | 1 | 9 |

Ejemplo para `FitnessLevelStep.kt`: `stepIndex = 3,` → `stepIndex = 4,` y `totalSteps = 6,` → `totalSteps = 9,`.

- [ ] **Step 9: Altura editable en Perfil**

`PKG/presentation/viewmodel/profile/ProfileViewModel.kt`: reemplaza la firma y el cuerpo de `updatePersonalInfo` para recibir la altura:

```kotlin
    fun updatePersonalInfo(name: String, email: String, location: String, age: Int?, weightKg: Double?, heightCm: Int? = null) {
        viewModelScope.launch {
            try {
                val changes = mutableMapOf<String, Any?>()
                if (name.isNotBlank()) changes["displayName"] = name
                if (email.isNotBlank()) changes["email"] = email
                if (location.isNotBlank()) changes["location"] = location
                if (age != null) changes["age"] = age
                if (weightKg != null) changes["weightKg"] = weightKg
                if (heightCm != null) changes["heightCm"] = heightCm
                repository.update(userId, changes)
                error.value = null
            } catch (_: Exception) {
                error.value = "Personal info saved."
            }
        }
    }
```

`PKG/presentation/screen/profile/PersonalInfoScreen.kt`:

1. Debajo de `var weight by remember(profile) { ... }` agrega:

```kotlin
    var height by remember(profile) { mutableStateOf(profile?.heightCm?.let { "$it cm" } ?: "") }
```

2. Debajo del `MovaTextField` de «Current Weight» agrega:

```kotlin
            MovaTextField(value = height, onValueChange = { height = it }, label = "Height", leadingIconText = "📏", placeholder = "170 cm")
```

3. En el `onClick` de «Save Changes», reemplaza la llamada a `updatePersonalInfo` por:

```kotlin
                    val parsedHeight = height.replace("[^0-9]".toRegex(), "").toIntOrNull()?.takeIf { it in 100..250 }
                    profileViewModel.updatePersonalInfo(name, email, locationText, parsedAge, parsedWeight, parsedHeight)
```

- [ ] **Step 10: Compilar y probar a mano**

Run: `./gradlew :app:assembleDebug :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingViewModelTest"`
Expected: `BUILD SUCCESSFUL`. En el emulador: el onboarding muestra 9 pasos numerados del 1 al 9, la altura aparece después del peso y en Firestore el documento `users/{uid}` tiene `heightCm`. En Settings → Personal Info se puede editar la altura.

- [ ] **Step 11: Commit**

```bash
git add -A app/src/main/java/co/edu/unal/dadm/mova app/src/test/java/co/edu/unal/dadm/mova/presentation/viewmodel/onboarding
git commit -m "[Add] User height (D2) in onboarding and profile"
```

---

### Task 4: Meta calórica diaria

**Files:**
- Create: `PKG/domain/usecase/CalculateCalorieGoalUseCase.kt`
- Test: `TEST/domain/usecase/CalculateCalorieGoalUseCaseTest.kt`

**Interfaces:**
- Consumes: `UserProfile` (con `heightCm`, Task 3), `defaultHeightCm`, `Macros`.
- Produces: `data class CalorieGoal(val kcal: Int, val macros: Macros)`; `class CalculateCalorieGoalUseCase { operator fun invoke(profile: UserProfile?, burnedKcal: Int = 0): CalorieGoal }`.

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/domain/usecase/CalculateCalorieGoalUseCaseTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateCalorieGoalUseCaseTest {
    private val calculate = CalculateCalorieGoalUseCase()

    private fun profile(
        gender: Gender? = Gender.MALE, age: Int? = 30, weightKg: Double? = 80.0, heightCm: Int? = 180,
        level: FitnessLevel? = FitnessLevel.INTERMEDIATE, goal: Goal? = Goal.LOSE_WEIGHT
    ) = UserProfile(userId = "u", gender = gender, age = age, weightKg = weightKg, heightCm = heightCm, fitnessLevel = level, goal = goal)

    @Test
    fun `male losing weight`() {
        // BMR = 800 + 1125 - 150 + 5 = 1780; x1.55 x0.85 = 2345.15 -> 2350
        val goal = calculate(profile())
        assertEquals(2350, goal.kcal)
        assertEquals(128.0, goal.macros.proteinG, 0.01) // 1.6 g/kg
        assertEquals(65.28, goal.macros.fatG, 0.01)     // 25 % de 2350 / 9
        assertEquals(312.63, goal.macros.carbsG, 0.01)  // (2350 - 512 - 587.5) / 4
    }

    @Test
    fun `female beginner explore`() {
        // 600 + 1031.25 - 125 - 161 = 1345.25; x1.2 = 1614.3 -> 1610
        assertEquals(1610, calculate(profile(Gender.FEMALE, 25, 60.0, 165, FitnessLevel.BEGINNER, Goal.EXPLORE)).kcal)
    }

    @Test
    fun `gain muscle uses 2 g per kg and 1_10 factor`() {
        val goal = calculate(profile(goal = Goal.GAIN_MUSCLE))
        assertEquals(3030, goal.kcal) // 1780 x 1.55 x 1.10 = 3034.9 -> 3030
        assertEquals(160.0, goal.macros.proteinG, 0.01)
    }

    @Test
    fun `missing profile uses defaults`() {
        // 25 años, 70 kg, sin género -> 165 cm y -78; INTERMEDIATE, EXPLORE
        // 700 + 1031.25 - 125 - 78 = 1528.25; x1.55 = 2368.79 -> 2370
        assertEquals(2370, calculate(null).kcal)
    }

    @Test
    fun `missing height uses gender default`() {
        // MALE sin altura -> 170 cm: 800 + 1062.5 - 150 + 5 = 1717.5; x1.55 x0.85 = 2262.8 -> 2260
        assertEquals(2260, calculate(profile(heightCm = null)).kcal)
    }

    @Test
    fun `prefer not to say uses -78`() {
        // 800 + 1125 - 150 - 78 = 1697; x1.55 x0.85 = 2235.8 -> 2240
        assertEquals(2240, calculate(profile(gender = Gender.PREFER_NOT_TO_SAY)).kcal)
    }

    @Test
    fun `never below 1200`() {
        assertEquals(1200, calculate(profile(Gender.FEMALE, 70, 40.0, 150, FitnessLevel.BEGINNER, Goal.LOSE_WEIGHT)).kcal)
    }

    @Test
    fun `burned kcal raise the goal`() {
        // 2345.15 + 300 = 2645.15 -> 2650
        assertEquals(2650, calculate(profile(), burnedKcal = 300).kcal)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCaseTest"`
Expected: FAIL de compilación (`Unresolved reference 'CalculateCalorieGoalUseCase'`).

- [ ] **Step 3: Implementar**

`PKG/domain/usecase/CalculateCalorieGoalUseCase.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.model.defaultHeightCm
import kotlin.math.roundToInt

data class CalorieGoal(val kcal: Int, val macros: Macros)

/** Mifflin–St Jeor × nivel × objetivo + kcal quemadas (spec de Nutrición §4.5 y §13.4). */
class CalculateCalorieGoalUseCase {
    operator fun invoke(profile: UserProfile?, burnedKcal: Int = 0): CalorieGoal {
        val weight = profile?.weightKg ?: DEFAULT_WEIGHT_KG
        val age = profile?.age ?: DEFAULT_AGE
        val gender = profile?.gender
        val height = profile?.heightCm ?: defaultHeightCm(gender)
        val level = profile?.fitnessLevel ?: FitnessLevel.INTERMEDIATE
        val goal = profile?.goal ?: Goal.EXPLORE

        val bmr = 10 * weight + 6.25 * height - 5 * age + genderConstant(gender)
        val raw = bmr * activityFactor(level) * goalFactor(goal) + burnedKcal
        val kcal = maxOf(MIN_KCAL, (raw / 10).roundToInt() * 10)

        val protein = weight * if (goal == Goal.GAIN_MUSCLE) 2.0 else 1.6
        val fat = kcal * 0.25 / 9
        val carbs = maxOf(0.0, (kcal - protein * 4 - fat * 9) / 4)
        return CalorieGoal(kcal, Macros(protein, carbs, fat))
    }

    private fun genderConstant(gender: Gender?) = when (gender) {
        Gender.MALE -> 5.0
        Gender.FEMALE -> -161.0
        Gender.PREFER_NOT_TO_SAY, null -> -78.0
    }

    private fun activityFactor(level: FitnessLevel) = when (level) {
        FitnessLevel.BEGINNER -> 1.2
        FitnessLevel.NOVICE -> 1.375
        FitnessLevel.INTERMEDIATE -> 1.55
        FitnessLevel.ADVANCED -> 1.725
        FitnessLevel.ATHLETE -> 1.9
    }

    private fun goalFactor(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT -> 0.85
        Goal.GAIN_MUSCLE -> 1.10
        Goal.IMPROVE_ENDURANCE, Goal.AI_COACH, Goal.EXPLORE -> 1.0
    }

    private companion object {
        const val DEFAULT_WEIGHT_KG = 70.0
        const val DEFAULT_AGE = 25
        const val MIN_KCAL = 1200
    }
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCaseTest"`
Expected: PASS (8 pruebas).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/domain/usecase/CalculateCalorieGoalUseCase.kt app/src/test/java/co/edu/unal/dadm/mova/domain/usecase/CalculateCalorieGoalUseCaseTest.kt
git commit -m "[Add] Daily calorie goal calculation"
```

---

### Task 5: Tablas Room, DAO, mappers y `MovaDatabase`

**Files:**
- Create: `PKG/data/entity/SyncState.kt`
- Modify (stubs): `PKG/data/entity/MealEntity.kt`, `PKG/data/entity/MealLogEntity.kt`, `PKG/data/dao/MealDao.kt`, `PKG/data/dao/MealLogDao.kt`, `PKG/data/mapper/MealMapper.kt`, `PKG/data/mapper/MealLogMapper.kt`, `PKG/data/database/DatabaseSeeder.kt`
- Modify: `PKG/data/database/MovaDatabase.kt`
- Test: `TEST/data/mapper/MealLogMapperTest.kt`, `ATEST/data/dao/MealLogDaoTest.kt`

**Interfaces:**
- Consumes: modelos de la Task 2.
- Produces:
  - `enum class SyncState { PENDING, SYNCED }`
  - `MealEntity`, `MealLogEntity` (enums como `String`; `date` como texto ISO; `createdAt`/`updatedAt` en milisegundos)
  - `MealDao`: `observeAll()`, `observeById(id)`, `insertAll(list)`, `count()`
  - `MealLogDao`: `observeByDate(userId, date: String)`, `observeRange(userId, from: String, to: String)`, `getById(id)`, `upsert(entity)`, `upsertAll(list)`, `getPending(userId)`, `markSynced(id, updatedAt)`, `hardDelete(id)`, `deleteSyncedForUser(userId)`, `countForUser(userId)`
  - `MealEntity.toDomain(): Meal`, `MealLogEntity.toDomain(): MealLog`, `MealLog.toEntity(syncState: SyncState, deleted: Boolean = false): MealLogEntity`
  - `MovaDatabase.getInstance(context)`, `mealDao()`, `mealLogDao()`

- [ ] **Step 1: Escribir la prueba del mapper que falla**

`TEST/data/mapper/MealLogMapperTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealLogMapperTest {
    @Test
    fun `round trip keeps every field`() {
        val log = MealLog.manual("Arepa", Macros(5.0, 30.0, 8.0), "u1", LocalDate.of(2026, 11, 2), MealType.BREAKFAST, Instant.ofEpochMilli(1_000), id = "id1")
        val entity = log.toEntity(SyncState.PENDING)
        assertEquals("2026-11-02", entity.date)
        assertEquals("PENDING", entity.syncState)
        assertFalse(entity.deleted)
        assertEquals(log, entity.toDomain())
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.data.mapper.MealLogMapperTest"`
Expected: FAIL de compilación.

- [ ] **Step 3: Entidades**

`PKG/data/entity/SyncState.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.entity

/** Solo existe en Room; nunca se sube a Firestore. */
enum class SyncState {
    PENDING,
    SYNCED
}
```

`PKG/data/entity/MealEntity.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Catálogo de solo lectura. Listas y enums se guardan como texto separado por comas. */
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val suitableFor: String,
    val dietTags: String,
    val servingGrams: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val kcal: Int,
    val prepMinutes: Int,
    val imageRes: String,
    val tags: String
)
```

`PKG/data/entity/MealLogEntity.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs", indices = [Index(value = ["userId", "date"])])
data class MealLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: String,
    val mealType: String,
    val source: String,
    val mealId: String?,
    val name: String,
    val grams: Int?,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val kcal: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean,
    val syncState: String
)
```

- [ ] **Step 4: DAO**

`PKG/data/dao/MealDao.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.edu.unal.dadm.mova.data.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY name")
    fun observeAll(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE id = :id")
    fun observeById(id: String): Flow<MealEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<MealEntity>)

    @Query("SELECT COUNT(*) FROM meals")
    suspend fun count(): Int
}
```

`PKG/data/dao/MealLogDao.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date AND deleted = 0 ORDER BY createdAt")
    fun observeByDate(userId: String, date: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date BETWEEN :from AND :to AND deleted = 0 ORDER BY date, createdAt")
    fun observeRange(userId: String, from: String, to: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE id = :id")
    suspend fun getById(id: String): MealLogEntity?

    @Upsert
    suspend fun upsert(log: MealLogEntity)

    @Upsert
    suspend fun upsertAll(logs: List<MealLogEntity>)

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND syncState = 'PENDING'")
    suspend fun getPending(userId: String): List<MealLogEntity>

    /** Solo marca SYNCED si nadie modificó el registro mientras se subía. */
    @Query("UPDATE meal_logs SET syncState = 'SYNCED' WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markSynced(id: String, updatedAt: Long)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM meal_logs WHERE userId = :userId AND syncState = 'SYNCED'")
    suspend fun deleteSyncedForUser(userId: String)

    @Query("SELECT COUNT(*) FROM meal_logs WHERE userId = :userId")
    suspend fun countForUser(userId: String): Int
}
```

- [ ] **Step 5: Mappers**

`PKG/data/mapper/MealMapper.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.MealType

fun MealEntity.toDomain() = Meal(
    id = id,
    name = name,
    category = MealCategory.valueOf(category),
    suitableFor = suitableFor.csv().map { MealType.valueOf(it) }.toSet(),
    dietTags = dietTags.csv().map { Diet.valueOf(it) }.toSet(),
    servingGrams = servingGrams,
    macros = Macros(proteinG, carbsG, fatG),
    kcal = kcal,
    prepMinutes = prepMinutes,
    imageRes = imageRes,
    tags = tags.csv()
)

internal fun String.csv(): List<String> = split(',').map { it.trim() }.filter { it.isNotEmpty() }
```

`PKG/data/mapper/MealLogMapper.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.Instant
import java.time.LocalDate

fun MealLogEntity.toDomain() = MealLog(
    id = id,
    userId = userId,
    date = LocalDate.parse(date),
    mealType = MealType.valueOf(mealType),
    source = MealSource.valueOf(source),
    mealId = mealId,
    name = name,
    grams = grams,
    macros = Macros(proteinG, carbsG, fatG),
    kcal = kcal,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt)
)

fun MealLog.toEntity(syncState: SyncState, deleted: Boolean = false) = MealLogEntity(
    id = id,
    userId = userId,
    date = date.toString(),
    mealType = mealType.name,
    source = source.name,
    mealId = mealId,
    name = name,
    grams = grams,
    proteinG = macros.proteinG,
    carbsG = macros.carbsG,
    fatG = macros.fatG,
    kcal = kcal,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    deleted = deleted,
    syncState = syncState.name
)
```

- [ ] **Step 6: Base de datos**

Reemplaza `PKG/data/database/MovaDatabase.kt` por:

```kotlin
package co.edu.unal.dadm.mova.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.dao.MealLogDao
import co.edu.unal.dadm.mova.data.dao.UserDao
import co.edu.unal.dadm.mova.data.dao.UserProfileDao
import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.data.entity.MealLogEntity

@Database(entities = [MealEntity::class, MealLogEntity::class], version = 1, exportSchema = true)
abstract class MovaDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun mealLogDao(): MealLogDao

    // Simulaciones de Perfil; se conservan hasta que ese módulo migre a Room.
    fun userDao(): UserDao = UserDao()
    fun userProfileDao(): UserProfileDao = UserProfileDao()

    companion object {
        @Volatile
        private var instance: MovaDatabase? = null

        fun getInstance(context: Context): MovaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, MovaDatabase::class.java, "mova.db")
                    .addCallback(DatabaseSeeder(context.applicationContext))
                    .build()
                    .also { instance = it }
            }
    }
}
```

`DatabaseSeeder` se completa en la Task 6. Para que compile ahora, reemplaza el stub `PKG/data/database/DatabaseSeeder.kt` por:

```kotlin
package co.edu.unal.dadm.mova.data.database

import android.content.Context
import androidx.room.RoomDatabase

class DatabaseSeeder(private val context: Context) : RoomDatabase.Callback()
```

- [ ] **Step 7: Prueba instrumentada del DAO**

`ATEST/data/dao/MealLogDaoTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.unal.dadm.mova.data.database.MovaDatabase
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealLogDaoTest {
    private lateinit var db: MovaDatabase
    private lateinit var dao: MealLogDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MovaDatabase::class.java).build()
        dao = db.mealLogDao()
    }

    @After
    fun tearDown() = db.close()

    private fun log(id: String, userId: String = "u1", date: String = "2026-11-02", deleted: Boolean = false, sync: String = "SYNCED", updatedAt: Long = 1) =
        MealLogEntity(id, userId, date, "LUNCH", "MANUAL", null, "X", null, 1.0, 1.0, 1.0, 17, 1, updatedAt, deleted, sync)

    @Test
    fun observeByDateFiltersUserDateAndDeleted() = runTest {
        dao.upsertAll(listOf(log("a"), log("b", deleted = true), log("c", userId = "u2"), log("d", date = "2026-11-01")))
        assertEquals(listOf("a"), dao.observeByDate("u1", "2026-11-02").first().map { it.id })
    }

    @Test
    fun observeRangeIncludesBothEnds() = runTest {
        dao.upsertAll(listOf(log("a", date = "2026-11-01"), log("b", date = "2026-11-30"), log("c", date = "2026-12-01")))
        assertEquals(listOf("a", "b"), dao.observeRange("u1", "2026-11-01", "2026-11-30").first().map { it.id })
    }

    @Test
    fun markSyncedIgnoresStaleUpdate() = runTest {
        dao.upsert(log("a", sync = "PENDING", updatedAt = 5))
        dao.markSynced("a", updatedAt = 4)
        assertEquals("PENDING", dao.getById("a")!!.syncState)
        dao.markSynced("a", updatedAt = 5)
        assertEquals("SYNCED", dao.getById("a")!!.syncState)
    }

    @Test
    fun deleteSyncedForUserKeepsPending() = runTest {
        dao.upsertAll(listOf(log("a"), log("b", sync = "PENDING"), log("c", userId = "u2")))
        dao.deleteSyncedForUser("u1")
        assertEquals(listOf("b"), dao.getPending("u1").map { it.id })
        assertEquals(1, dao.countForUser("u2"))
    }
}
```

- [ ] **Step 8: Ejecutar las pruebas**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.data.mapper.MealLogMapperTest"`
Expected: PASS.

Run (con emulador encendido): `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=co.edu.unal.dadm.mova.data.dao.MealLogDaoTest`
Expected: PASS (4 pruebas). Debe aparecer `app/schemas/co.edu.unal.dadm.mova.data.database.MovaDatabase/1.json`.

- [ ] **Step 9: Commit**

```bash
git add app/schemas app/src/main/java/co/edu/unal/dadm/mova/data app/src/test/java/co/edu/unal/dadm/mova/data app/src/androidTest/java/co/edu/unal/dadm/mova/data
git commit -m "[Add] Room tables and DAOs for meals and meal logs"
```

---

### Task 6: Catálogo semilla de 40 comidas

**Files:**
- Modify: `app/src/main/assets/seed/meals.json` (hoy `{ "_todo": ... }`)
- Create: `PKG/data/database/MealSeedParser.kt`
- Modify: `PKG/data/database/DatabaseSeeder.kt`
- Test: `ATEST/data/database/MealSeedTest.kt`

**Interfaces:**
- Consumes: `MealEntity`, `Macros`.
- Produces: `object MealSeedParser { fun parse(json: String): List<MealEntity> }`; tabla `meals` llena al crear la base.

Reglas del seed (§4.3): 40 comidas, 10 por tipo de comida (el **primer** valor de `suitableFor` es el tipo principal), al menos 12 veganas y 20 vegetarianas (las veganas cuentan como vegetarianas). `dietTags` lista las dietas que pueden comerla: vegana → `VEGAN, VEGETARIAN, CARNIVORE`; vegetariana → `VEGETARIAN, CARNIVORE`; con carne o pescado → `CARNIVORE`. `kcal` no va en el JSON: se calcula al sembrar.

- [ ] **Step 1: Escribir la prueba que falla**

`ATEST/data/database/MealSeedTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.unal.dadm.mova.domain.model.Macros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealSeedTest {
    private val meals by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        MealSeedParser.parse(context.assets.open("seed/meals.json").bufferedReader().use { it.readText() })
    }

    @Test
    fun hasFortyUniqueMeals() {
        assertEquals(40, meals.size)
        assertEquals(40, meals.map { it.id }.toSet().size)
    }

    @Test
    fun tenPerPrimaryMealType() {
        val counts = meals.groupingBy { it.suitableFor.substringBefore(',') }.eachCount()
        assertEquals(mapOf("BREAKFAST" to 10, "LUNCH" to 10, "DINNER" to 10, "SNACK" to 10), counts)
    }

    @Test
    fun enoughVeganAndVegetarianMeals() {
        assertTrue(meals.count { "VEGAN" in it.dietTags.split(',') } >= 12)
        assertTrue(meals.count { "VEGETARIAN" in it.dietTags.split(',') } >= 20)
    }

    @Test
    fun kcalComesFromMacros() {
        meals.forEach { assertEquals(it.id, Macros(it.proteinG, it.carbsG, it.fatG).kcal, it.kcal) }
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=co.edu.unal.dadm.mova.data.database.MealSeedTest`
Expected: FAIL de compilación (`Unresolved reference 'MealSeedParser'`).

- [ ] **Step 3: Escribir el catálogo**

Reemplaza `app/src/main/assets/seed/meals.json` por el siguiente contenido. Los macros son aproximados (USDA FoodData Central / ICBF, redondeados a 1 decimal); conviene revisarlos con esas tablas antes de la demo.

```json
[
  {"id":"meal_avocado_toast","name":"Avocado Toast","category":"GRAINS","suitableFor":["BREAKFAST","SNACK"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":150,"proteinG":6.5,"carbsG":30.0,"fatG":16.0,"prepMinutes":10,"imageRes":"meal_avocado_toast","tags":["avocado","toast","bread"]},
  {"id":"meal_oatmeal_berries","name":"Oatmeal with Berries","category":"GRAINS","suitableFor":["BREAKFAST"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":250,"proteinG":7.0,"carbsG":45.0,"fatG":5.0,"prepMinutes":10,"imageRes":"meal_oatmeal_berries","tags":["oats","berries","porridge"]},
  {"id":"meal_greek_yogurt_granola","name":"Greek Yogurt with Granola","category":"PROTEIN","suitableFor":["BREAKFAST","SNACK"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":200,"proteinG":18.0,"carbsG":35.0,"fatG":8.0,"prepMinutes":5,"imageRes":"meal_greek_yogurt_granola","tags":["yogurt","granola"]},
  {"id":"meal_scrambled_eggs_toast","name":"Scrambled Eggs on Toast","category":"PROTEIN","suitableFor":["BREAKFAST"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":200,"proteinG":20.0,"carbsG":25.0,"fatG":18.0,"prepMinutes":10,"imageRes":"meal_scrambled_eggs_toast","tags":["egg","eggs","toast"]},
  {"id":"meal_arepa_cheese","name":"Arepa with Cheese","category":"GRAINS","suitableFor":["BREAKFAST"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":180,"proteinG":14.0,"carbsG":42.0,"fatG":15.0,"prepMinutes":15,"imageRes":"meal_arepa_cheese","tags":["arepa","cheese","corn"]},
  {"id":"meal_banana_pancakes","name":"Banana Pancakes","category":"GRAINS","suitableFor":["BREAKFAST"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":200,"proteinG":10.0,"carbsG":55.0,"fatG":9.0,"prepMinutes":20,"imageRes":"meal_banana_pancakes","tags":["banana","pancakes"]},
  {"id":"meal_tofu_scramble","name":"Tofu Scramble","category":"PROTEIN","suitableFor":["BREAKFAST"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":200,"proteinG":20.0,"carbsG":8.0,"fatG":12.0,"prepMinutes":15,"imageRes":"meal_tofu_scramble","tags":["tofu","scramble"]},
  {"id":"meal_chia_pudding","name":"Chia Pudding","category":"FRUIT","suitableFor":["BREAKFAST","SNACK"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":200,"proteinG":6.0,"carbsG":25.0,"fatG":11.0,"prepMinutes":5,"imageRes":"meal_chia_pudding","tags":["chia","pudding","coconut"]},
  {"id":"meal_huevos_pericos","name":"Huevos Pericos","category":"PROTEIN","suitableFor":["BREAKFAST"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":180,"proteinG":14.0,"carbsG":6.0,"fatG":15.0,"prepMinutes":10,"imageRes":"meal_huevos_pericos","tags":["egg","tomato","onion"]},
  {"id":"meal_turkey_omelette","name":"Turkey Omelette","category":"PROTEIN","suitableFor":["BREAKFAST"],"dietTags":["CARNIVORE"],"servingGrams":220,"proteinG":30.0,"carbsG":4.0,"fatG":17.0,"prepMinutes":15,"imageRes":"meal_turkey_omelette","tags":["turkey","egg","omelette"]},

  {"id":"meal_chicken_rice_bowl","name":"Chicken Rice Bowl","category":"PROTEIN","suitableFor":["LUNCH","DINNER"],"dietTags":["CARNIVORE"],"servingGrams":350,"proteinG":38.0,"carbsG":55.0,"fatG":10.0,"prepMinutes":25,"imageRes":"meal_chicken_rice_bowl","tags":["chicken","rice","bowl"]},
  {"id":"meal_quinoa_salad","name":"Quinoa Salad","category":"SALAD_VEGETABLES","suitableFor":["LUNCH"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":300,"proteinG":11.0,"carbsG":45.0,"fatG":12.0,"prepMinutes":15,"imageRes":"meal_quinoa_salad","tags":["quinoa","salad","vegetables"]},
  {"id":"meal_lentil_soup","name":"Lentil Soup","category":"GRAINS","suitableFor":["LUNCH","DINNER"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":350,"proteinG":18.0,"carbsG":40.0,"fatG":5.0,"prepMinutes":35,"imageRes":"meal_lentil_soup","tags":["lentils","soup"]},
  {"id":"meal_bandeja_light","name":"Bandeja Paisa Light","category":"PROTEIN","suitableFor":["LUNCH"],"dietTags":["CARNIVORE"],"servingGrams":450,"proteinG":42.0,"carbsG":70.0,"fatG":22.0,"prepMinutes":40,"imageRes":"meal_bandeja_light","tags":["beans","beef","rice"]},
  {"id":"meal_caesar_salad_chicken","name":"Chicken Caesar Salad","category":"SALAD_VEGETABLES","suitableFor":["LUNCH"],"dietTags":["CARNIVORE"],"servingGrams":300,"proteinG":32.0,"carbsG":12.0,"fatG":20.0,"prepMinutes":15,"imageRes":"meal_caesar_salad_chicken","tags":["chicken","salad","caesar"]},
  {"id":"meal_falafel_wrap","name":"Falafel Wrap","category":"GRAINS","suitableFor":["LUNCH"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":280,"proteinG":15.0,"carbsG":55.0,"fatG":18.0,"prepMinutes":20,"imageRes":"meal_falafel_wrap","tags":["falafel","wrap","chickpea"]},
  {"id":"meal_tuna_sandwich","name":"Tuna Sandwich","category":"PROTEIN","suitableFor":["LUNCH","SNACK"],"dietTags":["CARNIVORE"],"servingGrams":220,"proteinG":28.0,"carbsG":35.0,"fatG":10.0,"prepMinutes":10,"imageRes":"meal_tuna_sandwich","tags":["tuna","sandwich","fish"]},
  {"id":"meal_veggie_burrito","name":"Bean and Veggie Burrito","category":"GRAINS","suitableFor":["LUNCH"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":320,"proteinG":16.0,"carbsG":65.0,"fatG":12.0,"prepMinutes":20,"imageRes":"meal_veggie_burrito","tags":["beans","burrito"]},
  {"id":"meal_caprese_pasta","name":"Caprese Pasta","category":"GRAINS","suitableFor":["LUNCH","DINNER"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":320,"proteinG":18.0,"carbsG":70.0,"fatG":16.0,"prepMinutes":20,"imageRes":"meal_caprese_pasta","tags":["pasta","tomato","mozzarella"]},
  {"id":"meal_ajiaco","name":"Ajiaco","category":"PROTEIN","suitableFor":["LUNCH"],"dietTags":["CARNIVORE"],"servingGrams":400,"proteinG":30.0,"carbsG":45.0,"fatG":12.0,"prepMinutes":60,"imageRes":"meal_ajiaco","tags":["chicken","potato","soup"]},

  {"id":"meal_grilled_salmon","name":"Grilled Salmon with Vegetables","category":"PROTEIN","suitableFor":["DINNER"],"dietTags":["CARNIVORE"],"servingGrams":300,"proteinG":35.0,"carbsG":12.0,"fatG":22.0,"prepMinutes":25,"imageRes":"meal_grilled_salmon","tags":["salmon","fish"]},
  {"id":"meal_beef_stir_fry","name":"Beef Stir Fry","category":"PROTEIN","suitableFor":["DINNER"],"dietTags":["CARNIVORE"],"servingGrams":320,"proteinG":32.0,"carbsG":30.0,"fatG":15.0,"prepMinutes":20,"imageRes":"meal_beef_stir_fry","tags":["beef","noodles","vegetables"]},
  {"id":"meal_chickpea_curry","name":"Chickpea Curry","category":"GRAINS","suitableFor":["DINNER","LUNCH"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":350,"proteinG":15.0,"carbsG":50.0,"fatG":14.0,"prepMinutes":30,"imageRes":"meal_chickpea_curry","tags":["chickpea","curry"]},
  {"id":"meal_veggie_omelette","name":"Vegetable Omelette","category":"PROTEIN","suitableFor":["DINNER","BREAKFAST"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":220,"proteinG":19.0,"carbsG":8.0,"fatG":16.0,"prepMinutes":15,"imageRes":"meal_veggie_omelette","tags":["egg","vegetables"]},
  {"id":"meal_stuffed_peppers","name":"Stuffed Peppers","category":"SALAD_VEGETABLES","suitableFor":["DINNER"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":300,"proteinG":14.0,"carbsG":35.0,"fatG":12.0,"prepMinutes":40,"imageRes":"meal_stuffed_peppers","tags":["peppers","rice","cheese"]},
  {"id":"meal_baked_chicken_potatoes","name":"Baked Chicken with Potatoes","category":"PROTEIN","suitableFor":["DINNER"],"dietTags":["CARNIVORE"],"servingGrams":380,"proteinG":40.0,"carbsG":40.0,"fatG":14.0,"prepMinutes":45,"imageRes":"meal_baked_chicken_potatoes","tags":["chicken","potato"]},
  {"id":"meal_tofu_stir_fry","name":"Tofu Stir Fry","category":"PROTEIN","suitableFor":["DINNER"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":320,"proteinG":20.0,"carbsG":30.0,"fatG":14.0,"prepMinutes":20,"imageRes":"meal_tofu_stir_fry","tags":["tofu","vegetables"]},
  {"id":"meal_mushroom_risotto","name":"Mushroom Risotto","category":"GRAINS","suitableFor":["DINNER"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":320,"proteinG":10.0,"carbsG":65.0,"fatG":14.0,"prepMinutes":35,"imageRes":"meal_mushroom_risotto","tags":["mushroom","rice","risotto"]},
  {"id":"meal_fish_tacos","name":"Fish Tacos","category":"PROTEIN","suitableFor":["DINNER"],"dietTags":["CARNIVORE"],"servingGrams":280,"proteinG":26.0,"carbsG":35.0,"fatG":12.0,"prepMinutes":25,"imageRes":"meal_fish_tacos","tags":["fish","tacos","tortilla"]},
  {"id":"meal_black_bean_soup","name":"Black Bean Soup","category":"GRAINS","suitableFor":["DINNER"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":350,"proteinG":15.0,"carbsG":42.0,"fatG":6.0,"prepMinutes":30,"imageRes":"meal_black_bean_soup","tags":["beans","soup"]},

  {"id":"meal_apple_peanut_butter","name":"Apple with Peanut Butter","category":"FRUIT","suitableFor":["SNACK"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":180,"proteinG":7.0,"carbsG":28.0,"fatG":16.0,"prepMinutes":2,"imageRes":"meal_apple_peanut_butter","tags":["apple","peanut","butter"]},
  {"id":"meal_hummus_carrots","name":"Hummus with Carrots","category":"SNACK","suitableFor":["SNACK"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":150,"proteinG":6.0,"carbsG":18.0,"fatG":9.0,"prepMinutes":5,"imageRes":"meal_hummus_carrots","tags":["hummus","carrot","chickpea"]},
  {"id":"meal_mixed_nuts","name":"Mixed Nuts","category":"SNACK","suitableFor":["SNACK"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":40,"proteinG":8.0,"carbsG":9.0,"fatG":21.0,"prepMinutes":0,"imageRes":"meal_mixed_nuts","tags":["nuts","almonds","walnuts"]},
  {"id":"meal_protein_shake","name":"Protein Shake","category":"DRINK","suitableFor":["SNACK","BREAKFAST"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":350,"proteinG":30.0,"carbsG":12.0,"fatG":4.0,"prepMinutes":3,"imageRes":"meal_protein_shake","tags":["shake","protein","whey"]},
  {"id":"meal_fruit_salad","name":"Fruit Salad","category":"FRUIT","suitableFor":["SNACK","BREAKFAST"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":250,"proteinG":2.0,"carbsG":35.0,"fatG":1.0,"prepMinutes":10,"imageRes":"meal_fruit_salad","tags":["fruit","salad","papaya"]},
  {"id":"meal_cottage_cheese_pineapple","name":"Cottage Cheese with Pineapple","category":"PROTEIN","suitableFor":["SNACK"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":200,"proteinG":22.0,"carbsG":18.0,"fatG":4.0,"prepMinutes":5,"imageRes":"meal_cottage_cheese_pineapple","tags":["cottage","cheese","pineapple"]},
  {"id":"meal_green_smoothie","name":"Green Smoothie","category":"DRINK","suitableFor":["SNACK","BREAKFAST"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":350,"proteinG":5.0,"carbsG":40.0,"fatG":3.0,"prepMinutes":5,"imageRes":"meal_green_smoothie","tags":["smoothie","spinach","banana"]},
  {"id":"meal_boiled_eggs","name":"Boiled Eggs","category":"PROTEIN","suitableFor":["SNACK"],"dietTags":["VEGETARIAN","CARNIVORE"],"servingGrams":100,"proteinG":13.0,"carbsG":1.0,"fatG":10.0,"prepMinutes":12,"imageRes":"meal_boiled_eggs","tags":["egg","eggs"]},
  {"id":"meal_edamame","name":"Edamame","category":"SNACK","suitableFor":["SNACK"],"dietTags":["VEGAN","VEGETARIAN","CARNIVORE"],"servingGrams":150,"proteinG":17.0,"carbsG":13.0,"fatG":8.0,"prepMinutes":8,"imageRes":"meal_edamame","tags":["edamame","soy","beans"]},
  {"id":"meal_beef_jerky","name":"Beef Jerky","category":"PROTEIN","suitableFor":["SNACK"],"dietTags":["CARNIVORE"],"servingGrams":40,"proteinG":13.0,"carbsG":4.0,"fatG":3.0,"prepMinutes":0,"imageRes":"meal_beef_jerky","tags":["beef","jerky"]}
]
```

- [ ] **Step 4: Parser y seeder**

`PKG/data/database/MealSeedParser.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.database

import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.domain.model.Macros
import org.json.JSONArray

object MealSeedParser {
    fun parse(json: String): List<MealEntity> {
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val protein = item.getDouble("proteinG")
            val carbs = item.getDouble("carbsG")
            val fat = item.getDouble("fatG")
            MealEntity(
                id = item.getString("id"),
                name = item.getString("name"),
                category = item.getString("category"),
                suitableFor = item.getJSONArray("suitableFor").strings().joinToString(","),
                dietTags = item.getJSONArray("dietTags").strings().joinToString(","),
                servingGrams = item.getInt("servingGrams"),
                proteinG = protein,
                carbsG = carbs,
                fatG = fat,
                kcal = Macros(protein, carbs, fat).kcal,
                prepMinutes = item.getInt("prepMinutes"),
                imageRes = item.getString("imageRes"),
                tags = item.getJSONArray("tags").strings().joinToString(",")
            )
        }
    }

    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
}
```

Reemplaza `PKG/data/database/DatabaseSeeder.kt` por:

```kotlin
package co.edu.unal.dadm.mova.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import co.edu.unal.dadm.mova.data.entity.MealEntity

/** Carga assets/seed/meals.json la primera vez que se crea la base. */
class DatabaseSeeder(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val json = context.assets.open("seed/meals.json").bufferedReader().use { it.readText() }
        db.beginTransaction()
        try {
            MealSeedParser.parse(json).forEach { meal ->
                db.insert("meals", SQLiteDatabase.CONFLICT_REPLACE, meal.toContentValues())
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun MealEntity.toContentValues() = ContentValues().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("suitableFor", suitableFor)
        put("dietTags", dietTags)
        put("servingGrams", servingGrams)
        put("proteinG", proteinG)
        put("carbsG", carbsG)
        put("fatG", fatG)
        put("kcal", kcal)
        put("prepMinutes", prepMinutes)
        put("imageRes", imageRes)
        put("tags", tags)
    }
}
```

- [ ] **Step 5: Ejecutar la prueba**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=co.edu.unal.dadm.mova.data.database.MealSeedTest`
Expected: PASS (4 pruebas).

- [ ] **Step 6: Imágenes (tarea manual, no bloquea)**

Consigue 40 imágenes libres de derechos (Unsplash o Pexels), conviértelas a WebP de máximo 80 KB y guárdalas en `app/src/main/res/drawable/` con el nombre exacto de cada `imageRes` (por ejemplo `meal_avocado_toast.webp`). Mientras falten, la UI muestra `ImagePlaceholder` con el nombre de la comida (Task 11).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/assets/seed/meals.json app/src/main/java/co/edu/unal/dadm/mova/data/database app/src/androidTest/java/co/edu/unal/dadm/mova/data/database
git commit -m "[Add] Seed catalog with 40 meals"
```

---

### Task 7: Fuente remota en Firestore y reglas de seguridad

**Files:**
- Create: `PKG/data/remote/MealLogRemoteDataSource.kt`, `PKG/data/remote/FirestoreMealLogDataSource.kt`, `PKG/data/remote/FirebaseAuthUserIds.kt`, `firestore.rules`

**Interfaces:**
- Consumes: `MealLogEntity`, `SyncState`.
- Produces: `MealLogRemoteDataSource` (`upsert`, `delete`, `fetchAll`, `listen`), `RemoteChange.Upserted(log)` / `RemoteChange.Removed(id)`, `fun firebaseAuthUserIds(auth: FirebaseAuth = FirebaseAuth.getInstance()): Flow<String?>`.

Esta tarea no lleva prueba unitaria: es un adaptador directo del SDK de Firebase. El repositorio se prueba con una fuente falsa (Task 8) y la integración se verifica a mano (Task 10).

- [ ] **Step 1: Contrato**

`PKG/data/remote/MealLogRemoteDataSource.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.remote

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

interface MealLogRemoteDataSource {
    suspend fun upsert(log: MealLogEntity)
    suspend fun delete(userId: String, id: String)
    suspend fun fetchAll(userId: String): List<MealLogEntity>
    fun listen(userId: String): Flow<List<RemoteChange>>
}

sealed interface RemoteChange {
    data class Upserted(val log: MealLogEntity) : RemoteChange
    data class Removed(val id: String) : RemoteChange
}
```

- [ ] **Step 2: Implementación con Firestore**

`PKG/data/remote/FirestoreMealLogDataSource.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.remote

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

/** users/{uid}/meal_logs/{id}; mismos campos que la tabla salvo syncState (spec §5). */
class FirestoreMealLogDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MealLogRemoteDataSource {

    private fun collection(userId: String) =
        firestore.collection("users").document(userId).collection("meal_logs")

    override suspend fun upsert(log: MealLogEntity) {
        collection(log.userId).document(log.id).set(log.toDocument()).await()
    }

    override suspend fun delete(userId: String, id: String) {
        collection(userId).document(id).delete().await()
    }

    override suspend fun fetchAll(userId: String): List<MealLogEntity> =
        collection(userId).get().await().documents.mapNotNull { it.toMealLogEntity() }

    override fun listen(userId: String): Flow<List<RemoteChange>> = callbackFlow {
        val registration = collection(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val changes = snapshot?.documentChanges.orEmpty().mapNotNull { change ->
                when (change.type) {
                    DocumentChange.Type.REMOVED -> RemoteChange.Removed(change.document.id)
                    else -> change.document.toMealLogEntity()?.let { RemoteChange.Upserted(it) }
                }
            }
            if (changes.isNotEmpty()) trySend(changes)
        }
        awaitClose { registration.remove() }
    }
}

private fun MealLogEntity.toDocument(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "date" to date,
    "mealType" to mealType,
    "source" to source,
    "mealId" to mealId,
    "name" to name,
    "grams" to grams,
    "proteinG" to proteinG,
    "carbsG" to carbsG,
    "fatG" to fatG,
    "kcal" to kcal,
    "createdAt" to Timestamp(Date(createdAt)),
    "updatedAt" to Timestamp(Date(updatedAt)),
    "deleted" to deleted
)

private fun DocumentSnapshot.toMealLogEntity(): MealLogEntity? = runCatching {
    MealLogEntity(
        id = id,
        userId = getString("userId")!!,
        date = getString("date")!!,
        mealType = getString("mealType")!!,
        source = getString("source")!!,
        mealId = getString("mealId"),
        name = getString("name")!!,
        grams = getLong("grams")?.toInt(),
        proteinG = getDouble("proteinG") ?: 0.0,
        carbsG = getDouble("carbsG") ?: 0.0,
        fatG = getDouble("fatG") ?: 0.0,
        kcal = getLong("kcal")!!.toInt(),
        createdAt = getTimestamp("createdAt")!!.toDate().time,
        updatedAt = getTimestamp("updatedAt")!!.toDate().time,
        deleted = getBoolean("deleted") ?: false,
        syncState = SyncState.SYNCED.name
    )
}.getOrNull()
```

- [ ] **Step 3: Sesión de Firebase como Flow**

`PKG/data/remote/FirebaseAuthUserIds.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Emite el uid actual (o null) cada vez que cambia la sesión. */
fun firebaseAuthUserIds(auth: FirebaseAuth = FirebaseAuth.getInstance()): Flow<String?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
}
```

- [ ] **Step 4: Reglas de seguridad**

Crea `firestore.rules` (en `Mova/`, junto a `settings.gradle.kts`):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/meal_logs/{logId} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

El bloque `match /users/{uid}/meal_logs/{logId}` se agrega a las reglas que ya tenga el proyecto en Firebase Console (Firestore → Reglas), sin reemplazar las existentes. Publicarlas es un paso manual de quien administra Firebase.

- [ ] **Step 5: Compilar**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/data/remote firestore.rules
git commit -m "[Add] Firestore data source for meal logs"
```

---

### Task 8: `MealRepositoryImpl` con sincronización

Implementa §5: registrar, sin conexión, eliminar, descargar, conflictos y cerrar sesión.

**Files:**
- Modify (stubs): `PKG/domain/repository/MealRepository.kt`, `PKG/data/repository/MealRepositoryImpl.kt`
- Create: `PKG/data/repository/SyncScheduler.kt`
- Test: `TEST/data/repository/FakeMealLogDao.kt`, `TEST/data/repository/FakeMealLogRemote.kt`, `TEST/data/repository/MealRepositoryImplTest.kt`

**Interfaces:**
- Consumes: DAO y mappers (Task 5), `MealLogRemoteDataSource` y `RemoteChange` (Task 7).
- Produces:
  - `interface MealRepository` (firma de §6)
  - `fun interface SyncScheduler { fun schedule() }`
  - `class MealRepositoryImpl(mealDao: MealDao, logDao: MealLogDao, remote: MealLogRemoteDataSource, scheduler: SyncScheduler, scope: CoroutineScope, pushTimeoutMs: Long = 5_000, now: () -> Long = System::currentTimeMillis) : MealRepository` con además: `suspend fun syncPending(userId: String): Boolean`, `suspend fun downloadIfEmpty(userId: String)`, `suspend fun listenRemote(userId: String)`, `suspend fun applyRemote(change: RemoteChange)`, `suspend fun onSignOut(userId: String)`

- [ ] **Step 1: Contrato del dominio**

Reemplaza `PKG/domain/repository/MealRepository.kt` por:

```kotlin
package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Contrato congelado (D1). No cambiar firmas sin acordarlo con los módulos que lo usan. */
interface MealRepository {
    fun observeCatalog(): Flow<List<Meal>>
    fun observeMeal(id: String): Flow<Meal?>
    fun observeLogs(userId: String, date: LocalDate): Flow<List<MealLog>>
    fun observeLogs(userId: String, from: LocalDate, to: LocalDate): Flow<List<MealLog>>
    suspend fun log(entry: MealLog): Result<String>
    suspend fun delete(logId: String): Result<Unit>
}
```

- [ ] **Step 2: Fakes para las pruebas**

`TEST/data/repository/FakeMealLogDao.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.dao.MealLogDao
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMealLogDao : MealLogDao {
    val rows = MutableStateFlow<Map<String, MealLogEntity>>(emptyMap())

    override fun observeByDate(userId: String, date: String): Flow<List<MealLogEntity>> =
        rows.map { all -> all.values.filter { it.userId == userId && it.date == date && !it.deleted }.sortedBy { it.createdAt } }

    override fun observeRange(userId: String, from: String, to: String): Flow<List<MealLogEntity>> =
        rows.map { all ->
            all.values.filter { it.userId == userId && it.date in from..to && !it.deleted }
                .sortedWith(compareBy({ it.date }, { it.createdAt }))
        }

    override suspend fun getById(id: String) = rows.value[id]
    override suspend fun upsert(log: MealLogEntity) { rows.value = rows.value + (log.id to log) }
    override suspend fun upsertAll(logs: List<MealLogEntity>) { rows.value = rows.value + logs.associateBy { it.id } }
    override suspend fun getPending(userId: String) =
        rows.value.values.filter { it.userId == userId && it.syncState == "PENDING" }
    override suspend fun markSynced(id: String, updatedAt: Long) {
        val row = rows.value[id] ?: return
        if (row.updatedAt == updatedAt) upsert(row.copy(syncState = "SYNCED"))
    }
    override suspend fun hardDelete(id: String) { rows.value = rows.value - id }
    override suspend fun deleteSyncedForUser(userId: String) {
        rows.value = rows.value.filterValues { !(it.userId == userId && it.syncState == "SYNCED") }
    }
    override suspend fun countForUser(userId: String) = rows.value.values.count { it.userId == userId }
}
```

`TEST/data/repository/FakeMealLogRemote.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.remote.MealLogRemoteDataSource
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeMealLogRemote : MealLogRemoteDataSource {
    val docs = mutableMapOf<String, MealLogEntity>()
    var offline = false
    val changes = MutableSharedFlow<List<RemoteChange>>(extraBufferCapacity = 8)

    private fun failIfOffline() {
        if (offline) throw IllegalStateException("offline")
    }

    override suspend fun upsert(log: MealLogEntity) { failIfOffline(); docs[log.id] = log }
    override suspend fun delete(userId: String, id: String) { failIfOffline(); docs.remove(id) }
    override suspend fun fetchAll(userId: String): List<MealLogEntity> {
        failIfOffline()
        return docs.values.filter { it.userId == userId }
    }
    override fun listen(userId: String): Flow<List<RemoteChange>> = changes
}
```

- [ ] **Step 3: Escribir las pruebas que fallan**

`TEST/data/repository/MealRepositoryImplTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealRepositoryImplTest {
    private val dao = FakeMealLogDao()
    private val remote = FakeMealLogRemote()
    private var scheduled = 0
    private val date = LocalDate.of(2026, 11, 2)

    private val noMeals = object : MealDao {
        override fun observeAll(): Flow<List<MealEntity>> = emptyFlow()
        override fun observeById(id: String): Flow<MealEntity?> = emptyFlow()
        override suspend fun insertAll(meals: List<MealEntity>) = Unit
        override suspend fun count() = 0
    }

    private fun TestScope.repository() =
        MealRepositoryImpl(noMeals, dao, remote, { scheduled++ }, backgroundScope, now = { 9_000 })

    private fun entry(id: String = "a", userId: String = "u1", at: Long = 1_000) =
        MealLog.manual("X", Macros(10.0, 10.0, 10.0), userId, date, MealType.LUNCH, Instant.ofEpochMilli(at), id = id)

    @Test
    fun `log online ends synced and uploaded`() = runTest {
        val repo = repository()
        assertEquals(Result.Success("a"), repo.log(entry()))
        advanceUntilIdle()
        assertEquals("SYNCED", dao.getById("a")!!.syncState)
        assertTrue("a" in remote.docs)
    }

    @Test
    fun `log offline stays pending, visible and schedules worker`() = runTest {
        remote.offline = true
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        assertEquals("PENDING", dao.getById("a")!!.syncState)
        assertEquals(1, scheduled)
        assertEquals(1, repo.observeLogs("u1", date).first().size)
    }

    @Test
    fun `local user is never uploaded`() = runTest {
        val repo = repository()
        repo.log(entry(userId = MealLog.LOCAL_USER_ID))
        advanceUntilIdle()
        assertTrue(remote.docs.isEmpty())
        assertEquals(0, scheduled)
    }

    @Test
    fun `delete removes remote document and local row`() = runTest {
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        assertEquals(Result.Success(Unit), repo.delete("a"))
        advanceUntilIdle()
        assertFalse("a" in remote.docs)
        assertNull(dao.getById("a"))
    }

    @Test
    fun `delete offline hides the row until it syncs`() = runTest {
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        remote.offline = true
        repo.delete("a")
        advanceUntilIdle()
        assertTrue(dao.getById("a")!!.deleted)
        assertTrue(repo.observeLogs("u1", date).first().isEmpty())
        remote.offline = false
        assertTrue(repo.syncPending("u1"))
        assertNull(dao.getById("a"))
    }

    @Test
    fun `remote change wins only when newer`() = runTest {
        val repo = repository()
        repo.log(entry(at = 2_000))
        advanceUntilIdle()
        val older = dao.getById("a")!!.copy(name = "Old", updatedAt = 1_000)
        repo.applyRemote(RemoteChange.Upserted(older))
        assertEquals("X", dao.getById("a")!!.name)
        repo.applyRemote(RemoteChange.Upserted(older.copy(name = "New", updatedAt = 3_000)))
        assertEquals("New", dao.getById("a")!!.name)
    }

    @Test
    fun `remote removal deletes synced row`() = runTest {
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        repo.applyRemote(RemoteChange.Removed("a"))
        assertNull(dao.getById("a"))
    }

    @Test
    fun `download fills an empty phone`() = runTest {
        remote.docs["r1"] = entry(id = "r1").toEntity(SyncState.SYNCED)
        val repo = repository()
        repo.downloadIfEmpty("u1")
        assertEquals("SYNCED", dao.getById("r1")!!.syncState)
    }

    @Test
    fun `sign out keeps pending rows and drops synced ones`() = runTest {
        val repo = repository()
        repo.log(entry(id = "synced"))
        advanceUntilIdle()
        remote.offline = true
        repo.log(entry(id = "pending"))
        advanceUntilIdle()
        repo.onSignOut("u1")
        assertNull(dao.getById("synced"))
        assertEquals("PENDING", dao.getById("pending")!!.syncState)
    }
}
```

- [ ] **Step 4: Ejecutar y ver que fallan**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.data.repository.MealRepositoryImplTest"`
Expected: FAIL de compilación (`Unresolved reference 'MealRepositoryImpl'` o firma distinta).

- [ ] **Step 5: Implementar**

`PKG/data/repository/SyncScheduler.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.repository

/** Programa un reintento de subida para cuando vuelva la conexión. */
fun interface SyncScheduler {
    fun schedule()
}
```

Reemplaza `PKG/data/repository/MealRepositoryImpl.kt` por:

```kotlin
package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.dao.MealLogDao
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.data.mapper.toDomain
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.data.remote.MealLogRemoteDataSource
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalDate

/**
 * Room es lo que leen las pantallas; Firestore es la copia remota (spec de Nutrición §5).
 * Las escrituras se guardan primero en Room y se suben en [scope] sin bloquear la UI.
 */
class MealRepositoryImpl(
    private val mealDao: MealDao,
    private val logDao: MealLogDao,
    private val remote: MealLogRemoteDataSource,
    private val scheduler: SyncScheduler,
    private val scope: CoroutineScope,
    private val pushTimeoutMs: Long = 5_000,
    private val now: () -> Long = System::currentTimeMillis
) : MealRepository {

    override fun observeCatalog(): Flow<List<Meal>> =
        mealDao.observeAll().map { meals -> meals.map { it.toDomain() } }

    override fun observeMeal(id: String): Flow<Meal?> =
        mealDao.observeById(id).map { it?.toDomain() }

    override fun observeLogs(userId: String, date: LocalDate): Flow<List<MealLog>> =
        logDao.observeByDate(userId, date.toString()).map { logs -> logs.map { it.toDomain() } }

    override fun observeLogs(userId: String, from: LocalDate, to: LocalDate): Flow<List<MealLog>> =
        logDao.observeRange(userId, from.toString(), to.toString()).map { logs -> logs.map { it.toDomain() } }

    override suspend fun log(entry: MealLog): Result<String> = try {
        val entity = entry.toEntity(SyncState.PENDING)
        logDao.upsert(entity)
        scope.launch { pushOrSchedule(entity) }
        Result.Success(entry.id)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.message ?: "Could not save the meal")
    }

    override suspend fun delete(logId: String): Result<Unit> = try {
        val current = logDao.getById(logId)
        if (current == null) {
            Result.Error("Meal log not found")
        } else {
            val tombstone = current.copy(deleted = true, updatedAt = now(), syncState = SyncState.PENDING.name)
            logDao.upsert(tombstone)
            scope.launch { pushOrSchedule(tombstone) }
            Result.Success(Unit)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.message ?: "Could not delete the meal")
    }

    /** Sube todos los pendientes del usuario. Devuelve false si alguno falló. */
    suspend fun syncPending(userId: String): Boolean {
        if (userId == MealLog.LOCAL_USER_ID) return true
        var allOk = true
        logDao.getPending(userId).forEach { entity ->
            try {
                push(entity)
            } catch (_: TimeoutCancellationException) {
                allOk = false
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                allOk = false
            }
        }
        return allOk
    }

    /** Descarga inicial: solo si el teléfono no tiene registros de ese usuario. */
    suspend fun downloadIfEmpty(userId: String) {
        if (userId == MealLog.LOCAL_USER_ID || logDao.countForUser(userId) > 0) return
        try {
            logDao.upsertAll(remote.fetchAll(userId).map { it.copy(syncState = SyncState.SYNCED.name) })
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Sin conexión: el listener completa los datos cuando vuelva la red.
        }
    }

    /** Escucha cambios de otros dispositivos hasta que se cancele la corrutina. */
    suspend fun listenRemote(userId: String) {
        if (userId == MealLog.LOCAL_USER_ID) return
        try {
            remote.listen(userId).collect { changes -> changes.forEach { applyRemote(it) } }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // El listener se reinicia en el próximo inicio de sesión.
        }
    }

    /** Last-write-wins por updatedAt. */
    suspend fun applyRemote(change: RemoteChange) {
        when (change) {
            is RemoteChange.Upserted -> {
                val local = logDao.getById(change.log.id)
                if (local == null || change.log.updatedAt > local.updatedAt) {
                    logDao.upsert(change.log.copy(syncState = SyncState.SYNCED.name))
                }
            }
            is RemoteChange.Removed -> {
                val local = logDao.getById(change.id)
                if (local != null && local.syncState == SyncState.SYNCED.name) logDao.hardDelete(change.id)
            }
        }
    }

    /** Intenta subir los pendientes y borra de Room solo lo ya sincronizado. */
    suspend fun onSignOut(userId: String) {
        syncPending(userId)
        logDao.deleteSyncedForUser(userId)
    }

    private suspend fun pushOrSchedule(entity: MealLogEntity) {
        if (entity.userId == MealLog.LOCAL_USER_ID) return
        try {
            push(entity)
        } catch (_: TimeoutCancellationException) {
            scheduler.schedule()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            scheduler.schedule()
        }
    }

    private suspend fun push(entity: MealLogEntity) {
        withTimeout(pushTimeoutMs) {
            if (entity.deleted) {
                remote.delete(entity.userId, entity.id)
                logDao.hardDelete(entity.id)
            } else {
                remote.upsert(entity)
                logDao.markSynced(entity.id, entity.updatedAt)
            }
        }
    }
}
```

Por qué `withTimeout`: con Firestore sin conexión, `set().await()` no termina hasta que vuelve la red (el SDK guarda la escritura en su propia cola). El timeout lo convierte en un fallo para que el registro quede `PENDING` y el worker reintente. Subir dos veces el mismo documento es inofensivo porque `set` es idempotente. `withTimeout` lanza `TimeoutCancellationException`, que hereda de `CancellationException`; por eso se captura antes y se trata como fallo, mientras que las demás cancelaciones se relanzan.

- [ ] **Step 6: Ejecutar las pruebas**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.data.repository.MealRepositoryImplTest"`
Expected: PASS (9 pruebas).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/domain/repository/MealRepository.kt app/src/main/java/co/edu/unal/dadm/mova/data/repository app/src/test/java/co/edu/unal/dadm/mova/data/repository
git commit -m "[Add] Meal repository with Room cache and Firestore sync"
```

---

### Task 9: Casos de uso (registrar, ingesta del día y sugerencias)

**Files:**
- Create: `PKG/domain/repository/BurnedKcalProvider.kt`, `PKG/domain/usecase/SuggestMealsUseCase.kt`
- Modify (stubs): `PKG/domain/usecase/LogMealUseCase.kt`, `PKG/domain/usecase/ObserveDailyIntakeUseCase.kt`
- Create (utilidades de prueba, las reutilizan las Tasks 12–17 y el plan de Búsqueda): `TEST/testutil/FakeMealRepository.kt`, `TEST/testutil/FakeProfileRepository.kt`, `TEST/testutil/TestMeals.kt`, `TEST/testutil/MainDispatcherRule.kt`
- Test: `TEST/domain/usecase/LogMealUseCaseTest.kt`, `TEST/domain/usecase/ObserveDailyIntakeUseCaseTest.kt`, `TEST/domain/usecase/SuggestMealsUseCaseTest.kt`

**Interfaces:**
- Consumes: `MealRepository` (Task 8), `ProfileRepository.observe(userId): Flow<UserProfile?>` (existente), `CalculateCalorieGoalUseCase` (Task 4).
- Produces:
  - `fun interface BurnedKcalProvider { fun observeBurnedKcal(userId: String, date: LocalDate): Flow<Int> }` con `BurnedKcalProvider.NONE` (siempre 0)
  - `class LogMealUseCase(repository: MealRepository) { suspend operator fun invoke(entry: MealLog): Result<String> }`
  - `class ObserveDailyIntakeUseCase(mealRepository, profileRepository, burnedKcal: BurnedKcalProvider, calculateGoal: CalculateCalorieGoalUseCase, currentUserId: () -> String) { operator fun invoke(date: LocalDate): Flow<DailyIntake> }`
  - `class SuggestMealsUseCase { operator fun invoke(catalog: List<Meal>, diet: Diet?, intake: DailyIntake?, limit: Int = 5): List<Meal> }`
  - Test utils: `FakeMealRepository(catalog)` con `catalog`, `logs` (`MutableStateFlow`) y `logResult`; `FakeProfileRepository(profile)`; `TestMeals.meal(...)`, `TestMeals.log(...)`, `TestMeals.VEGAN`, `VEGETARIAN`, `MEAT`; `MainDispatcherRule`

- [ ] **Step 1: Utilidades de prueba**

`TEST/testutil/MainDispatcherRule.kt`:

```kotlin
package co.edu.unal.dadm.mova.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Reemplaza Dispatchers.Main para que viewModelScope funcione en pruebas JVM. */
class MainDispatcherRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

`TEST/testutil/FakeMealRepository.kt`:

```kotlin
package co.edu.unal.dadm.mova.testutil

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeMealRepository(catalog: List<Meal> = emptyList()) : MealRepository {
    val catalog = MutableStateFlow(catalog)
    val logs = MutableStateFlow<List<MealLog>>(emptyList())
    /** Si no es null, log() devuelve este resultado sin guardar nada. */
    var logResult: Result<String>? = null

    override fun observeCatalog(): Flow<List<Meal>> = catalog
    override fun observeMeal(id: String): Flow<Meal?> = catalog.map { all -> all.firstOrNull { it.id == id } }
    override fun observeLogs(userId: String, date: LocalDate): Flow<List<MealLog>> =
        logs.map { all -> all.filter { it.userId == userId && it.date == date } }
    override fun observeLogs(userId: String, from: LocalDate, to: LocalDate): Flow<List<MealLog>> =
        logs.map { all -> all.filter { it.userId == userId && !it.date.isBefore(from) && !it.date.isAfter(to) } }

    override suspend fun log(entry: MealLog): Result<String> {
        logResult?.let { return it }
        logs.value = logs.value + entry
        return Result.Success(entry.id)
    }

    override suspend fun delete(logId: String): Result<Unit> {
        logs.value = logs.value.filterNot { it.id == logId }
        return Result.Success(Unit)
    }
}
```

`TEST/testutil/FakeProfileRepository.kt`:

```kotlin
package co.edu.unal.dadm.mova.testutil

import android.net.Uri
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProfileRepository(profile: UserProfile? = null) : ProfileRepository {
    val profile = MutableStateFlow(profile)
    override fun observe(userId: String): Flow<UserProfile?> = profile
    override suspend fun update(userId: String, changes: Map<String, Any?>) = Unit
    override suspend fun saveAvatar(uri: Uri, contentType: String): String = ""
}
```

`TEST/testutil/TestMeals.kt`:

```kotlin
package co.edu.unal.dadm.mova.testutil

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.Instant
import java.time.LocalDate

object TestMeals {
    val VEGAN = setOf(Diet.VEGAN, Diet.VEGETARIAN, Diet.CARNIVORE)
    val VEGETARIAN = setOf(Diet.VEGETARIAN, Diet.CARNIVORE)
    val MEAT = setOf(Diet.CARNIVORE)

    fun meal(
        id: String,
        name: String = "Meal $id",
        protein: Double = 10.0,
        carbs: Double = 10.0,
        fat: Double = 5.0,
        diets: Set<Diet> = MEAT,
        category: MealCategory = MealCategory.PROTEIN,
        suitableFor: Set<MealType> = setOf(MealType.LUNCH),
        servingGrams: Int = 100,
        tags: List<String> = emptyList()
    ): Meal {
        val macros = Macros(protein, carbs, fat)
        return Meal(id, name, category, suitableFor, diets, servingGrams, macros, macros.kcal, 10, id, tags)
    }

    fun log(
        id: String,
        date: LocalDate,
        protein: Double = 10.0,
        carbs: Double = 10.0,
        fat: Double = 5.0,
        mealType: MealType = MealType.LUNCH,
        userId: String = "u1"
    ) = MealLog.manual("Log $id", Macros(protein, carbs, fat), userId, date, mealType, Instant.EPOCH, id = id)
}
```

- [ ] **Step 2: Escribir las pruebas que fallan**

`TEST/domain/usecase/LogMealUseCaseTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LogMealUseCaseTest {
    @Test
    fun `delegates to the repository`() = runTest {
        val repo = FakeMealRepository()
        val entry = TestMeals.log("a", LocalDate.of(2026, 11, 2))
        assertEquals(Result.Success("a"), LogMealUseCase(repo)(entry))
        assertEquals(listOf(entry), repo.logs.value)
    }
}
```

`TEST/domain/usecase/ObserveDailyIntakeUseCaseTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.FakeProfileRepository
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ObserveDailyIntakeUseCaseTest {
    private val date = LocalDate.of(2026, 11, 2)
    private val profile = UserProfile(
        userId = "u1", gender = Gender.MALE, age = 30, weightKg = 80.0, heightCm = 180,
        fitnessLevel = FitnessLevel.INTERMEDIATE, goal = Goal.LOSE_WEIGHT
    )
    private val meals = FakeMealRepository().apply {
        logs.value = listOf(
            TestMeals.log("a", date, protein = 30.0, carbs = 50.0, fat = 10.0, mealType = MealType.BREAKFAST), // 410
            TestMeals.log("b", date, protein = 20.0, carbs = 20.0, fat = 10.0, mealType = MealType.LUNCH),     // 250
            TestMeals.log("c", date.minusDays(1))
        )
    }

    private fun useCase(burned: BurnedKcalProvider = BurnedKcalProvider.NONE) =
        ObserveDailyIntakeUseCase(meals, FakeProfileRepository(profile), burned, CalculateCalorieGoalUseCase()) { "u1" }

    @Test
    fun `sums the day and subtracts from the goal`() = runTest {
        val intake = useCase()(date).first()
        assertEquals(660, intake.consumedKcal)
        assertEquals(2350, intake.goalKcal)
        assertEquals(1690, intake.remainingKcal)
        assertEquals(50.0, intake.consumed.proteinG, 0.001)
        assertEquals(setOf(MealType.BREAKFAST, MealType.LUNCH), intake.byMealType.keys)
    }

    @Test
    fun `burned kcal raise the goal`() = runTest {
        val intake = useCase { _, _ -> flowOf(300) }(date).first()
        assertEquals(300, intake.burnedKcal)
        assertEquals(2650, intake.goalKcal)
    }

    @Test
    fun `remaining can be negative`() = runTest {
        meals.logs.value = listOf(TestMeals.log("big", date, protein = 200.0, carbs = 400.0, fat = 100.0)) // 3300
        assertEquals(-950, useCase()(date).first().remainingKcal)
    }
}
```

`TEST/domain/usecase/SuggestMealsUseCaseTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.DailyIntake
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SuggestMealsUseCaseTest {
    private val suggest = SuggestMealsUseCase()
    private val vegan = TestMeals.meal("vegan", diets = TestMeals.VEGAN, protein = 5.0)
    private val veggie = TestMeals.meal("veggie", diets = TestMeals.VEGETARIAN, protein = 20.0)
    private val meat = TestMeals.meal("meat", diets = TestMeals.MEAT, protein = 40.0)
    private val catalog = listOf(vegan, veggie, meat)

    private fun intake(consumedProtein: Double, remainingKcal: Int) = DailyIntake(
        date = LocalDate.of(2026, 11, 2), consumed = Macros(consumedProtein, 0.0, 0.0), consumedKcal = 0,
        goalKcal = 2000, burnedKcal = 0, remainingKcal = remainingKcal, goalMacros = Macros(100.0, 200.0, 60.0), byMealType = emptyMap()
    )

    @Test
    fun `vegan only sees vegan meals`() {
        assertEquals(listOf(vegan), suggest(catalog, Diet.VEGAN, null))
    }

    @Test
    fun `vegetarian sees vegan and vegetarian`() {
        assertEquals(setOf(vegan, veggie), suggest(catalog, Diet.VEGETARIAN, null).toSet())
    }

    @Test
    fun `null diet and carnivore see everything`() {
        assertEquals(3, suggest(catalog, null, null).size)
        assertEquals(3, suggest(catalog, Diet.CARNIVORE, null).size)
    }

    @Test
    fun `protein first when more than 30 percent is missing`() {
        // faltan 60 g de 100 g
        assertEquals(listOf(meat, veggie, vegan), suggest(catalog, null, intake(consumedProtein = 40.0, remainingKcal = 500)))
    }

    @Test
    fun `otherwise closest kcal to what is left`() {
        // faltan 10 g de 100 g; kcal: vegan 105, veggie 165, meat 245
        assertEquals(listOf(veggie, meat, vegan), suggest(catalog, null, intake(consumedProtein = 90.0, remainingKcal = 180)))
    }

    @Test
    fun `at most five`() {
        val many = (1..8).map { TestMeals.meal("m$it") }
        assertEquals(5, suggest(many, null, null).size)
    }
}
```

- [ ] **Step 3: Ejecutar y ver que fallan**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.domain.usecase.*"`
Expected: FAIL de compilación (`Unresolved reference 'BurnedKcalProvider'`, etc.).

- [ ] **Step 4: Implementar**

`PKG/domain/repository/BurnedKcalProvider.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/** Puerto hacia Actividad (D3). Mientras Actividad no lo exponga, se usa NONE y la meta es fija. */
fun interface BurnedKcalProvider {
    fun observeBurnedKcal(userId: String, date: LocalDate): Flow<Int>

    companion object {
        val NONE = BurnedKcalProvider { _, _ -> flowOf(0) }
    }
}
```

`PKG/domain/usecase/LogMealUseCase.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.MealRepository

// Registrar desde cualquier pantalla (catálogo, manual, Coach)
class LogMealUseCase(private val repository: MealRepository) {
    suspend operator fun invoke(entry: MealLog): Result<String> = repository.log(entry)
}
```

`PKG/domain/usecase/ObserveDailyIntakeUseCase.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.DailyIntake
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

// Ingesta, meta y restante de un día
class ObserveDailyIntakeUseCase(
    private val mealRepository: MealRepository,
    private val profileRepository: ProfileRepository,
    private val burnedKcal: BurnedKcalProvider,
    private val calculateGoal: CalculateCalorieGoalUseCase,
    private val currentUserId: () -> String
) {
    operator fun invoke(date: LocalDate): Flow<DailyIntake> {
        val userId = currentUserId()
        // Sin perfil (sin sesión o error de red) se usan los valores por defecto de la meta.
        val profile = profileRepository.observe(userId).catch { emit(null) }
        return combine(
            mealRepository.observeLogs(userId, date),
            profile,
            burnedKcal.observeBurnedKcal(userId, date)
        ) { logs, userProfile, burned ->
            val consumed = logs.fold(Macros.ZERO) { total, log -> total + log.macros }
            val consumedKcal = logs.sumOf { it.kcal }
            val goal = calculateGoal(userProfile, burned)
            DailyIntake(
                date = date,
                consumed = consumed,
                consumedKcal = consumedKcal,
                goalKcal = goal.kcal,
                burnedKcal = burned,
                remainingKcal = goal.kcal - consumedKcal,
                goalMacros = goal.macros,
                byMealType = logs.groupBy { it.mealType }
            )
        }
    }
}
```

`PKG/domain/usecase/SuggestMealsUseCase.kt`:

```kotlin
package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.DailyIntake
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Meal
import kotlin.math.abs

/**
 * Sugerencias (spec §3): se descartan comidas incompatibles con la dieta (null = sin restricción)
 * y se ordenan por proteína si falta más del 30 % de su meta; si no, por cercanía a las kcal restantes.
 */
class SuggestMealsUseCase {
    operator fun invoke(catalog: List<Meal>, diet: Diet?, intake: DailyIntake?, limit: Int = 5): List<Meal> {
        val allowed = if (diet == null) catalog else catalog.filter { diet in it.dietTags }
        if (intake == null) return allowed.take(limit)
        val proteinGoal = intake.goalMacros.proteinG
        val missingProtein = proteinGoal - intake.consumed.proteinG
        val sorted = if (missingProtein > proteinGoal * 0.3) {
            allowed.sortedByDescending { it.macros.proteinG }
        } else {
            val remaining = intake.remainingKcal.coerceAtLeast(0)
            allowed.sortedBy { abs(it.kcal - remaining) }
        }
        return sorted.take(limit)
    }
}
```

- [ ] **Step 5: Ejecutar las pruebas**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.domain.usecase.*"`
Expected: PASS (todas las de `domain.usecase`, incluidas las de las Tasks 2 y 4).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/domain app/src/test/java/co/edu/unal/dadm/mova/domain app/src/test/java/co/edu/unal/dadm/mova/testutil
git commit -m "[Add] Log meal, daily intake and suggestion use cases"
```

---

### Task 10: Worker de sincronización, coordinador de sesión y `NutritionContainer`

**Files:**
- Create: `PKG/core/work/SyncMealLogsWorker.kt`, `PKG/core/work/MealLogSyncScheduler.kt`, `PKG/data/repository/MealSyncCoordinator.kt`, `PKG/core/di/NutritionContainer.kt`
- Modify: `PKG/MovaApp.kt`
- Test: `TEST/data/repository/MealSyncCoordinatorTest.kt`

**Interfaces:**
- Consumes: `MealRepositoryImpl` (Task 8), `firebaseAuthUserIds()` y `FirestoreMealLogDataSource` (Task 7), casos de uso (Tasks 4 y 9), `MovaDatabase` (Task 5), `ProfileRepositoryImpl` (existente).
- Produces:
  - `class MealSyncCoordinator(repository: MealRepositoryImpl, userIds: Flow<String?>, scope: CoroutineScope) { fun start(): Job }`
  - `class NutritionContainer` con `NutritionContainer.get(context)` y las propiedades `currentUserId: () -> String`, `mealRepository: MealRepositoryImpl`, `profileRepository: ProfileRepository`, `calculateKcal`, `logMeal`, `observeDailyIntake`, `suggestMeals`, `syncCoordinator`

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/data/repository/MealSyncCoordinatorTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MealSyncCoordinatorTest {
    private val dao = FakeMealLogDao()
    private val remote = FakeMealLogRemote()
    private val date = LocalDate.of(2026, 11, 2)
    private val noMeals = object : MealDao {
        override fun observeAll(): Flow<List<MealEntity>> = emptyFlow()
        override fun observeById(id: String): Flow<MealEntity?> = emptyFlow()
        override suspend fun insertAll(meals: List<MealEntity>) = Unit
        override suspend fun count() = 0
    }

    @Test
    fun `downloads on sign in, applies remote changes and cleans up on sign out`() = runTest {
        remote.docs["r1"] = TestMeals.log("r1", date).toEntity(SyncState.SYNCED)
        val repo = MealRepositoryImpl(noMeals, dao, remote, {}, backgroundScope)
        val userIds = MutableStateFlow<String?>("u1")
        MealSyncCoordinator(repo, userIds, backgroundScope).start()
        advanceUntilIdle()
        assertEquals("SYNCED", dao.getById("r1")!!.syncState)

        remote.changes.emit(listOf(RemoteChange.Upserted(TestMeals.log("r2", date).toEntity(SyncState.SYNCED))))
        advanceUntilIdle()
        assertEquals("r2", dao.getById("r2")!!.id)

        userIds.value = null
        advanceUntilIdle()
        assertNull(dao.getById("r1"))
        assertNull(dao.getById("r2"))
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.data.repository.MealSyncCoordinatorTest"`
Expected: FAIL de compilación (`Unresolved reference 'MealSyncCoordinator'`).

- [ ] **Step 3: Coordinador**

`PKG/data/repository/MealSyncCoordinator.kt`:

```kotlin
package co.edu.unal.dadm.mova.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Reacciona a los cambios de sesión (spec §5): al iniciar sesión descarga, sube pendientes y escucha
 * cambios remotos; al cerrar sesión limpia los registros sincronizados del usuario anterior.
 */
class MealSyncCoordinator(
    private val repository: MealRepositoryImpl,
    private val userIds: Flow<String?>,
    private val scope: CoroutineScope
) {
    fun start(): Job = scope.launch {
        var previous: String? = null
        var listener: Job? = null
        userIds.distinctUntilChanged().collect { userId ->
            listener?.cancel()
            previous?.let { repository.onSignOut(it) }
            if (userId != null) {
                repository.downloadIfEmpty(userId)
                repository.syncPending(userId)
                listener = launch { repository.listenRemote(userId) }
            }
            previous = userId
        }
    }
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.data.repository.MealSyncCoordinatorTest"`
Expected: PASS.

- [ ] **Step 5: Worker y scheduler**

`PKG/core/work/MealLogSyncScheduler.kt`:

```kotlin
package co.edu.unal.dadm.mova.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import co.edu.unal.dadm.mova.data.repository.SyncScheduler

/** Encola SyncMealLogsWorker con restricción de red; si ya hay uno en cola, no duplica. */
class MealLogSyncScheduler(private val context: Context) : SyncScheduler {
    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<SyncMealLogsWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private companion object {
        const val WORK_NAME = "sync_meal_logs"
    }
}
```

`PKG/core/work/SyncMealLogsWorker.kt`:

```kotlin
package co.edu.unal.dadm.mova.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.edu.unal.dadm.mova.core.di.NutritionContainer

/** Sube los registros PENDING del usuario con sesión cuando hay red; reintenta con backoff si falla. */
class SyncMealLogsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = NutritionContainer.get(applicationContext)
        val synced = container.mealRepository.syncPending(container.currentUserId())
        return if (synced) Result.success() else Result.retry()
    }
}
```

- [ ] **Step 6: Contenedor de dependencias**

`PKG/core/di/NutritionContainer.kt`:

```kotlin
package co.edu.unal.dadm.mova.core.di

import android.content.Context
import co.edu.unal.dadm.mova.core.work.MealLogSyncScheduler
import co.edu.unal.dadm.mova.data.database.MovaDatabase
import co.edu.unal.dadm.mova.data.remote.FirestoreMealLogDataSource
import co.edu.unal.dadm.mova.data.remote.firebaseAuthUserIds
import co.edu.unal.dadm.mova.data.repository.MealRepositoryImpl
import co.edu.unal.dadm.mova.data.repository.MealSyncCoordinator
import co.edu.unal.dadm.mova.data.repository.ProfileRepositoryImpl
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCase
import co.edu.unal.dadm.mova.domain.usecase.CalculateKcalFromMacrosUseCase
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.domain.usecase.SuggestMealsUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Ensamblado manual del módulo de Nutrición (el proyecto no usa Hilt). Una instancia por proceso. */
class NutritionContainer private constructor(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = MovaDatabase.getInstance(context)

    val currentUserId: () -> String = { FirebaseAuth.getInstance().currentUser?.uid ?: MealLog.LOCAL_USER_ID }

    val mealRepository = MealRepositoryImpl(
        mealDao = database.mealDao(),
        logDao = database.mealLogDao(),
        remote = FirestoreMealLogDataSource(),
        scheduler = MealLogSyncScheduler(context),
        scope = appScope
    )
    val profileRepository: ProfileRepository = ProfileRepositoryImpl(context)

    val calculateKcal = CalculateKcalFromMacrosUseCase()
    val logMeal = LogMealUseCase(mealRepository)
    val observeDailyIntake = ObserveDailyIntakeUseCase(
        mealRepository, profileRepository, BurnedKcalProvider.NONE, CalculateCalorieGoalUseCase(), currentUserId
    )
    val suggestMeals = SuggestMealsUseCase()

    val syncCoordinator = MealSyncCoordinator(mealRepository, firebaseAuthUserIds(), appScope)

    companion object {
        @Volatile
        private var instance: NutritionContainer? = null

        fun get(context: Context): NutritionContainer =
            instance ?: synchronized(this) {
                instance ?: NutritionContainer(context.applicationContext).also { instance = it }
            }
    }
}
```

Cuando Actividad exponga `observeBurnedKcal` (D3), solo se cambia `BurnedKcalProvider.NONE` por su implementación en este archivo.

- [ ] **Step 7: Arrancar el coordinador**

Reemplaza `PKG/MovaApp.kt` por:

```kotlin
package co.edu.unal.dadm.mova

import android.app.Application
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import com.google.firebase.FirebaseApp

class MovaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NutritionContainer.get(this).syncCoordinator.start()
    }
}
```

- [ ] **Step 8: Compilar**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/co/edu/unal/dadm/mova app/src/test/java/co/edu/unal/dadm/mova/data/repository/MealSyncCoordinatorTest.kt
git commit -m "[Add] Meal log sync worker, session coordinator and nutrition container"
```

---

### Task 11: Infraestructura de UI y navegación del módulo

**Files:**
- Create: `PKG/presentation/viewmodel/nutrition/NutritionViewModelFactory.kt`, `PKG/presentation/screen/nutrition/components/MealImage.kt`, `PKG/presentation/screen/nutrition/components/MealTypeChips.kt`, `PKG/presentation/screen/nutrition/components/MacroBar.kt`, `PKG/presentation/navigation/NutritionNavGraph.kt`
- Modify: `PKG/presentation/navigation/Routes.kt`, `PKG/presentation/navigation/MovaNavHost.kt`

**Interfaces:**
- Consumes: `NutritionContainer` (Task 10), `ImagePlaceholder` (existente).
- Produces:
  - `class NutritionViewModelFactory<VM : ViewModel>(creator: () -> VM) : ViewModelProvider.Factory`
  - `@Composable fun MealImage(name: String, imageRes: String, modifier: Modifier = Modifier)`
  - `@Composable fun MealTypeChips(selected: MealType?, onSelect: (MealType?) -> Unit, includeAll: Boolean, modifier: Modifier = Modifier)`
  - `@Composable fun MacroBar(label: String, value: Double, goal: Double, color: Color, modifier: Modifier = Modifier)`
  - Rutas: `Routes.NutritionIntake`, `NutritionMyMeals`, `NutritionMeal`, `NutritionAdd`, `NutritionAdded`, `NutritionExplore`, `NutritionStats` y los constructores `Routes.nutritionMyMeals(date)`, `nutritionMeal(mealId, date)`, `nutritionAdd(type, date)`, `nutritionAdded(date)`
  - `fun NavGraphBuilder.nutritionGraph(navController: NavController, container: NutritionContainer)` y `fun NavBackStackEntry.dateArg(): LocalDate`

Esta tarea es infraestructura sin lógica propia; se verifica compilando. Las pantallas de las Tasks 12–17 agregan sus destinos a `nutritionGraph`.

- [ ] **Step 1: Factory**

`PKG/presentation/viewmodel/nutrition/NutritionViewModelFactory.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Crea cualquier ViewModel del módulo a partir de una lambda (sin Hilt). */
class NutritionViewModelFactory<VM : ViewModel>(private val creator: () -> VM) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
```

- [ ] **Step 2: Componentes**

`PKG/presentation/screen/nutrition/components/MealImage.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder

/** Imagen local del catálogo (res/drawable/{imageRes}); si aún no existe, muestra el nombre. */
@SuppressLint("DiscouragedApi")
@Composable
fun MealImage(name: String, imageRes: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawableId = remember(imageRes) {
        context.resources.getIdentifier(imageRes, "drawable", context.packageName).takeIf { it != 0 }
    }
    ImagePlaceholder(label = name, modifier = modifier, drawableResId = drawableId)
}
```

`PKG/presentation/screen/nutrition/components/MealTypeChips.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.domain.model.MealType

@Composable
fun MealTypeChips(
    selected: MealType?,
    onSelect: (MealType?) -> Unit,
    includeAll: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (includeAll) {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        }
        MealType.entries.forEach { type ->
            FilterChip(selected = selected == type, onClick = { onSelect(type) }, label = { Text(type.label) })
        }
    }
}
```

`PKG/presentation/screen/nutrition/components/MacroBar.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun MacroBar(label: String, value: Double, goal: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("${value.roundToInt()} / ${goal.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { if (goal <= 0) 0f else (value / goal).toFloat().coerceIn(0f, 1f) },
            color = color,
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
    }
}
```

- [ ] **Step 3: Rutas**

En `PKG/presentation/navigation/Routes.kt`, agrega el import `import co.edu.unal.dadm.mova.domain.model.MealType` y `import java.time.LocalDate`, y dentro de `object Routes` (después de `Premium`):

```kotlin
    // Nutrición (spec §6). date es ISO (2026-11-02) y opcional: por defecto, hoy.
    const val NutritionIntake = "nutrition/intake"
    const val NutritionMyMeals = "nutrition/my_meals?date={date}"
    const val NutritionMeal = "nutrition/meal/{mealId}?date={date}"
    const val NutritionAdd = "nutrition/add?type={type}&date={date}"
    const val NutritionAdded = "nutrition/added?date={date}"
    const val NutritionExplore = "nutrition/explore"
    const val NutritionStats = "nutrition/stats"

    fun nutritionMyMeals(date: LocalDate? = null) = "nutrition/my_meals" + query("date" to date)
    fun nutritionMeal(mealId: String, date: LocalDate? = null) = "nutrition/meal/$mealId" + query("date" to date)
    fun nutritionAdd(type: MealType? = null, date: LocalDate? = null) =
        "nutrition/add" + query("type" to type?.name, "date" to date)
    fun nutritionAdded(date: LocalDate) = "nutrition/added?date=$date"

    private fun query(vararg params: Pair<String, Any?>): String =
        params.filter { it.second != null }
            .joinToString("&") { "${it.first}=${it.second}" }
            .let { if (it.isEmpty()) "" else "?$it" }
```

- [ ] **Step 4: Grafo de Nutrición y conexión con `MovaNavHost`**

`PKG/presentation/navigation/NutritionNavGraph.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import java.time.LocalDate

/** Destinos de Nutrición. Cada pantalla agrega aquí su composable (Tasks 12–17). */
fun NavGraphBuilder.nutritionGraph(navController: NavController, container: NutritionContainer) {
}

internal val optionalDateArg = navArgument("date") {
    type = NavType.StringType
    nullable = true
    defaultValue = null
}

internal fun NavBackStackEntry.dateArg(): LocalDate =
    arguments?.getString("date")?.let(LocalDate::parse) ?: LocalDate.now()
```

En `PKG/presentation/navigation/MovaNavHost.kt`:

1. Agrega el import `import co.edu.unal.dadm.mova.core.di.NutritionContainer` y `import androidx.compose.runtime.remember`.
2. Debajo de `val preferencesRepository = UserPreferencesImpl(context)` agrega:

```kotlin
    val nutritionContainer = remember { NutritionContainer.get(context) }
```

3. Al final del bloque `NavHost(...) { ... }`, después de `composable(Routes.Premium) { ... }`, agrega:

```kotlin
        nutritionGraph(navController, nutritionContainer)
```

- [ ] **Step 5: Compilar**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL` (puede haber avisos de parámetros sin usar en `nutritionGraph`; desaparecen con la Task 12).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/presentation
git commit -m "[Add] Nutrition navigation and shared UI components"
```

---

### Task 12: Detalle de comida (N2) y confirmación (N4)

**Files:**
- Modify (stubs): `PKG/presentation/viewmodel/nutrition/MealDetailUiState.kt`, `MealDetailEvent.kt`, `MealDetailViewModel.kt`, `PKG/presentation/screen/nutrition/MealDetailScreen.kt`, `PKG/presentation/screen/nutrition/FoodAddedScreen.kt`
- Modify: `PKG/presentation/navigation/NutritionNavGraph.kt`
- Test: `TEST/presentation/viewmodel/nutrition/MealDetailViewModelTest.kt`

**Interfaces:**
- Consumes: `MealRepository.observeMeal`, `LogMealUseCase`, `MealLog.fromMeal`, `MealImage`, `MealTypeChips`, `NutritionViewModelFactory`, `Routes.nutritionAdded`, `Routes.nutritionMyMeals`.
- Produces: `MealDetailViewModel(mealId: String, date: LocalDate, today: LocalDate, mealRepository: MealRepository, logMeal: LogMealUseCase, currentUserId: () -> String, clock: () -> Instant = Instant::now)`; destinos `Routes.NutritionMeal` y `Routes.NutritionAdded`.

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/presentation/viewmodel/nutrition/MealDetailViewModelTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealDetailViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 2)
    private val meal = TestMeals.meal("m1", protein = 10.0, carbs = 20.0, fat = 5.0, suitableFor = setOf(MealType.SNACK, MealType.LUNCH), servingGrams = 100)
    private val repo = FakeMealRepository(listOf(meal))

    private fun viewModel(date: LocalDate = today, mealId: String = "m1") =
        MealDetailViewModel(mealId, date, today, repo, LogMealUseCase(repo), { "u1" }, { Instant.EPOCH })

    @Test
    fun `starts with base serving and first suitable meal type`() {
        val state = viewModel().state.value
        assertEquals(100, state.grams)
        assertEquals(165, state.kcal)
        assertEquals(MealType.LUNCH, state.mealType)
    }

    @Test
    fun `changing grams scales macros`() {
        val vm = viewModel()
        vm.onEvent(MealDetailEvent.GramsChanged(150))
        assertEquals(15.0, vm.state.value.macros.proteinG, 0.001)
        assertEquals(248, vm.state.value.kcal)
    }

    @Test
    fun `add meal logs a catalog entry on the selected date`() {
        val vm = viewModel(date = today.minusDays(2))
        vm.onEvent(MealDetailEvent.MealTypeChanged(MealType.DINNER))
        vm.onEvent(MealDetailEvent.AddMeal)
        val log = repo.logs.value.single()
        assertEquals(MealSource.CATALOG, log.source)
        assertEquals(today.minusDays(2), log.date)
        assertEquals(MealType.DINNER, log.mealType)
        assertEquals(today.minusDays(2), vm.state.value.addedOn)
    }

    @Test
    fun `future dates are logged today`() {
        val vm = viewModel(date = today.plusDays(3))
        vm.onEvent(MealDetailEvent.AddMeal)
        assertEquals(today, repo.logs.value.single().date)
    }

    @Test
    fun `failed log shows error and does not navigate`() {
        repo.logResult = Result.Error("boom")
        val vm = viewModel()
        vm.onEvent(MealDetailEvent.AddMeal)
        assertEquals("boom", vm.state.value.error)
        assertNull(vm.state.value.addedOn)
    }

    @Test
    fun `unknown meal shows error`() {
        assertEquals("Meal not found", viewModel(mealId = "nope").state.value.error)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailViewModelTest"`
Expected: FAIL de compilación.

- [ ] **Step 3: Estado, eventos y ViewModel**

`PKG/presentation/viewmodel/nutrition/MealDetailUiState.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

data class MealDetailUiState(
    val meal: Meal? = null,
    val grams: Int = 0,
    val macros: Macros = Macros.ZERO,
    val kcal: Int = 0,
    val mealType: MealType = MealType.BREAKFAST,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** Fecha en que se registró; la pantalla navega a «Food Added!» cuando deja de ser null. */
    val addedOn: LocalDate? = null,
    val error: String? = null
)
```

`PKG/presentation/viewmodel/nutrition/MealDetailEvent.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType

sealed interface MealDetailEvent {
    data class GramsChanged(val grams: Int) : MealDetailEvent
    data class MealTypeChanged(val mealType: MealType) : MealDetailEvent
    data object AddMeal : MealDetailEvent
    data object ErrorShown : MealDetailEvent
}
```

`PKG/presentation/viewmodel/nutrition/MealDetailViewModel.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class MealDetailViewModel(
    mealId: String,
    date: LocalDate,
    today: LocalDate,
    private val mealRepository: MealRepository,
    private val logMeal: LogMealUseCase,
    private val currentUserId: () -> String,
    private val clock: () -> Instant = Instant::now
) : ViewModel() {

    /** No se registra en días futuros (spec §3). */
    private val logDate = if (date.isAfter(today)) today else date

    private val _state = MutableStateFlow(MealDetailUiState())
    val state: StateFlow<MealDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeMeal(mealId).collect { meal ->
                _state.update { current ->
                    when {
                        meal == null -> current.copy(isLoading = false, error = "Meal not found")
                        current.meal == null -> current.withPortion(meal, meal.servingGrams).copy(
                            isLoading = false,
                            mealType = meal.suitableFor.minOrNull() ?: MealType.SNACK
                        )
                        else -> current.withPortion(meal, current.grams)
                    }
                }
            }
        }
    }

    fun onEvent(event: MealDetailEvent) {
        when (event) {
            is MealDetailEvent.GramsChanged -> _state.update { current ->
                current.meal?.let { current.withPortion(it, event.grams.coerceIn(MIN_GRAMS, MAX_GRAMS)) } ?: current
            }
            is MealDetailEvent.MealTypeChanged -> _state.update { it.copy(mealType = event.mealType) }
            MealDetailEvent.AddMeal -> addMeal()
            MealDetailEvent.ErrorShown -> _state.update { it.copy(error = null) }
        }
    }

    private fun addMeal() {
        val current = _state.value
        val meal = current.meal ?: return
        if (current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val entry = MealLog.fromMeal(meal, current.grams, currentUserId(), logDate, current.mealType, clock())
            val result = logMeal(entry)
            _state.update {
                when (result) {
                    is Result.Success -> it.copy(isSaving = false, addedOn = logDate)
                    is Result.Error -> it.copy(isSaving = false, error = result.message)
                    Result.Loading -> it
                }
            }
        }
    }

    private fun MealDetailUiState.withPortion(meal: Meal, grams: Int): MealDetailUiState {
        val macros = meal.macros.scaled(grams.toDouble() / meal.servingGrams)
        return copy(meal = meal, grams = grams, macros = macros, kcal = macros.kcal)
    }

    private companion object {
        const val MIN_GRAMS = 1
        const val MAX_GRAMS = 2000
    }
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailViewModelTest"`
Expected: PASS (6 pruebas).

- [ ] **Step 5: Pantallas**

`PKG/presentation/screen/nutrition/MealDetailScreen.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealImage
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealTypeChips
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun MealDetailScreen(
    container: NutritionContainer,
    mealId: String,
    date: LocalDate,
    onBack: () -> Unit,
    onAdded: (LocalDate) -> Unit
) {
    val viewModel: MealDetailViewModel = viewModel(factory = NutritionViewModelFactory {
        MealDetailViewModel(mealId, date, LocalDate.now(), container.mealRepository, container.logMeal, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.addedOn) { state.addedOn?.let(onAdded) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBarBack(onBack = onBack, title = "Meal Detail")
        val meal = state.meal
        when {
            state.isLoading -> CircularProgressIndicator()
            meal == null -> Text(state.error ?: "Meal not found")
            else -> {
                MealImage(meal.name, meal.imageRes, Modifier.fillMaxWidth().height(220.dp))
                Text(meal.name, style = MaterialTheme.typography.headlineSmall)
                Text("${meal.category.label} · ${meal.prepMinutes} min", color = MovaMuted)
                MovaTextField(
                    value = state.grams.toString(),
                    onValueChange = { text -> text.filter(Char::isDigit).toIntOrNull()?.let { viewModel.onEvent(MealDetailEvent.GramsChanged(it)) } },
                    label = "Portion (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Protein ${state.macros.proteinG.roundToInt()} g")
                    Text("Carbs ${state.macros.carbsG.roundToInt()} g")
                    Text("Fat ${state.macros.fatG.roundToInt()} g")
                }
                Text("${state.kcal} kcal", style = MaterialTheme.typography.titleLarge)
                MealTypeChips(
                    selected = state.mealType,
                    onSelect = { type -> type?.let { viewModel.onEvent(MealDetailEvent.MealTypeChanged(it)) } },
                    includeAll = false
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                MovaPrimaryButton(
                    text = if (state.isSaving) "Adding..." else "Add Meal",
                    onClick = { viewModel.onEvent(MealDetailEvent.AddMeal) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
```

`PKG/presentation/screen/nutrition/FoodAddedScreen.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.presentation.components.SuccessDialog

@Composable
fun FoodAddedScreen(onBackToMyMeals: () -> Unit) {
    SuccessDialog(
        title = "Food Added!",
        message = "Your meal was added to My Meals.",
        buttonText = "Back to My Meals",
        onButtonClick = onBackToMyMeals,
        onClose = onBackToMyMeals
    )
}
```

- [ ] **Step 6: Registrar los destinos**

En `PKG/presentation/navigation/NutritionNavGraph.kt`, agrega los imports `androidx.navigation.compose.composable`, `co.edu.unal.dadm.mova.presentation.screen.nutrition.MealDetailScreen` y `co.edu.unal.dadm.mova.presentation.screen.nutrition.FoodAddedScreen`, y dentro de `nutritionGraph { }`:

```kotlin
    composable(
        Routes.NutritionMeal,
        arguments = listOf(navArgument("mealId") { type = NavType.StringType }, optionalDateArg)
    ) { entry ->
        MealDetailScreen(
            container = container,
            mealId = entry.arguments?.getString("mealId").orEmpty(),
            date = entry.dateArg(),
            onBack = { navController.popBackStack() },
            onAdded = { date ->
                navController.navigate(Routes.nutritionAdded(date)) {
                    popUpTo(Routes.NutritionMeal) { inclusive = true }
                }
            }
        )
    }
    composable(Routes.NutritionAdded, arguments = listOf(optionalDateArg)) { entry ->
        val date = entry.dateArg()
        FoodAddedScreen(onBackToMyMeals = {
            navController.navigate(Routes.nutritionMyMeals(date)) {
                popUpTo(Routes.NutritionAdded) { inclusive = true }
            }
        })
    }
```

- [ ] **Step 7: Compilar**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/presentation app/src/test/java/co/edu/unal/dadm/mova/presentation/viewmodel/nutrition
git commit -m "[Add] Meal detail and food added screens"
```

---

### Task 13: Registro manual (N3)

**Files:**
- Modify (stubs): `PKG/presentation/viewmodel/nutrition/AddMealUiState.kt`, `AddMealEvent.kt`, `AddMealViewModel.kt`, `PKG/presentation/screen/nutrition/AddMealScreen.kt`
- Modify: `PKG/presentation/navigation/NutritionNavGraph.kt`
- Test: `TEST/presentation/viewmodel/nutrition/AddMealViewModelTest.kt`

**Interfaces:**
- Consumes: `LogMealUseCase`, `CalculateKcalFromMacrosUseCase`, `MealLog.manual`.
- Produces: `AddMealViewModel(date: LocalDate, today: LocalDate, initialType: MealType?, logMeal: LogMealUseCase, calculateKcal: CalculateKcalFromMacrosUseCase, currentUserId: () -> String, clock: () -> Instant = Instant::now)`; destino `Routes.NutritionAdd`.

Validación (§3): nombre de 2 a 60 caracteres (sin contar espacios de los extremos); cada macro entre 0 y 300 g (vacío cuenta como 0); al menos un macro mayor que 0. Corrección del mockup (§10): sin pestañas, cuatro tipos de comida, campos numéricos.

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/presentation/viewmodel/nutrition/AddMealViewModelTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.usecase.CalculateKcalFromMacrosUseCase
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class AddMealViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 2)
    private val repo = FakeMealRepository()
    private val vm = AddMealViewModel(today, today, MealType.LUNCH, LogMealUseCase(repo), CalculateKcalFromMacrosUseCase(), { "u1" }, { Instant.EPOCH })

    private fun fill(name: String, p: String, c: String, f: String) {
        vm.onEvent(AddMealEvent.NameChanged(name))
        vm.onEvent(AddMealEvent.ProteinChanged(p))
        vm.onEvent(AddMealEvent.CarbsChanged(c))
        vm.onEvent(AddMealEvent.FatChanged(f))
    }

    @Test
    fun `kcal update while typing`() {
        fill("Arepa", "5", "30", "8")
        assertEquals(212, vm.state.value.kcal)
    }

    @Test
    fun `initial meal type comes from the route`() {
        assertEquals(MealType.LUNCH, vm.state.value.mealType)
    }

    @Test
    fun `short name is rejected`() {
        fill("A", "5", "30", "8")
        vm.onEvent(AddMealEvent.Save)
        assertNotNull(vm.state.value.nameError)
        assertTrue(repo.logs.value.isEmpty())
    }

    @Test
    fun `macro over 300 is rejected`() {
        fill("Arepa", "301", "0", "0")
        vm.onEvent(AddMealEvent.Save)
        assertNotNull(vm.state.value.macroError)
    }

    @Test
    fun `all macros zero is rejected`() {
        fill("Arepa", "", "0", "")
        vm.onEvent(AddMealEvent.Save)
        assertNotNull(vm.state.value.macroError)
    }

    @Test
    fun `valid form saves a manual log`() {
        fill("  Arepa  ", "5", "30,5", "8")
        vm.onEvent(AddMealEvent.MealTypeChanged(MealType.BREAKFAST))
        vm.onEvent(AddMealEvent.Save)
        val log = repo.logs.value.single()
        assertEquals("Arepa", log.name)
        assertEquals(30.5, log.macros.carbsG, 0.001)
        assertEquals(MealSource.MANUAL, log.source)
        assertEquals(MealType.BREAKFAST, log.mealType)
        assertEquals(today, vm.state.value.addedOn)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealViewModelTest"`
Expected: FAIL de compilación.

- [ ] **Step 3: Estado, eventos y ViewModel**

`PKG/presentation/viewmodel/nutrition/AddMealUiState.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

data class AddMealUiState(
    val name: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val kcal: Int = 0,
    val nameError: String? = null,
    val macroError: String? = null,
    val isSaving: Boolean = false,
    val addedOn: LocalDate? = null,
    val error: String? = null
)
```

`PKG/presentation/viewmodel/nutrition/AddMealEvent.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType

sealed interface AddMealEvent {
    data class NameChanged(val value: String) : AddMealEvent
    data class ProteinChanged(val value: String) : AddMealEvent
    data class CarbsChanged(val value: String) : AddMealEvent
    data class FatChanged(val value: String) : AddMealEvent
    data class MealTypeChanged(val mealType: MealType) : AddMealEvent
    data object Save : AddMealEvent
    data object ErrorShown : AddMealEvent
}
```

`PKG/presentation/viewmodel/nutrition/AddMealViewModel.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.usecase.CalculateKcalFromMacrosUseCase
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class AddMealViewModel(
    date: LocalDate,
    today: LocalDate,
    initialType: MealType?,
    private val logMeal: LogMealUseCase,
    private val calculateKcal: CalculateKcalFromMacrosUseCase,
    private val currentUserId: () -> String,
    private val clock: () -> Instant = Instant::now
) : ViewModel() {

    private val logDate = if (date.isAfter(today)) today else date

    private val _state = MutableStateFlow(AddMealUiState(mealType = initialType ?: MealType.BREAKFAST))
    val state: StateFlow<AddMealUiState> = _state.asStateFlow()

    fun onEvent(event: AddMealEvent) {
        when (event) {
            is AddMealEvent.NameChanged -> _state.update { it.copy(name = event.value, nameError = null) }
            is AddMealEvent.ProteinChanged -> updateMacros { it.copy(protein = event.value) }
            is AddMealEvent.CarbsChanged -> updateMacros { it.copy(carbs = event.value) }
            is AddMealEvent.FatChanged -> updateMacros { it.copy(fat = event.value) }
            is AddMealEvent.MealTypeChanged -> _state.update { it.copy(mealType = event.mealType) }
            AddMealEvent.Save -> save()
            AddMealEvent.ErrorShown -> _state.update { it.copy(error = null) }
        }
    }

    private fun updateMacros(change: (AddMealUiState) -> AddMealUiState) {
        _state.update { current ->
            val next = change(current)
            next.copy(kcal = calculateKcal(next.macrosOrZero()), macroError = null)
        }
    }

    private fun save() {
        val current = _state.value
        if (current.isSaving) return
        val name = current.name.trim()
        val nameError = if (name.length !in 2..60) "Name must be between 2 and 60 characters" else null
        val values = listOf(current.protein, current.carbs, current.fat).map(::parseGrams)
        val macroError = when {
            values.any { it == null || it !in 0.0..300.0 } -> "Each macro must be between 0 and 300 g"
            values.all { it == 0.0 } -> "Enter at least one macro greater than 0 g"
            else -> null
        }
        if (nameError != null || macroError != null) {
            _state.update { it.copy(nameError = nameError, macroError = macroError) }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val entry = MealLog.manual(name, current.macrosOrZero(), currentUserId(), logDate, current.mealType, clock())
            val result = logMeal(entry)
            _state.update {
                when (result) {
                    is Result.Success -> it.copy(isSaving = false, addedOn = logDate)
                    is Result.Error -> it.copy(isSaving = false, error = result.message)
                    Result.Loading -> it
                }
            }
        }
    }

    private fun AddMealUiState.macrosOrZero() =
        Macros(parseGrams(protein) ?: 0.0, parseGrams(carbs) ?: 0.0, parseGrams(fat) ?: 0.0)

    /** Vacío = 0; acepta coma decimal; null si no es un número. */
    private fun parseGrams(text: String): Double? =
        if (text.isBlank()) 0.0 else text.trim().replace(',', '.').toDoubleOrNull()
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealViewModelTest"`
Expected: PASS (6 pruebas).

- [ ] **Step 5: Pantalla**

`PKG/presentation/screen/nutrition/AddMealScreen.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealTypeChips
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun AddMealScreen(
    container: NutritionContainer,
    date: LocalDate,
    initialType: MealType?,
    onBack: () -> Unit,
    onAdded: (LocalDate) -> Unit
) {
    val viewModel: AddMealViewModel = viewModel(factory = NutritionViewModelFactory {
        AddMealViewModel(date, LocalDate.now(), initialType, container.logMeal, container.calculateKcal, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val number = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    LaunchedEffect(state.addedOn) { state.addedOn?.let(onAdded) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TopBarBack(onBack = onBack, title = "Add New Meal")
        MovaTextField(
            value = state.name,
            onValueChange = { viewModel.onEvent(AddMealEvent.NameChanged(it)) },
            label = "Meal Name",
            isError = state.nameError != null,
            errorMessage = state.nameError
        )
        MealTypeChips(
            selected = state.mealType,
            onSelect = { type -> type?.let { viewModel.onEvent(AddMealEvent.MealTypeChanged(it)) } },
            includeAll = false
        )
        MovaTextField(value = state.protein, onValueChange = { viewModel.onEvent(AddMealEvent.ProteinChanged(it)) }, label = "Total Protein (g)", keyboardOptions = number)
        MovaTextField(value = state.carbs, onValueChange = { viewModel.onEvent(AddMealEvent.CarbsChanged(it)) }, label = "Total Carbs (g)", keyboardOptions = number)
        MovaTextField(value = state.fat, onValueChange = { viewModel.onEvent(AddMealEvent.FatChanged(it)) }, label = "Total Fat (g)", keyboardOptions = number)
        state.macroError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("${state.kcal} kcal", style = MaterialTheme.typography.titleLarge)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        MovaPrimaryButton(
            text = if (state.isSaving) "Adding..." else "Add Meal",
            onClick = { viewModel.onEvent(AddMealEvent.Save) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

- [ ] **Step 6: Registrar el destino**

En `NutritionNavGraph.kt`, agrega los imports `co.edu.unal.dadm.mova.presentation.screen.nutrition.AddMealScreen` y `co.edu.unal.dadm.mova.domain.model.MealType`, y dentro de `nutritionGraph { }`:

```kotlin
    composable(
        Routes.NutritionAdd,
        arguments = listOf(
            navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
            optionalDateArg
        )
    ) { entry ->
        AddMealScreen(
            container = container,
            date = entry.dateArg(),
            initialType = entry.arguments?.getString("type")?.let { runCatching { MealType.valueOf(it) }.getOrNull() },
            onBack = { navController.popBackStack() },
            onAdded = { date ->
                navController.navigate(Routes.nutritionAdded(date)) {
                    popUpTo(Routes.NutritionAdd) { inclusive = true }
                }
            }
        )
    }
```

- [ ] **Step 7: Compilar y commit**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/presentation app/src/test/java/co/edu/unal/dadm/mova/presentation/viewmodel/nutrition
git commit -m "[Add] Manual meal entry screen"
```

---

### Task 14: Mis comidas (N5)

**Files:**
- Modify (stubs): `PKG/presentation/viewmodel/nutrition/MyMealsUiState.kt`, `MyMealsEvent.kt`, `MyMealsViewModel.kt`, `PKG/presentation/screen/nutrition/MyMealsScreen.kt`
- Modify: `PKG/presentation/navigation/NutritionNavGraph.kt`
- Test: `TEST/presentation/viewmodel/nutrition/MyMealsViewModelTest.kt`

**Interfaces:**
- Consumes: `MealRepository.observeLogs(userId, date)`, `MealRepository.delete`.
- Produces: `MyMealsViewModel(initialDate: LocalDate, today: LocalDate, mealRepository: MealRepository, currentUserId: () -> String)`; destino `Routes.NutritionMyMeals`.

El carrusel muestra los 7 días que terminan hoy (no hay días futuros porque no se puede registrar en ellos). Si `initialDate` es anterior a esa ventana, la ventana termina en `initialDate + 6` días o en hoy, lo que sea antes.

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/presentation/viewmodel/nutrition/MyMealsViewModelTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class MyMealsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 2)
    private val repo = FakeMealRepository().apply {
        logs.value = listOf(
            TestMeals.log("b", today, mealType = MealType.BREAKFAST),
            TestMeals.log("l", today, mealType = MealType.LUNCH),
            TestMeals.log("y", today.minusDays(1), mealType = MealType.DINNER)
        )
    }

    private fun viewModel(initial: LocalDate = today) = MyMealsViewModel(initial, today, repo) { "u1" }

    @Test
    fun `seven days ending today`() {
        val days = viewModel().state.value.days
        assertEquals(7, days.size)
        assertEquals(today.minusDays(6), days.first())
        assertEquals(today, days.last())
    }

    @Test
    fun `shows logs of the selected day`() {
        val vm = viewModel()
        assertEquals(setOf("b", "l"), vm.state.value.visibleLogs.map { it.id }.toSet())
        vm.onEvent(MyMealsEvent.SelectDate(today.minusDays(1)))
        assertEquals(listOf("y"), vm.state.value.visibleLogs.map { it.id })
    }

    @Test
    fun `filters by meal type`() {
        val vm = viewModel()
        vm.onEvent(MyMealsEvent.SelectFilter(MealType.LUNCH))
        assertEquals(listOf("l"), vm.state.value.visibleLogs.map { it.id })
        vm.onEvent(MyMealsEvent.SelectFilter(null))
        assertEquals(2, vm.state.value.visibleLogs.size)
    }

    @Test
    fun `delete asks for confirmation first`() {
        val vm = viewModel()
        val target = vm.state.value.visibleLogs.first { it.id == "b" }
        vm.onEvent(MyMealsEvent.RequestDelete(target))
        assertEquals(3, repo.logs.value.size)
        vm.onEvent(MyMealsEvent.ConfirmDelete)
        assertEquals(2, repo.logs.value.size)
        assertNull(vm.state.value.pendingDelete)
    }

    @Test
    fun `cancel keeps the log`() {
        val vm = viewModel()
        vm.onEvent(MyMealsEvent.RequestDelete(vm.state.value.visibleLogs.first()))
        vm.onEvent(MyMealsEvent.CancelDelete)
        assertEquals(3, repo.logs.value.size)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsViewModelTest"`
Expected: FAIL de compilación.

- [ ] **Step 3: Estado, eventos y ViewModel**

`PKG/presentation/viewmodel/nutrition/MyMealsUiState.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

data class MyMealsUiState(
    val selectedDate: LocalDate,
    val days: List<LocalDate>,
    val filter: MealType? = null,
    val logs: List<MealLog> = emptyList(),
    val pendingDelete: MealLog? = null,
    val error: String? = null
) {
    val visibleLogs: List<MealLog> get() = if (filter == null) logs else logs.filter { it.mealType == filter }
    val totalKcal: Int get() = visibleLogs.sumOf { it.kcal }
}
```

`PKG/presentation/viewmodel/nutrition/MyMealsEvent.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

sealed interface MyMealsEvent {
    data class SelectDate(val date: LocalDate) : MyMealsEvent
    data class SelectFilter(val mealType: MealType?) : MyMealsEvent
    data class RequestDelete(val log: MealLog) : MyMealsEvent
    data object ConfirmDelete : MyMealsEvent
    data object CancelDelete : MyMealsEvent
    data object ErrorShown : MyMealsEvent
}
```

`PKG/presentation/viewmodel/nutrition/MyMealsViewModel.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MyMealsViewModel(
    initialDate: LocalDate,
    today: LocalDate,
    private val mealRepository: MealRepository,
    private val currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(
        MyMealsUiState(selectedDate = minOf(initialDate, today), days = window(initialDate, today))
    )
    val state: StateFlow<MyMealsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date -> mealRepository.observeLogs(currentUserId(), date) }
                .collect { logs -> _state.update { it.copy(logs = logs) } }
        }
    }

    fun onEvent(event: MyMealsEvent) {
        when (event) {
            is MyMealsEvent.SelectDate -> _state.update { it.copy(selectedDate = event.date) }
            is MyMealsEvent.SelectFilter -> _state.update { it.copy(filter = event.mealType) }
            is MyMealsEvent.RequestDelete -> _state.update { it.copy(pendingDelete = event.log) }
            MyMealsEvent.CancelDelete -> _state.update { it.copy(pendingDelete = null) }
            MyMealsEvent.ConfirmDelete -> confirmDelete()
            MyMealsEvent.ErrorShown -> _state.update { it.copy(error = null) }
        }
    }

    private fun confirmDelete() {
        val target = _state.value.pendingDelete ?: return
        _state.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            val result = mealRepository.delete(target.id)
            if (result is Result.Error) _state.update { it.copy(error = result.message) }
        }
    }

    private companion object {
        fun window(initialDate: LocalDate, today: LocalDate): List<LocalDate> {
            val end = minOf(initialDate.plusDays(6), today)
            return (6 downTo 0).map { end.minusDays(it.toLong()) }
        }
    }
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsViewModelTest"`
Expected: PASS (5 pruebas).

- [ ] **Step 5: Pantalla**

`PKG/presentation/screen/nutrition/MyMealsScreen.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealTypeChips
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MyMealsScreen(
    container: NutritionContainer,
    initialDate: LocalDate,
    onBack: () -> Unit,
    onAddMeal: (MealType?, LocalDate) -> Unit,
    onExplore: () -> Unit
) {
    val viewModel: MyMealsViewModel = viewModel(factory = NutritionViewModelFactory {
        MyMealsViewModel(initialDate, LocalDate.now(), container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBarBack(onBack = onBack, title = "My Meals")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.days) { day ->
                FilterChip(
                    selected = day == state.selectedDate,
                    onClick = { viewModel.onEvent(MyMealsEvent.SelectDate(day)) },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                            Text(day.dayOfMonth.toString())
                        }
                    }
                )
            }
        }
        MealTypeChips(selected = state.filter, onSelect = { viewModel.onEvent(MyMealsEvent.SelectFilter(it)) }, includeAll = true)
        Text("${state.totalKcal} kcal", style = MaterialTheme.typography.titleMedium)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.visibleLogs.isEmpty()) {
            Text("No meals logged for this day yet.", color = MovaMuted, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.visibleLogs, key = { it.id }) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${log.mealType.label} · P ${log.macros.proteinG.roundToInt()} g · C ${log.macros.carbsG.roundToInt()} g · F ${log.macros.fatG.roundToInt()} g",
                                    color = MovaMuted
                                )
                            }
                            Text("${log.kcal} kcal")
                            TextButton(onClick = { viewModel.onEvent(MyMealsEvent.RequestDelete(log)) }) { Text("Delete") }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MovaPrimaryButton(text = "Add Meal", onClick = { onAddMeal(state.filter, state.selectedDate) }, modifier = Modifier.weight(1f))
            MovaPrimaryButton(text = "Explore", onClick = onExplore, modifier = Modifier.weight(1f), trailingText = null)
        }
    }

    state.pendingDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(MyMealsEvent.CancelDelete) },
            title = { Text("Delete meal?") },
            text = { Text("${log.name} will be removed from ${log.date}.") },
            confirmButton = { TextButton(onClick = { viewModel.onEvent(MyMealsEvent.ConfirmDelete) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.onEvent(MyMealsEvent.CancelDelete) }) { Text("Cancel") } }
        )
    }
}
```

- [ ] **Step 6: Registrar el destino**

En `NutritionNavGraph.kt`, agrega el import `co.edu.unal.dadm.mova.presentation.screen.nutrition.MyMealsScreen` y dentro de `nutritionGraph { }`:

```kotlin
    composable(Routes.NutritionMyMeals, arguments = listOf(optionalDateArg)) { entry ->
        MyMealsScreen(
            container = container,
            initialDate = entry.dateArg(),
            onBack = { navController.popBackStack() },
            onAddMeal = { type, date -> navController.navigate(Routes.nutritionAdd(type, date)) },
            onExplore = { navController.navigate(Routes.NutritionExplore) }
        )
    }
```

- [ ] **Step 7: Compilar y commit**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/presentation app/src/test/java/co/edu/unal/dadm/mova/presentation/viewmodel/nutrition
git commit -m "[Add] My Meals screen with day carousel and delete"
```

---

### Task 15: Ingesta del día (N6) y meta adaptativa con gráfica mensual (N7)

**Files:**
- Modify (stubs): `PKG/presentation/viewmodel/nutrition/CalorieIntakeUiState.kt`, `CalorieIntakeEvent.kt`, `CalorieIntakeViewModel.kt`, `PKG/presentation/screen/nutrition/CalorieIntakeScreen.kt`, `PKG/presentation/charts/SmoothAreaChart.kt`
- Modify: `PKG/presentation/navigation/NutritionNavGraph.kt`
- Test: `TEST/presentation/viewmodel/nutrition/CalorieIntakeViewModelTest.kt`

**Interfaces:**
- Consumes: `ObserveDailyIntakeUseCase`, `MealRepository.observeLogs(userId, from, to)`, `MacroBar`.
- Produces: `CalorieIntakeViewModel(today: LocalDate, observeDailyIntake: ObserveDailyIntakeUseCase, mealRepository: MealRepository, currentUserId: () -> String)`; `@Composable fun SmoothAreaChart(values: List<Float>, modifier: Modifier = Modifier, lineColor: Color = MovaOrange, fillColor: Color = MovaOrange.copy(alpha = 0.2f))`; destino `Routes.NutritionIntake`.

Texto de exceso (§3), literal y en color neutro: «Hoy superaste tu meta por X kcal; mañana se equilibra».

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/presentation/viewmodel/nutrition/CalorieIntakeViewModelTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCase
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.FakeProfileRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalorieIntakeViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 3)
    private val repo = FakeMealRepository()

    private fun viewModel() = CalorieIntakeViewModel(
        today,
        ObserveDailyIntakeUseCase(repo, FakeProfileRepository(null), BurnedKcalProvider.NONE, CalculateCalorieGoalUseCase()) { "u1" },
        repo
    ) { "u1" }

    @Test
    fun `monthly series has one value per day until today`() {
        repo.logs.value = listOf(
            TestMeals.log("a", LocalDate.of(2026, 11, 1)), // 125 kcal
            TestMeals.log("b", LocalDate.of(2026, 11, 1)), // 125 kcal
            TestMeals.log("c", today),                      // 125 kcal
            TestMeals.log("old", LocalDate.of(2026, 10, 31))
        )
        assertEquals(listOf(250, 0, 125), viewModel().state.value.monthlyKcal)
    }

    @Test
    fun `exceeded is null under the goal`() {
        repo.logs.value = listOf(TestMeals.log("a", today))
        assertNull(viewModel().state.value.exceededBy)
    }

    @Test
    fun `exceeded shows how much over the goal`() {
        // Meta por defecto sin perfil: 2370 kcal. 3300 consumidas -> 930 de más.
        repo.logs.value = listOf(TestMeals.log("big", today, protein = 200.0, carbs = 400.0, fat = 100.0))
        assertEquals(930, viewModel().state.value.exceededBy)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieIntakeViewModelTest"`
Expected: FAIL de compilación.

- [ ] **Step 3: Estado, eventos y ViewModel**

`PKG/presentation/viewmodel/nutrition/CalorieIntakeUiState.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.DailyIntake

data class CalorieIntakeUiState(
    val intake: DailyIntake? = null,
    /** kcal consumidas por día del mes actual, desde el día 1 hasta hoy. */
    val monthlyKcal: List<Int> = emptyList()
) {
    val exceededBy: Int? get() = intake?.remainingKcal?.let { if (it < 0) -it else null }
}
```

`PKG/presentation/viewmodel/nutrition/CalorieIntakeEvent.kt` (la pantalla solo navega; no hay eventos hacia el ViewModel):

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

sealed interface CalorieIntakeEvent
```

`PKG/presentation/viewmodel/nutrition/CalorieIntakeViewModel.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalorieIntakeViewModel(
    today: LocalDate,
    observeDailyIntake: ObserveDailyIntakeUseCase,
    mealRepository: MealRepository,
    currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(CalorieIntakeUiState())
    val state: StateFlow<CalorieIntakeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeDailyIntake(today).collect { intake -> _state.update { it.copy(intake = intake) } }
        }
        viewModelScope.launch {
            val monthStart = today.withDayOfMonth(1)
            mealRepository.observeLogs(currentUserId(), monthStart, today).collect { logs ->
                val byDay = logs.groupBy { it.date }.mapValues { (_, dayLogs) -> dayLogs.sumOf { it.kcal } }
                val series = (1..today.dayOfMonth).map { day -> byDay[today.withDayOfMonth(day)] ?: 0 }
                _state.update { it.copy(monthlyKcal = series) }
            }
        }
    }
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieIntakeViewModelTest"`
Expected: PASS (3 pruebas).

- [ ] **Step 5: Gráfica**

Reemplaza el stub `PKG/presentation/charts/SmoothAreaChart.kt` por:

```kotlin
package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

/** Curva suavizada con área rellena. Con un solo valor dibuja una línea horizontal. */
@Composable
fun SmoothAreaChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MovaOrange,
    fillColor: Color = MovaOrange.copy(alpha = 0.2f)
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val series = if (values.size == 1) values + values else values
        val max = series.max().coerceAtLeast(1f)
        val stepX = size.width / (series.size - 1)
        val points = series.mapIndexed { index, value -> Offset(index * stepX, size.height - value / max * size.height) }

        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val previous = points[i - 1]
                val current = points[i]
                val midX = (previous.x + current.x) / 2
                cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
            }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        drawPath(area, fillColor)
        drawPath(line, lineColor, style = Stroke(width = 3.dp.toPx()))
    }
}
```

- [ ] **Step 6: Pantalla**

`PKG/presentation/screen/nutrition/CalorieIntakeScreen.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.presentation.charts.SmoothAreaChart
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MacroBar
import co.edu.unal.dadm.mova.presentation.theme.MovaBlueIcon
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaPurpleIcon
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieIntakeViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun CalorieIntakeScreen(
    container: NutritionContainer,
    onBack: () -> Unit,
    onMyMeals: () -> Unit,
    onAddMeal: () -> Unit,
    onExplore: () -> Unit,
    onStats: () -> Unit
) {
    val viewModel: CalorieIntakeViewModel = viewModel(factory = NutritionViewModelFactory {
        CalorieIntakeViewModel(LocalDate.now(), container.observeDailyIntake, container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBarBack(onBack = onBack, title = "Calorie Intake")
        val intake = state.intake
        if (intake == null) {
            CircularProgressIndicator()
        } else {
            Text("${intake.consumedKcal} kcal", style = MaterialTheme.typography.displaySmall)
            val exceeded = state.exceededBy
            Text(
                text = if (exceeded != null) "Hoy superaste tu meta por $exceeded kcal; mañana se equilibra"
                else "${intake.remainingKcal} kcal left of ${intake.goalKcal}",
                color = MovaMuted
            )
            if (intake.burnedKcal > 0) {
                Text("Goal includes ${intake.burnedKcal} kcal burned today", color = MovaMuted)
            }
            MacroBar("Protein", intake.consumed.proteinG, intake.goalMacros.proteinG, MovaOrange)
            MacroBar("Carbs", intake.consumed.carbsG, intake.goalMacros.carbsG, MovaBlueIcon)
            MacroBar("Fat", intake.consumed.fatG, intake.goalMacros.fatG, MovaPurpleIcon)
        }

        Text("This month", style = MaterialTheme.typography.titleMedium)
        SmoothAreaChart(
            values = state.monthlyKcal.map { it.toFloat() },
            modifier = Modifier.fillMaxWidth().height(160.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MovaPrimaryButton(text = "My Meals", onClick = onMyMeals, modifier = Modifier.weight(1f), trailingText = null)
            MovaPrimaryButton(text = "Add Meal", onClick = onAddMeal, modifier = Modifier.weight(1f), trailingText = null)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MovaPrimaryButton(text = "Explore", onClick = onExplore, modifier = Modifier.weight(1f), trailingText = null)
            MovaPrimaryButton(text = "Stats", onClick = onStats, modifier = Modifier.weight(1f), trailingText = null)
        }
    }
}
```

- [ ] **Step 7: Registrar el destino**

En `NutritionNavGraph.kt`, agrega el import `co.edu.unal.dadm.mova.presentation.screen.nutrition.CalorieIntakeScreen` y dentro de `nutritionGraph { }`:

```kotlin
    composable(Routes.NutritionIntake) {
        CalorieIntakeScreen(
            container = container,
            onBack = { navController.popBackStack() },
            onMyMeals = { navController.navigate(Routes.nutritionMyMeals()) },
            onAddMeal = { navController.navigate(Routes.nutritionAdd()) },
            onExplore = { navController.navigate(Routes.NutritionExplore) },
            onStats = { navController.navigate(Routes.NutritionStats) }
        )
    }
```

- [ ] **Step 8: Compilar y commit**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/presentation app/src/test/java/co/edu/unal/dadm/mova/presentation/viewmodel/nutrition
git commit -m "[Add] Calorie intake screen with monthly chart"
```

---

### Task 16: Catálogo y sugerencias (N1 y N8)

**Files:**
- Modify (stubs): `PKG/presentation/viewmodel/nutrition/MealSuggestionUiState.kt`, `MealSuggestionEvent.kt`, `MealSuggestionViewModel.kt`, `PKG/presentation/screen/nutrition/MealSuggestionScreen.kt`
- Modify: `PKG/presentation/navigation/NutritionNavGraph.kt`
- Test: `TEST/presentation/viewmodel/nutrition/MealSuggestionViewModelTest.kt`

**Interfaces:**
- Consumes: `MealRepository.observeCatalog()`, `ProfileRepository.observe`, `ObserveDailyIntakeUseCase`, `SuggestMealsUseCase`, `MealImage`.
- Produces: `MealSuggestionViewModel(today: LocalDate, mealRepository: MealRepository, profileRepository: ProfileRepository, observeDailyIntake: ObserveDailyIntakeUseCase, suggestMeals: SuggestMealsUseCase, currentUserId: () -> String)`; destino `Routes.NutritionExplore`.

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/presentation/viewmodel/nutrition/MealSuggestionViewModelTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCase
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.domain.usecase.SuggestMealsUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.FakeProfileRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class MealSuggestionViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val salad = TestMeals.meal("salad", diets = TestMeals.VEGAN, category = MealCategory.SALAD_VEGETABLES)
    private val steak = TestMeals.meal("steak", diets = TestMeals.MEAT, category = MealCategory.PROTEIN)
    private val repo = FakeMealRepository(listOf(salad, steak))

    private fun viewModel(diet: Diet?): MealSuggestionViewModel {
        val profiles = FakeProfileRepository(UserProfile(userId = "u1", diet = diet))
        val intake = ObserveDailyIntakeUseCase(repo, profiles, BurnedKcalProvider.NONE, CalculateCalorieGoalUseCase()) { "u1" }
        return MealSuggestionViewModel(LocalDate.of(2026, 11, 2), repo, profiles, intake, SuggestMealsUseCase()) { "u1" }
    }

    @Test
    fun `catalog shows every meal and filters by category`() {
        val vm = viewModel(Diet.VEGAN)
        assertEquals(2, vm.state.value.visibleCatalog.size)
        vm.onEvent(MealSuggestionEvent.SelectCategory(MealCategory.PROTEIN))
        assertEquals(listOf(steak), vm.state.value.visibleCatalog)
    }

    @Test
    fun `suggestions respect the diet`() {
        assertEquals(listOf(salad), viewModel(Diet.VEGAN).state.value.suggestions)
        assertEquals(2, viewModel(null).state.value.suggestions.size)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionViewModelTest"`
Expected: FAIL de compilación.

- [ ] **Step 3: Estado, eventos y ViewModel**

`PKG/presentation/viewmodel/nutrition/MealSuggestionUiState.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory

data class MealSuggestionUiState(
    val catalog: List<Meal> = emptyList(),
    val category: MealCategory? = null,
    val suggestions: List<Meal> = emptyList(),
    val isLoading: Boolean = true
) {
    val visibleCatalog: List<Meal> get() = category?.let { selected -> catalog.filter { it.category == selected } } ?: catalog
}
```

`PKG/presentation/viewmodel/nutrition/MealSuggestionEvent.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealCategory

sealed interface MealSuggestionEvent {
    data class SelectCategory(val category: MealCategory?) : MealSuggestionEvent
}
```

`PKG/presentation/viewmodel/nutrition/MealSuggestionViewModel.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.domain.usecase.SuggestMealsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MealSuggestionViewModel(
    today: LocalDate,
    mealRepository: MealRepository,
    profileRepository: ProfileRepository,
    observeDailyIntake: ObserveDailyIntakeUseCase,
    suggestMeals: SuggestMealsUseCase,
    currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(MealSuggestionUiState())
    val state: StateFlow<MealSuggestionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                mealRepository.observeCatalog(),
                profileRepository.observe(currentUserId()).catch { emit(null) },
                observeDailyIntake(today)
            ) { catalog, profile, intake ->
                catalog to suggestMeals(catalog, profile?.diet, intake)
            }.collect { (catalog, suggestions) ->
                _state.update { it.copy(catalog = catalog, suggestions = suggestions, isLoading = false) }
            }
        }
    }

    fun onEvent(event: MealSuggestionEvent) {
        when (event) {
            is MealSuggestionEvent.SelectCategory -> _state.update { it.copy(category = event.category) }
        }
    }
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionViewModelTest"`
Expected: PASS (2 pruebas).

- [ ] **Step 5: Pantalla**

`PKG/presentation/screen/nutrition/MealSuggestionScreen.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealImage
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun MealSuggestionScreen(
    container: NutritionContainer,
    onBack: () -> Unit,
    onMeal: (String) -> Unit
) {
    val viewModel: MealSuggestionViewModel = viewModel(factory = NutritionViewModelFactory {
        MealSuggestionViewModel(
            LocalDate.now(), container.mealRepository, container.profileRepository,
            container.observeDailyIntake, container.suggestMeals, container.currentUserId
        )
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBarBack(onBack = onBack, title = "Explore")
        if (state.isLoading) {
            CircularProgressIndicator()
            return@Column
        }
        if (state.suggestions.isNotEmpty()) {
            Text("Suggested for you", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.suggestions, key = { it.id }) { meal ->
                    Card(modifier = Modifier.width(160.dp).clickable { onMeal(meal.id) }) {
                        MealImage(meal.name, meal.imageRes, Modifier.fillMaxWidth().height(100.dp))
                        Text(meal.name, modifier = Modifier.padding(8.dp), maxLines = 2)
                        Text("${meal.kcal} kcal", color = MovaMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.category == null, onClick = { viewModel.onEvent(MealSuggestionEvent.SelectCategory(null)) }, label = { Text("All") })
            MealCategory.entries.forEach { category ->
                FilterChip(
                    selected = state.category == category,
                    onClick = { viewModel.onEvent(MealSuggestionEvent.SelectCategory(category)) },
                    label = { Text(category.label) }
                )
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.visibleCatalog, key = { it.id }) { meal -> CatalogRow(meal, onClick = { onMeal(meal.id) }) }
        }
    }
}

@Composable
private fun CatalogRow(meal: Meal, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MealImage(meal.name, meal.imageRes, Modifier.width(72.dp).height(72.dp))
            Column {
                Text(meal.name, style = MaterialTheme.typography.titleSmall)
                Text(meal.category.label, color = MovaMuted)
                Text("${meal.kcal} kcal · ${meal.prepMinutes} min", color = MovaMuted)
            }
        }
    }
}
```

- [ ] **Step 6: Registrar el destino**

En `NutritionNavGraph.kt`, agrega el import `co.edu.unal.dadm.mova.presentation.screen.nutrition.MealSuggestionScreen` y dentro de `nutritionGraph { }`:

```kotlin
    composable(Routes.NutritionExplore) {
        MealSuggestionScreen(
            container = container,
            onBack = { navController.popBackStack() },
            onMeal = { mealId -> navController.navigate(Routes.nutritionMeal(mealId)) }
        )
    }
```

- [ ] **Step 7: Compilar y commit**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/presentation app/src/test/java/co/edu/unal/dadm/mova/presentation/viewmodel/nutrition
git commit -m "[Add] Meal catalog and suggestions screen"
```

---

### Task 17: Estadísticas mensuales con insignia Premium (N9)

**Files:**
- Modify (stubs): `PKG/presentation/viewmodel/nutrition/CalorieStatsUiState.kt`, `CalorieStatsEvent.kt`, `CalorieStatsViewModel.kt`, `PKG/presentation/screen/nutrition/CalorieStatsScreen.kt`
- Modify: `PKG/presentation/navigation/NutritionNavGraph.kt`
- Test: `TEST/presentation/viewmodel/nutrition/CalorieStatsViewModelTest.kt`

**Interfaces:**
- Consumes: `MealRepository.observeLogs(userId, from, to)`.
- Produces: `data class MacroShare(proteinPct: Int, carbsPct: Int, fatPct: Int)`; `CalorieStatsViewModel(today: LocalDate, mealRepository: MealRepository, currentUserId: () -> String)` con `CalorieStatsViewModel.share(macros: Macros): MacroShare?`; destino `Routes.NutritionStats`.

D6 y §10: N9 no se bloquea en el prototipo; solo muestra una insignia «Premium» en una esquina. Tres barras (proteína, carbohidratos, grasa) cuyos porcentajes suman 100.

- [ ] **Step 1: Escribir la prueba que falla**

`TEST/presentation/viewmodel/nutrition/CalorieStatsViewModelTest.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalorieStatsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test
    fun `share always sums 100`() {
        // kcal: P 100, C 200, G 90 -> 25.6 %, 51.3 %, 23.1 %
        val share = CalorieStatsViewModel.share(Macros(25.0, 50.0, 10.0))!!
        assertEquals(26, share.proteinPct)
        assertEquals(51, share.carbsPct)
        assertEquals(23, share.fatPct)
        assertEquals(100, share.proteinPct + share.carbsPct + share.fatPct)
    }

    @Test
    fun `no data gives no share`() {
        assertNull(CalorieStatsViewModel.share(Macros.ZERO))
    }

    @Test
    fun `uses only the current month`() {
        val today = LocalDate.of(2026, 11, 15)
        val repo = FakeMealRepository().apply {
            logs.value = listOf(
                TestMeals.log("in", LocalDate.of(2026, 11, 1), protein = 25.0, carbs = 50.0, fat = 10.0),
                TestMeals.log("out", LocalDate.of(2026, 10, 31), protein = 100.0, carbs = 0.0, fat = 0.0)
            )
        }
        val state = CalorieStatsViewModel(today, repo) { "u1" }.state.value
        assertEquals(390, state.totalKcal)
        assertEquals(26, state.share!!.proteinPct)
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieStatsViewModelTest"`
Expected: FAIL de compilación.

- [ ] **Step 3: Estado, eventos y ViewModel**

`PKG/presentation/viewmodel/nutrition/CalorieStatsUiState.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

data class MacroShare(val proteinPct: Int, val carbsPct: Int, val fatPct: Int)

data class CalorieStatsUiState(
    val share: MacroShare? = null,
    val totalKcal: Int = 0,
    val isLoading: Boolean = true
)
```

`PKG/presentation/viewmodel/nutrition/CalorieStatsEvent.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

sealed interface CalorieStatsEvent
```

`PKG/presentation/viewmodel/nutrition/CalorieStatsViewModel.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

class CalorieStatsViewModel(
    today: LocalDate,
    mealRepository: MealRepository,
    currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(CalorieStatsUiState())
    val state: StateFlow<CalorieStatsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeLogs(currentUserId(), today.withDayOfMonth(1), today).collect { logs ->
                val total = logs.fold(Macros.ZERO) { sum, log -> sum + log.macros }
                _state.update { it.copy(share = share(total), totalKcal = logs.sumOf { log -> log.kcal }, isLoading = false) }
            }
        }
    }

    companion object {
        /** Porcentaje de kcal de cada macro; la grasa absorbe el redondeo para que sumen 100. */
        fun share(macros: Macros): MacroShare? {
            val protein = macros.proteinG * 4
            val carbs = macros.carbsG * 4
            val fat = macros.fatG * 9
            val total = protein + carbs + fat
            if (total <= 0.0) return null
            val proteinPct = (protein / total * 100).roundToInt()
            val carbsPct = (carbs / total * 100).roundToInt()
            return MacroShare(proteinPct, carbsPct, 100 - proteinPct - carbsPct)
        }
    }
}
```

- [ ] **Step 4: Ejecutar la prueba**

Run: `./gradlew :app:testDebugUnitTest --tests "co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieStatsViewModelTest"`
Expected: PASS (3 pruebas).

- [ ] **Step 5: Pantalla**

`PKG/presentation/screen/nutrition/CalorieStatsScreen.kt`:

```kotlin
package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaBlueIcon
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaPurpleIcon
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieStatsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun CalorieStatsScreen(container: NutritionContainer, onBack: () -> Unit) {
    val viewModel: CalorieStatsViewModel = viewModel(factory = NutritionViewModelFactory {
        CalorieStatsViewModel(LocalDate.now(), container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TopBarBack(onBack = onBack, title = "Calorie Stats")
            val share = state.share
            when {
                state.isLoading -> CircularProgressIndicator()
                share == null -> Text("No meals logged this month yet.", color = MovaMuted)
                else -> {
                    Text("${state.totalKcal} kcal this month", style = MaterialTheme.typography.titleLarge)
                    ShareBar("Protein", share.proteinPct, MovaOrange)
                    ShareBar("Carbs", share.carbsPct, MovaBlueIcon)
                    ShareBar("Fat", share.fatPct, MovaPurpleIcon)
                }
            }
        }
        // Insignia visual (D6): N9 no se bloquea en el prototipo.
        Text(
            text = "PREMIUM",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .background(MovaOrange, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ShareBar(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f))
            Text("$percent %")
        }
        LinearProgressIndicator(progress = { percent / 100f }, color = color, modifier = Modifier.fillMaxWidth().height(10.dp))
    }
}
```

- [ ] **Step 6: Registrar el destino**

En `NutritionNavGraph.kt`, agrega el import `co.edu.unal.dadm.mova.presentation.screen.nutrition.CalorieStatsScreen` y dentro de `nutritionGraph { }`:

```kotlin
    composable(Routes.NutritionStats) {
        CalorieStatsScreen(container = container, onBack = { navController.popBackStack() })
    }
```

- [ ] **Step 7: Compilar y commit**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

```bash
git add app/src/main/java/co/edu/unal/dadm/mova/presentation app/src/test/java/co/edu/unal/dadm/mova/presentation/viewmodel/nutrition
git commit -m "[Add] Monthly calorie stats with premium badge"
```

---

### Task 18: Verificación completa

Home todavía no existe, así que las pantallas de Nutrición no tienen un punto de entrada en la app. Esta tarea las prueba de punta a punta sin dejar cambios en el repo.

- [ ] **Step 1: Todas las pruebas unitarias**

Run: `./gradlew :app:testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, sin pruebas fallidas.

- [ ] **Step 2: Pruebas instrumentadas**

Run (emulador encendido): `./gradlew :app:connectedDebugAndroidTest`
Expected: PASS de `MealLogDaoTest` y `MealSeedTest`.

- [ ] **Step 3: Recorrido manual**

Cambia temporalmente, solo en tu copia local, `startDestination = Routes.Welcome` por `startDestination = Routes.NutritionIntake` en `MovaNavHost.kt`. Inicia sesión antes en la app (o usa la sesión que ya tenga el emulador). Comprueba:

1. Ingesta del día (N6) muestra 0 kcal, la meta y las tres barras.
2. Explore (N1, N8): aparecen las 40 comidas, el filtro por categoría y 5 sugerencias.
3. Detalle (N2): cambiar los gramos recalcula macros y kcal; «Add Meal» lleva a «Food Added!» (N4) y luego a Mis comidas.
4. Mis comidas (N5): el registro aparece en el día; el filtro por tipo funciona; borrar pide confirmación.
5. Registro manual (N3): con nombre de 1 letra o un macro de 301 muestra error; con datos válidos registra.
6. Firestore Console: el documento aparece en `users/{uid}/meal_logs/{id}` sin el campo `syncState`.
7. Modo avión: registra una comida; aparece en Mis comidas. Quita el modo avión: en menos de un minuto aparece en Firestore.
8. Supera la meta del día: aparece «Hoy superaste tu meta por X kcal; mañana se equilibra» en gris.
9. Stats (N9): tres barras que suman 100 % y la insignia «PREMIUM» arriba a la derecha.

Revierte el cambio de `startDestination` (`git checkout -- app/src/main/java/co/edu/unal/dadm/mova/presentation/navigation/MovaNavHost.kt` si no hay otros cambios en ese archivo).

- [ ] **Step 4: Avisar al equipo**

Comparte en el canal del equipo las rutas de §6 (`Routes.NutritionIntake`, `Routes.nutritionMyMeals(date)`, `Routes.nutritionMeal(id)`, `Routes.nutritionAdd(type, date)`) y que `NutritionContainer.get(context)` expone `observeDailyIntake`, `logMeal` y `mealRepository` para Home, Progreso, Plan, Notificaciones y Coach.
