package fi.lempimetsa.kuntavaalit2025.facebook

import fi.lempimetsa.kuntavaalit2025.Answer
import fi.lempimetsa.kuntavaalit2025.Candidate
import fi.lempimetsa.kuntavaalit2025.FacebookQuestionOption
import fi.lempimetsa.kuntavaalit2025.Question
import java.util.SortedMap
import java.util.TreeMap

class FacebookService {

    private val parser = FacebookHtmlParser()

    fun questionsAndAnswers(candidates: Set<Candidate>): Pair<List<Question>, SortedMap<Candidate, SortedMap<Question, Answer>>> {
        val questions = mutableListOf<Question>()
        val answers = TreeMap<Candidate, SortedMap<Question, Answer>>()
        FacebookQuestionOption.entries.forEach { option ->
            val question = option.toQuestion()
            questions.add(question)
            val names = parser.parseNames(option)
            val namedCandidates = namesToCandidates(names, candidates)
            if (namedCandidates.isEmpty()) {
                candidates.forEach { candidate ->
                    val candidateAnswers = answers.computeIfAbsent(candidate) { TreeMap() }
                    candidateAnswers[question] = Answer(question, 11, "")
                }
            } else {
                namedCandidates.forEach { candidate ->
                    val candidateAnswers = answers.computeIfAbsent(candidate) { TreeMap() }
                    candidateAnswers[question] = Answer(question, 10, "")
                }
            }
        }
        return Pair(questions, answers)
    }

    private fun namesToCandidates(names: List<Pair<String, String>>, candidates: Set<Candidate>): List<Candidate> {
        val namedCandidates = mutableListOf<Candidate>()
        names.forEach { (firstName, lastName) ->
            val candidate = candidates.find {
                (it.firstName == firstName && it.lastName == lastName) ||
                        (it.firstName == lastName && it.lastName == firstName) ||
                        (lastName.contains(it.lastName) && firstName.contains(it.firstName)) ||
                        (it.lastName == "Jääskeläinen" && firstName == "Jääskeläisen" && it.firstName == "Rauno" && lastName == "Rane") ||
                        (it.lastName == "Hankala-Vuorinen" && firstName == "Hankala-Vuorisen" && it.firstName == "Minna" && lastName == "Minna") ||
                        (it.lastName == "Kairimo" && firstName == "Kairimon" && it.firstName == "Hanna" && lastName == "Hanna") ||
                        (it.lastName == "Vänskä" && lastName == "Vihreät" && it.firstName == "Laura" && firstName == "Laura Vänskä Tampereen")
            }
            if (candidate != null) {
                namedCandidates.add(candidate)
            }
        }
        return namedCandidates
    }
}