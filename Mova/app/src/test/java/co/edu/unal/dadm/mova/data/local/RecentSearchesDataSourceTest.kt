package co.edu.unal.dadm.mova.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentSearchesDataSourceTest {

    /**
     * DataStore en memoria. El DataStore basado en archivo del JVM reemplaza el archivo con
     * File.renameTo, que en Windows no sobrescribe un destino existente, así que las pruebas
     * unitarias fallaban en la segunda escritura. Android (Linux) no tiene ese problema.
     */
    private class InMemoryPreferencesStore : DataStore<Preferences> {
        private val state = MutableStateFlow<Preferences>(emptyPreferences())
        private val mutex = Mutex()
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            mutex.withLock { transform(state.value).also { state.value = it } }
    }

    private fun store() = RecentSearchesDataSource(InMemoryPreferencesStore())

    @Test
    fun `newest first without duplicates`() = runTest {
        val store = store()
        store.add("toast")
        store.add("salad")
        store.add("Toast ")
        assertEquals(listOf("Toast", "salad"), store.recent.first())
    }

    @Test
    fun `keeps at most eight`() = runTest {
        val store = store()
        (1..10).forEach { store.add("q$it") }
        val recent = store.recent.first()
        assertEquals(8, recent.size)
        assertEquals("q10", recent.first())
        assertEquals("q3", recent.last())
    }

    @Test
    fun `blank queries are ignored and clear empties the list`() = runTest {
        val store = store()
        store.add("   ")
        assertTrue(store.recent.first().isEmpty())
        store.add("toast")
        store.clear()
        assertTrue(store.recent.first().isEmpty())
    }
}
