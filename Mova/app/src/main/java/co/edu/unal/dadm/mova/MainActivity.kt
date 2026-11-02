package co.edu.unal.dadm.mova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unal.dadm.mova.data.preferences.UserPreferencesImpl
import co.edu.unal.dadm.mova.data.repository.AssessmentRepositoryImpl
import co.edu.unal.dadm.mova.data.repository.AuthRepositoryImpl
import co.edu.unal.dadm.mova.presentation.navigation.MovaNavHost
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences = UserPreferencesImpl(this@MainActivity)
            val settings by preferences.preferences.collectAsStateWithLifecycle(
                initialValue = co.edu.unal.dadm.mova.domain.repository.UserPreferences()
            )
            MovaTheme(darkTheme = settings.darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MovaNavHost(
                        authRepository = AuthRepositoryImpl(),
                        assessmentRepository = AssessmentRepositoryImpl()
                    )
                }
            }
        }
    }
}
