package co.edu.unal.dadm.mova.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.edu.unal.dadm.mova.domain.repository.RecentSearchesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.searchDataStore by preferencesDataStore("search_preferences")

class RecentSearchesDataSource(private val dataStore: DataStore<Preferences>) : RecentSearchesStore {

    override val recent: Flow<List<String>> = dataStore.data.map { decode(it[KEY]) }

    override suspend fun add(query: String) {
        val clean = query.trim().replace('\n', ' ')
        if (clean.isEmpty()) return
        dataStore.edit { prefs ->
            val updated = listOf(clean) + decode(prefs[KEY]).filterNot { it.equals(clean, ignoreCase = true) }
            prefs[KEY] = updated.take(MAX_RECENT).joinToString("\n")
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.remove(KEY) }
    }

    private fun decode(raw: String?): List<String> = raw?.split('\n')?.filter { it.isNotBlank() }.orEmpty()

    companion object {
        private val KEY = stringPreferencesKey("recent_searches")
        private const val MAX_RECENT = 8

        fun create(context: Context) = RecentSearchesDataSource(context.applicationContext.searchDataStore)
    }
}
