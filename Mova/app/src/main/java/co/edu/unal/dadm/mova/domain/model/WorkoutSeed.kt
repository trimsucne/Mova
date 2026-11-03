package co.edu.unal.dadm.mova.domain.model

/** Catalogo semilla de workouts (Kotlin puro; tambien usado por previews y tests). */
object WorkoutSeed {
    private fun ex(prefix: String, names: List<String>, sets: Int, reps: Int) =
        names.mapIndexed { i, n -> WorkoutExercise("$prefix-$i", n, sets, reps) }

    val workouts: List<Workout> = listOf(
        Workout(
            "back", "Back Workout", WorkoutCategory.Strength, "Azunyan U. WU",
            58, 254, 3, 4, "male_athlete",
            ex("back", listOf("Pull-ups", "Bent-over row", "Lat pulldown", "Seated cable row", "Deadlift", "Face pull"), 3, 4)
        ),
        Workout(
            "chest", "Chest Workout", WorkoutCategory.Strength, "Azunyan U. WU",
            50, 230, 3, 10, "female_athlete",
            ex("chest", listOf("Bench press", "Incline dumbbell press", "Cable fly", "Push-ups", "Dips"), 3, 10)
        ),
        Workout(
            "legs", "Leg Day", WorkoutCategory.Strength, "Azunyan U. WU",
            65, 310, 4, 8, "male_athlete",
            ex("legs", listOf("Back squat", "Romanian deadlift", "Walking lunges", "Leg press", "Calf raises"), 4, 8)
        ),
        Workout(
            "shoulders", "Shoulder Workout", WorkoutCategory.Strength, "Azunyan U. WU",
            40, 180, 3, 12, "female_athlete",
            ex("shoulders", listOf("Overhead press", "Lateral raise", "Rear delt fly", "Arnold press"), 3, 12)
        ),
        Workout(
            "arms", "Arms Blaster", WorkoutCategory.Strength, "Azunyan U. WU",
            35, 160, 3, 12, "male_athlete",
            ex("arms", listOf("Barbell curl", "Triceps pushdown", "Hammer curl", "Skull crushers"), 3, 12)
        ),
        Workout(
            "core", "Core Crusher", WorkoutCategory.Strength, null,
            25, 120, 3, 15, "female_athlete",
            ex("core", listOf("Plank", "Russian twist", "Leg raises", "Bicycle crunch"), 3, 15)
        ),
        Workout(
            "run-intervals", "Run Intervals", WorkoutCategory.Cardio, null,
            30, 320, 5, 1, "male_athlete",
            ex("run", listOf("Warm-up jog", "Sprint 30s", "Recovery walk", "Sprint 30s", "Cool-down"), 2, 1)
        ),
        Workout(
            "burpees", "Burpee Burner", WorkoutCategory.Hiit, null,
            20, 280, 4, 10, "male_athlete",
            ex("burpee", listOf("Burpees", "Mountain climbers", "Jump squats", "High knees"), 4, 10)
        ),
        Workout(
            "sun-salutation", "Sun Salutation Flow", WorkoutCategory.Yoga, null,
            30, 110, 3, 5, "female_athlete",
            ex("yoga", listOf("Mountain pose", "Downward dog", "Cobra", "Warrior II"), 3, 5)
        )
    )
}
