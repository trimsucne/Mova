package co.edu.unal.dadm.mova.data.tracking

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.domain.model.ActivityType
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Servicio en primer plano (tipo location) que mantiene vivo el tracking con la pantalla
 * apagada. Es dueno del GPS y del cronometro; alimenta a [TrackingSession].
 */
class TrackingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ticker: Job? = null
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var locationActive = false

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val session = FitnessContainer.trackingSession
            result.locations.forEach { loc ->
                session.onLocation(
                    loc.latitude, loc.longitude, loc.time,
                    if (loc.hasAccuracy()) loc.accuracy else null
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val usesGps = intent.getBooleanExtra(EXTRA_USES_GPS, true)
                startAsForeground()
                startTicker()
                if (usesGps) startLocationUpdates()
            }
            ACTION_STOP -> shutdown()
        }
        return START_NOT_STICKY
    }

    private fun startAsForeground() {
        ensureChannel()
        val label = FitnessContainer.trackingSession.state.value.type?.label ?: "Activity"
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mova is tracking your $label")
            .setContentText("Tap to return to the app")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(
                android.app.PendingIntent.getActivity(
                    this, 0,
                    packageManager.getLaunchIntentForPackage(packageName),
                    android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                delay(1_000)
                FitnessContainer.trackingSession.tick()
            }
        }
    }

    @SuppressLint("MissingPermission") // El permiso se solicita en la UI antes de iniciar el servicio.
    private fun startLocationUpdates() {
        if (locationActive) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3_000L)
            .setMinUpdateDistanceMeters(2f)
            .build()
        try {
            fused.requestLocationUpdates(request, callback, mainLooper)
            locationActive = true
        } catch (_: SecurityException) {
            // Sin permiso: el cronometro sigue, pero no hay ruta.
        }
    }

    private fun shutdown() {
        ticker?.cancel()
        if (locationActive) fused.removeLocationUpdates(callback)
        locationActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        if (locationActive) fused.removeLocationUpdates(callback)
        scope.cancel()
        super.onDestroy()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Activity tracking", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    companion object {
        const val ACTION_START = "co.edu.unal.dadm.mova.TRACKING_START"
        const val ACTION_STOP = "co.edu.unal.dadm.mova.TRACKING_STOP"
        const val EXTRA_USES_GPS = "uses_gps"
        private const val CHANNEL_ID = "activity_tracking"
        private const val NOTIFICATION_ID = 4201

        fun startIntent(context: Context, type: ActivityType) =
            Intent(context, TrackingService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_USES_GPS, type.usesGps)

        fun stopIntent(context: Context) =
            Intent(context, TrackingService::class.java).setAction(ACTION_STOP)
    }
}
