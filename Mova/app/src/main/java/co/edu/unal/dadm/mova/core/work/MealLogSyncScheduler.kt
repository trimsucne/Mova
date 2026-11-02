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
