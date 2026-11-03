package co.edu.unal.dadm.mova.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Emite el uid actual (o null) cada vez que cambia la sesión. */
fun firebaseAuthUserIds(auth: FirebaseAuth = FirebaseAuth.getInstance()): Flow<String?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
}
