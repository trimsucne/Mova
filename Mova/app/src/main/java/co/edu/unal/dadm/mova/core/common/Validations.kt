package co.edu.unal.dadm.mova.core.common

private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

fun validateEmail(email: String): String? =
    when {
        email.isBlank() -> "Email is required"
        !emailPattern.matches(email.trim()) -> "Enter a valid email"
        else -> null
    }

fun validatePassword(password: String): String? =
    when {
        password.isBlank() -> "Password is required"
        password.length < 8 -> "Password must contain at least 8 characters"
        else -> null
    }

fun validatePasswordConfirmation(password: String, confirmation: String): String? =
    when {
        confirmation.isBlank() -> "Confirm your password"
        password != confirmation -> "Passwords do not match"
        else -> null
    }
