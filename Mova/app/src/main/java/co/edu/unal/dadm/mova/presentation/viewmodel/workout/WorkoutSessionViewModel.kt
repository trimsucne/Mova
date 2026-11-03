package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import co.edu.unal.dadm.mova.domain.usecase.CompleteWorkoutSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutSessionViewModel(
    private val workoutId: String,
    private val repository: WorkoutRepository,
    private val completeSession: CompleteWorkoutSessionUseCase,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {
    private val startedAt = clock()
    private val _state = MutableStateFlow(WorkoutSessionUiState())
    val state: StateFlow<WorkoutSessionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val workout = repository.observeWorkout(workoutId).first()
            _state.update { it.copy(loading = false, workout = workout) }
        }
    }

    fun onEvent(event: WorkoutSessionEvent) {
        when (event) {
            WorkoutSessionEvent.Tick -> tick()
            WorkoutSessionEvent.CompleteSet -> completeSet()
            WorkoutSessionEvent.SkipRest -> _state.update { it.copy(resting = false, restRemainingSec = 0) }
            WorkoutSessionEvent.TogglePause -> _state.update { it.copy(paused = !it.paused) }
            WorkoutSessionEvent.FinishClicked -> finish()
            WorkoutSessionEvent.BackClicked -> Unit // lo resuelve la Route
        }
    }

    private fun tick() = _state.update { s ->
        if (s.loading || s.paused || s.savedSessionId != null || s.workout == null) return@update s
        val rest = if (s.resting) s.restRemainingSec - 1 else 0
        s.copy(
            elapsedSec = s.elapsedSec + 1,
            resting = s.resting && rest > 0,
            restRemainingSec = rest.coerceAtLeast(0)
        )
    }

    private fun completeSet() = _state.update { s ->
        val exercise = s.currentExercise
        if (exercise == null || s.allDone || s.resting || s.paused) return@update s
        val doneInEx = s.setsDoneInExercise + 1
        val completed = s.completedSets + 1
        if (doneInEx < exercise.sets) {
            s.copy(setsDoneInExercise = doneInEx, completedSets = completed, resting = true, restRemainingSec = exercise.restSeconds)
        } else if (s.nextExercise != null) {
            s.copy(
                exerciseIndex = s.exerciseIndex + 1, setsDoneInExercise = 0, completedSets = completed,
                resting = true, restRemainingSec = exercise.restSeconds
            )
        } else {
            s.copy(setsDoneInExercise = doneInEx, completedSets = completed, allDone = true)
        }
    }

    private fun finish() {
        val s = _state.value
        val workout = s.workout ?: return
        if (s.saving || s.savedSessionId != null) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val log = completeSession(
                workout = workout,
                startedAt = startedAt,
                durationSec = s.elapsedSec,
                completedSets = s.completedSets
            )
            _state.update { it.copy(saving = false, savedSessionId = log.id) }
        }
    }
}
