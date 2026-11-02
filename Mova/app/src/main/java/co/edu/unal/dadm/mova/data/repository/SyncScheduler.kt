package co.edu.unal.dadm.mova.data.repository

/** Programa un reintento de subida para cuando vuelva la conexión. */
fun interface SyncScheduler {
    fun schedule()
}
