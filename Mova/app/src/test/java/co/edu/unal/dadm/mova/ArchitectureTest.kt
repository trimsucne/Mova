package co.edu.unal.dadm.mova

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Reglas de capas verificadas leyendo los imports del codigo fuente (sin dependencias extra):
 *  - domain no importa android/androidx/compose/room ni data/presentation.
 *  - presentation de Dev C (workout, activity, places) no importa data.
 */
class ArchitectureTest {
    private val root = File("src/main/java/co/edu/unal/dadm/mova")

    private fun importsOf(dir: String): List<Pair<File, String>> =
        File(root, dir).walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { f -> f.readLines().filter { it.startsWith("import ") }.map { f to it.removePrefix("import ").trim() } }
            .toList()

    /** Deuda conocida de otros modulos (no Dev C); quitar de la lista al corregirla. */
    private val knownDebt = setOf("ProfileRepository.kt" /* importa android.net.Uri (Dev A) */)

    private fun assertNoImports(dirs: List<String>, forbidden: List<String>, rule: String) {
        val violations = dirs.flatMap { importsOf(it) }
            .filter { (f, imp) -> f.name !in knownDebt && forbidden.any { imp.startsWith(it) } }
            .map { (f, imp) -> "${f.name} -> $imp" }
        assertTrue("$rule. Violaciones:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun `el codigo fuente existe`() {
        assertTrue("No se encontro ${root.absolutePath}", root.isDirectory)
    }

    @Test
    fun `domain no depende de android ni de otras capas`() = assertNoImports(
        dirs = listOf("domain"),
        forbidden = listOf(
            "android.", "androidx.", "com.google.", "co.edu.unal.dadm.mova.data",
            "co.edu.unal.dadm.mova.presentation", "co.edu.unal.dadm.mova.core.di"
        ),
        rule = "domain debe ser Kotlin puro"
    )

    @Test
    fun `presentation de Dev C no importa data`() = assertNoImports(
        dirs = listOf(
            "presentation/screen/workout", "presentation/screen/activity", "presentation/screen/places",
            "presentation/viewmodel/workout", "presentation/viewmodel/activity", "presentation/viewmodel/places"
        ),
        forbidden = listOf("co.edu.unal.dadm.mova.data"),
        rule = "presentation debe depender de domain, no de data"
    )
}
