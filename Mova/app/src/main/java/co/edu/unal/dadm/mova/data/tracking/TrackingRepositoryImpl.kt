package co.edu.unal.dadm.mova.data.tracking

import android.content.Context
import androidx.core.content.ContextCompat
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.TrackingState
import co.edu.unal.dadm.mova.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.StateFlow

/** Orquesta [TrackingSession] y el [TrackingService] de primer plano. */
class TrackingRepositoryImpl(
    private val context: Context,
    private val session: TrackingSession
) : TrackingRepository {
    override val state: StateFlow<TrackingState> get() = session.state

    override fun start(type: ActivityType) {
        session.start(type)
        ContextCompat.startForegroundService(context, TrackingService.startIntent(context, type))
    }

    override fun pause() = session.pause()

    override fun resume() = session.resume()

    override fun stop(): TrackingState? {
        val result = session.finish()
        context.startService(TrackingService.stopIntent(context))
        return result
    }
}
