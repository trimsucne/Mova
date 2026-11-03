package co.edu.unal.dadm.mova

import android.app.Application
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import com.google.firebase.FirebaseApp

class MovaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FitnessContainer.init(this)
    }
}
