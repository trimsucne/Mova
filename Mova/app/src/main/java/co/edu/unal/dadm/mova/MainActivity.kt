package co.edu.unal.dadm.mova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.unal.dadm.mova.presentation.navigation.MovaNavHost
import co.edu.unal.dadm.mova.data.repository.AuthRepositoryImpl
import co.edu.unal.dadm.mova.ui.theme.MovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovaTheme {
                MovaNavHost(authRepository = AuthRepositoryImpl())
            }
        }
    }
}
