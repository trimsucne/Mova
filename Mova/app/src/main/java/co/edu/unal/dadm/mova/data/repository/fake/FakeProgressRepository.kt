package co.edu.unal.dadm.mova.data.repository.fake

import co.edu.unal.dadm.mova.domain.model.ScoreSnapshot
import co.edu.unal.dadm.mova.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** In-memory ProgressRepository. Temporary fake until the Progress/Uplift module is implemented. */
class FakeProgressRepository(today: LocalDate = LocalDate.now()) : ProgressRepository {
    private val weekly: List<ScoreSnapshot> = listOf(78, 80, 79, 82, 81, 85, 84).mapIndexed { index, score ->
        val date = today.minusDays((6 - index).toLong())
        ScoreSnapshot(
            date = date,
            dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            score = score,
            healthStatus = "Optimal Health",
            strength = 88,
            agility = 76,
            endurance = 82
        )
    }

    override fun observeLatestScore(userId: String): Flow<ScoreSnapshot?> = flowOf(weekly.last())

    override fun observeWeeklyScores(userId: String): Flow<List<ScoreSnapshot>> = flowOf(weekly)
}
