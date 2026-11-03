package co.edu.unal.dadm.mova.data.entity

/** Solo existe en Room; nunca se sube a Firestore. */
enum class SyncState {
    PENDING,
    SYNCED
}
