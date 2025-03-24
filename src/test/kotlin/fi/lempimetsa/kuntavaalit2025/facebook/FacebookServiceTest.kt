package fi.lempimetsa.kuntavaalit2025.facebook

import fi.lempimetsa.kuntavaalit2025.Candidate
import fi.lempimetsa.kuntavaalit2025.Party
import org.junit.jupiter.api.Test

class FacebookServiceTest {

    private val service = FacebookService()

    @Test
    fun questionsAndAnswers() {
        val answers = service.questionsAndAnswers(setOf(Candidate("Ville", "Karhila", 0, Party.SIT)))
        println(answers)
        assert(answers.first.size == 5)
        assert(answers.second.size == 1)
    }
}
