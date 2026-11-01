package co.edu.unal.dadm.mova.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationsTest {
    @Test
    fun `email validation rejects malformed email`() {
        assertEquals("Enter a valid email", validateEmail("invalid"))
    }

    @Test
    fun `password validation accepts eight characters`() {
        assertNull(validatePassword("12345678"))
    }

    @Test
    fun `confirmation validation detects mismatch`() {
        assertEquals(
            "Passwords do not match",
            validatePasswordConfirmation("12345678", "87654321")
        )
    }
}
