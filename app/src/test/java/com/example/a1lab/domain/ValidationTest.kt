package com.example.a1lab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test
    fun validNameHasTwoToFortyCharacters() {
        assertEquals(ValidationResult.Valid, Validation.validateName("Анна"))
        assertEquals(ValidationResult.Valid, Validation.validateName("а".repeat(40)))
        assertTrue(Validation.validateName(" А") is ValidationResult.Invalid)
        assertTrue(Validation.validateName("а".repeat(41)) is ValidationResult.Invalid)
    }

    @Test
    fun loginValidationAcceptsRegularLoginAndEmail() {
        assertEquals(ValidationResult.Valid, Validation.validateLogin("reader"))
        assertEquals(ValidationResult.Valid, Validation.validateLogin("reader@example.com"))
        assertTrue(Validation.validateLogin("ab") is ValidationResult.Invalid)
        assertTrue(Validation.validateLogin("broken@email") is ValidationResult.Invalid)
        assertTrue(Validation.validateLogin("a".repeat(65)) is ValidationResult.Invalid)
    }

    @Test
    fun passwordBoundariesAndConfirmationAreChecked() {
        assertEquals(ValidationResult.Valid, Validation.validatePassword("secret".toCharArray()))
        assertTrue(Validation.validatePassword("12345".toCharArray()) is ValidationResult.Invalid)
        assertTrue(Validation.validatePassword("x".repeat(65).toCharArray()) is ValidationResult.Invalid)
        assertEquals(
            ValidationResult.Valid,
            Validation.validatePasswordConfirmation("secret".toCharArray(), "secret".toCharArray())
        )
        assertTrue(
            Validation.validatePasswordConfirmation("secret".toCharArray(), "other".toCharArray()) is ValidationResult.Invalid
        )
    }

    @Test
    fun optionalAgeHasRequiredRange() {
        assertTrue(Validation.validateAge("6") is ValidationResult.Valid)
        assertTrue(Validation.validateAge("120") is ValidationResult.Valid)
        assertTrue(Validation.validateAge("5") is ValidationResult.Invalid)
        assertTrue(Validation.validateAge("121") is ValidationResult.Invalid)
        assertTrue(Validation.validateAge("text") is ValidationResult.Invalid)
    }
}
