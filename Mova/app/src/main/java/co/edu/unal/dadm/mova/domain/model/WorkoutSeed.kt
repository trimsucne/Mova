package co.edu.unal.dadm.mova.domain.model

/** Catalogo semilla de workouts (Kotlin puro; tambien usado por previews y tests). */
object WorkoutSeed {
    private fun ex(prefix: String, names: List<String>, sets: Int, reps: Int) =
        names.mapIndexed { i, n -> WorkoutExercise("$prefix-$i", n, sets, reps) }

    val workouts: List<Workout> = listOf(
        Workout(
            id = "back", name = "Back Workout", category = WorkoutCategory.STRENGTH, durationMin = 58,
            imageRes = "male_athlete", caloriesBurned = 254, trainer = "Azunyan U. WU", sets = 3, reps = 4,
            exercises = ex("back", listOf("Pull-ups", "Bent-over row", "Lat pulldown", "Seated cable row", "Deadlift", "Face pull"), 3, 4)
        ),
        Workout(
            id = "chest", name = "Chest Workout", category = WorkoutCategory.STRENGTH, durationMin = 50,
            imageRes = "female_athlete", caloriesBurned = 230, trainer = "Azunyan U. WU", sets = 3, reps = 10,
            exercises = ex("chest", listOf("Bench press", "Incline dumbbell press", "Cable fly", "Push-ups", "Dips"), 3, 10)
        ),
        Workout(
            id = "legs", name = "Leg Day", category = WorkoutCategory.STRENGTH, durationMin = 65,
            imageRes = "male_athlete", caloriesBurned = 310, trainer = "Azunyan U. WU", sets = 4, reps = 8,
            exercises = ex("legs", listOf("Back squat", "Romanian deadlift", "Walking lunges", "Leg press", "Calf raises"), 4, 8)
        ),
        Workout(
            id = "shoulders", name = "Shoulder Workout", category = WorkoutCategory.STRENGTH, durationMin = 40,
            imageRes = "female_athlete", caloriesBurned = 180, trainer = "Azunyan U. WU", sets = 3, reps = 12,
            exercises = ex("shoulders", listOf("Overhead press", "Lateral raise", "Rear delt fly", "Arnold press"), 3, 12)
        ),
        Workout(
            id = "arms", name = "Arms Blaster", category = WorkoutCategory.STRENGTH, durationMin = 35,
            imageRes = "male_athlete", caloriesBurned = 160, trainer = "Azunyan U. WU", sets = 3, reps = 12,
            exercises = ex("arms", listOf("Barbell curl", "Triceps pushdown", "Hammer curl", "Skull crushers"), 3, 12)
        ),
        Workout(
            id = "core", name = "Core Crusher", category = WorkoutCategory.STRENGTH, durationMin = 25,
            imageRes = "female_athlete", caloriesBurned = 120, trainer = null, sets = 3, reps = 15,
            exercises = ex("core", listOf("Plank", "Russian twist", "Leg raises", "Bicycle crunch"), 3, 15)
        ),
        Workout(
            id = "run-intervals", name = "Run Intervals", category = WorkoutCategory.CARDIO, durationMin = 30,
            imageRes = "male_athlete", caloriesBurned = 320, trainer = null, sets = 5, reps = 1,
            exercises = ex("run", listOf("Warm-up jog", "Sprint 30s", "Recovery walk", "Sprint 30s", "Cool-down"), 2, 1)
        ),
        Workout(
            id = "burpees", name = "Burpee Burner", category = WorkoutCategory.HIIT, durationMin = 20,
            imageRes = "male_athlete", caloriesBurned = 280, trainer = null, sets = 4, reps = 10,
            exercises = ex("burpee", listOf("Burpees", "Mountain climbers", "Jump squats", "High knees"), 4, 10)
        ),
        Workout(
            id = "sun-salutation", name = "Sun Salutation Flow", category = WorkoutCategory.YOGA, durationMin = 30,
            imageRes = "female_athlete", caloriesBurned = 110, trainer = null, sets = 3, reps = 5,
            exercises = ex("yoga", listOf("Mountain pose", "Downward dog", "Cobra", "Warrior II"), 3, 5)
        )
    )
}
