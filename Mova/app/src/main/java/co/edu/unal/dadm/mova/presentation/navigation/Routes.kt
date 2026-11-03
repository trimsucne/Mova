package co.edu.unal.dadm.mova.presentation.navigation

import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

object Routes {
    const val Welcome = "welcome"
    const val SignIn = "sign_in"
    const val SignUp = "sign_up"
    const val ResetPassword = "reset_password"
    const val ResetMethod = "reset_method"
    const val ResetSent = "reset_sent"
    const val Onboarding = "onboarding"
    const val Assessment = "assessment"
    const val Main = "main"
    const val Profile = "profile"
    const val AvatarPicker = "avatar_picker"
    const val Settings = "settings"
    const val SettingsNotifications = "settings_notifications"
    const val SettingsPersonalInfo = "settings_personal_info"
    const val SettingsCoachContact = "settings_coach_contact"
    const val SettingsLinkedDevices = "settings_linked_devices"
    const val SettingsMainSecurity = "settings_main_security"
    const val Premium = "premium"

    // Home (PR #5). Coinciden con las rutas de las pestañas de MovaBottomBar.
    const val Notifications = "notifications"
    const val UpliftScore = "uplift_score"

    // Nutrición (spec §6). date es ISO (2026-11-02) y opcional: por defecto, hoy.
    const val NutritionIntake = "nutrition/intake"
    const val NutritionMyMeals = "nutrition/my_meals?date={date}"
    const val NutritionMeal = "nutrition/meal/{mealId}?date={date}"
    const val NutritionAdd = "nutrition/add?type={type}&date={date}"
    const val NutritionAdded = "nutrition/added?date={date}"
    const val NutritionExplore = "nutrition/explore"
    const val NutritionStats = "nutrition/stats"

    fun nutritionMyMeals(date: LocalDate? = null) = "nutrition/my_meals" + query("date" to date)
    fun nutritionMeal(mealId: String, date: LocalDate? = null) = "nutrition/meal/$mealId" + query("date" to date)
    fun nutritionAdd(type: MealType? = null, date: LocalDate? = null) =
        "nutrition/add" + query("type" to type?.name, "date" to date)
    fun nutritionAdded(date: LocalDate) = "nutrition/added?date=$date"

    // Búsqueda (spec §5). Home navega con Routes.search(texto).
    const val Search = "search?q={q}"
    fun search(query: String? = null) = "search" + query("q" to query?.let { android.net.Uri.encode(it) })

    private fun query(vararg params: Pair<String, Any?>): String =
        params.filter { it.second != null }
            .joinToString("&") { "${it.first}=${it.second}" }
            .let { if (it.isEmpty()) "" else "?$it" }
}

/** Rutas del modulo Fitness (Dev C): Workouts + Actividades/Tracking/Mapas. */
object FitnessRoutes {
    const val Hub = "fitness_hub"

    const val WorkoutCategory = "workout_category/{category}"
    const val WorkoutDetail = "workout/{workoutId}"
    const val WorkoutSession = "workout/{workoutId}/session"
    const val WorkoutSummary = "workout_summary/{sessionId}"

    const val ActivityStatus = "activity/status"
    const val ActivityAdd = "activity/add"
    const val ActivityTrack = "activity/{typeId}/track"
    const val ActivitySummary = "activity_summary/{logId}"
    const val HeartRate = "activity/heart_rate"

    const val NearbyGyms = "places/gyms"
    const val Directions = "places/gyms/{gymId}/directions"

    fun workoutCategory(category: String) = "workout_category/$category"
    fun workoutDetail(id: String) = "workout/$id"
    fun workoutSession(id: String) = "workout/$id/session"
    fun workoutSummary(sessionId: String) = "workout_summary/$sessionId"
    fun activityTrack(typeId: String) = "activity/$typeId/track"
    fun activitySummary(logId: String) = "activity_summary/$logId"
    fun directions(gymId: String) = "places/gyms/$gymId/directions"
}
