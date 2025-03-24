package fi.lempimetsa.kuntavaalit2025.lvs

import fi.lempimetsa.kuntavaalit2025.INTERESTING_QUESTION_FILTER
import fi.lempimetsa.kuntavaalit2025.Municipality
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class LVSServiceITest {

    private val service = LVSService()

    @Test
    fun questionsAndAnswers() = runBlocking {
        val questionsAndAnswers = service.questionsAndAnswers(Municipality.LEMPAALA, INTERESTING_QUESTION_FILTER)
        println(questionsAndAnswers)
    }

}