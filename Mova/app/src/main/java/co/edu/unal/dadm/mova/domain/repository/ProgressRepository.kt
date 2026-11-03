package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.ScoreSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Uplift Score y su histórico.
 *
 * Temporary fake until the Progress/Uplift module is implemented: solo lo que usa PR #5.
 */
interface ProgressRepository {
    fun observeLatestScore(userId: String): Flow<ScoreSnapshot?>

    /** Últimos 7 días, de lunes a domingo (o del más antiguo al más reciente). */
    fun observeWeeklyScores(userId: String): Flow<List<ScoreSnapshot>>
}
