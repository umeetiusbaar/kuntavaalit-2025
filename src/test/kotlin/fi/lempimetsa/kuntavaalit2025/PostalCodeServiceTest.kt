package fi.lempimetsa.kuntavaalit2025

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PostalCodeServiceTest {

    private val postalCodeService = PostalCodeService()

    @Test
    fun postOffice() {
        val postOffice = postalCodeService.postOffice("33100")
        assertEquals("Tampere 10", postOffice)
    }
}