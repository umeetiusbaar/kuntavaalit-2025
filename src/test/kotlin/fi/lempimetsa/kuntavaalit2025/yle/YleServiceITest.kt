package fi.lempimetsa.kuntavaalit2025.yle

import fi.lempimetsa.kuntavaalit2025.Municipality
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class YleServiceITest {

    private val service = YleService()

    @Test
    fun questionsAndAnswers() = runBlocking {
        println(service.questionsAndAnswers(Municipality.LEMPAALA))
    }

    @Test
    fun candidateResults() = runBlocking {
        println(service.candidateResults(Municipality.LEMPAALA))
    }
}
