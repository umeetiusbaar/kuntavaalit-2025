package fi.lempimetsa.kuntavaalit2025

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class VaalikoneResultsServiceITest {

    private val service = VaalikoneAnswersService()

    @Test
    fun questionsAndAnswers() = runBlocking {
        val questionsAndAnswers = service.questionsAndAnswers(Municipality.TAMPERE, INTERESTING_QUESTION_FILTER)
        printQuestionsAndAnswers(questionsAndAnswers)
    }
}
