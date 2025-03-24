package fi.lempimetsa.kuntavaalit2025

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class VaalikoneResultsServiceITest {

    private val service = VaalikoneResultsService()

    @Test
    fun questionsAndAnswers() = runBlocking {
        val questionsAndAnswers = service.questionsAndAnswers(Municipality.LEMPAALA, INTERESTING_QUESTION_FILTER)
        printQuestionsAndAnswers(questionsAndAnswers)
    }
}
