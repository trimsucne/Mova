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
