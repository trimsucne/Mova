package co.edu.unal.dadm.mova.core.health

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord

/** Permisos de Health Connect que usa la app y utilidades de disponibilidad. */
object HealthPermissions {
    val required: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class)
    )

    /** Health Connect requiere API 26+ y la app/proveedor instalado. */
    fun isAvailable(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    suspend fun hasAll(context: Context): Boolean {
        if (!isAvailable(context)) return false
        val granted = HealthConnectClient.getOrCreate(context).permissionController.getGrantedPermissions()
        return granted.containsAll(required)
    }

    /** true si Health Connect existe pero el usuario aun no concedio los permisos. */
    suspend fun needsPermission(context: Context): Boolean = isAvailable(context) && !hasAll(context)
}
