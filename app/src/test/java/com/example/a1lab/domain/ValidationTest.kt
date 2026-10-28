package com.example.a1lab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test
    fun validNameHasTwoToFortyCharacters() {
        assertEquals(ValidationResult.Valid, Validation.name("Анна"))
        assertEquals(ValidationResult.Valid, Validation.name("а".repeat(40)))
        assertTrue(Validation.name(" А") is ValidationResult.Invalid)
        assertTrue(Validation.name("а".repeat(41)) is ValidationResult.Invalid)
    }

    @Test
    fun loginValidationAcceptsRegularLoginAndEmail() {
        assertEquals(ValidationResult.Valid, Validation.login("reader"))
        assertEquals(ValidationResult.Valid, Validation.login("reader@example.com"))
        assertTrue(Validation.login("ab") is ValidationResult.Invalid)
        assertTrue(Validation.login("broken@email") is ValidationResult.Invalid)
        assertTrue(Validation.login("a".repeat(65)) is ValidationResult.Invalid)
    }

    @Test
    fun passwordBoundariesAndConfirmationAreChecked() {
        assertEquals(ValidationResult.Valid, Validation.password("secret".toCharArray()))
        assertTrue(Validation.password("12345".toCharArray()) is ValidationResult.Invalid)
        assertTrue(Validation.password("x".repeat(65).toCharArray()) is ValidationResult.Invalid)
        assertEquals(
            ValidationResult.Valid,
            Validation.confirmation("secret".toCharArray(), "secret".toCharArray())
        )
        assertTrue(
            Validation.confirmation("secret".toCharArray(), "other".toCharArray()) is ValidationResult.Invalid
        )
    }

    @Test
    fun optionalAgeHasRequiredRange() {
        assertTrue(Validation.age("6") is ValidationResult.Valid)
        assertTrue(Validation.age("120") is ValidationResult.Valid)
        assertTrue(Validation.age("5") is ValidationResult.Invalid)
        assertTrue(Validation.age("121") is ValidationResult.Invalid)
        assertTrue(Validation.age("text") is ValidationResult.Invalid)
    }
}
