package co.edu.unal.dadm.mova.data.remote

import co.edu.unal.dadm.mova.domain.model.GeoPoint
import co.edu.unal.dadm.mova.domain.model.RoutePlan
import co.edu.unal.dadm.mova.domain.model.RouteStep
import org.json.JSONObject

/** Convierte la respuesta JSON de OSRM (`geometries=geojson&steps=true`) en un [RoutePlan]. */
internal object OsrmRouteParser {
    fun parse(json: String): RoutePlan? {
        val root = JSONObject(json)
        if (root.optString("code") != "Ok") return null
        val route = root.optJSONArray("routes")?.optJSONObject(0) ?: return null

        val coords = route.optJSONObject("geometry")?.optJSONArray("coordinates") ?: return null
        // GeoJSON es [lng, lat].
        val points = (0 until coords.length()).mapNotNull { i ->
            coords.optJSONArray(i)?.takeIf { it.length() >= 2 }?.let { GeoPoint(it.getDouble(1), it.getDouble(0)) }
        }
        if (points.size < 2) return null

        val steps = mutableListOf<RouteStep>()
        val legs = route.optJSONArray("legs")
        if (legs != null) {
            for (l in 0 until legs.length()) {
                val legSteps = legs.optJSONObject(l)?.optJSONArray("steps") ?: continue
                for (s in 0 until legSteps.length()) {
                    val step = legSteps.optJSONObject(s) ?: continue
                    val maneuver = step.optJSONObject("maneuver")
                    steps += RouteStep(
                        instruction = instruction(
                            type = maneuver?.optString("type").orEmpty(),
                            modifier = maneuver?.optString("modifier").orEmpty(),
                            street = step.optString("name"),
                            bearingAfter = maneuver?.optInt("bearing_after", -1) ?: -1
                        ),
                        distanceMeters = step.optDouble("distance", 0.0)
                    )
                }
            }
        }
        return RoutePlan(
            points = points,
            distanceMeters = route.optDouble("distance", 0.0),
            durationSec = route.optDouble("duration", 0.0),
            steps = steps
        )
    }

    internal fun instruction(type: String, modifier: String, street: String, bearingAfter: Int): String {
        val onto = if (street.isNotBlank()) " onto $street" else ""
        val on = if (street.isNotBlank()) " on $street" else ""
        return when (type) {
            "depart" -> "Head ${compass(bearingAfter)}$on"
            "arrive" -> "Arrive at your destination"
            "roundabout", "rotary" -> "Take the roundabout$onto"
            "turn", "end of road", "fork", "on ramp", "off ramp" -> "${turnVerb(modifier)}$onto"
            else -> if (modifier == "uturn") "Make a U-turn$onto" else "Continue${if (modifier == "straight" || modifier.isEmpty()) "" else " ${modifier}"}$on"
        }
    }

    private fun turnVerb(modifier: String) = when (modifier) {
        "left" -> "Turn left"
        "right" -> "Turn right"
        "slight left" -> "Keep left"
        "slight right" -> "Keep right"
        "sharp left" -> "Turn sharp left"
        "sharp right" -> "Turn sharp right"
        "uturn" -> "Make a U-turn"
        else -> "Continue"
    }

    private fun compass(bearing: Int): String {
        if (bearing < 0) return "out"
        val names = listOf("north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west")
        return names[(((bearing % 360) + 22.5) / 45).toInt() % 8]
    }
}
