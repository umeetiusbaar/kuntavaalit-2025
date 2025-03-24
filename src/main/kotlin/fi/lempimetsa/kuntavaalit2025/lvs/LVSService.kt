package fi.lempimetsa.kuntavaalit2025.lvs

import fi.lempimetsa.kuntavaalit2025.Candidates
import fi.lempimetsa.kuntavaalit2025.Municipality
import fi.lempimetsa.kuntavaalit2025.Party
import fi.lempimetsa.kuntavaalit2025.Source
import fi.lempimetsa.kuntavaalit2025.VaalikoneService
import fi.lempimetsa.kuntavaalit2025.adjustFirstName
import fi.lempimetsa.kuntavaalit2025.adjustLastName
import fi.lempimetsa.kuntavaalit2025.adjustNumber
import java.util.SortedMap
import java.util.TreeMap

class LVSService : VaalikoneService {

    private val client = LVSClient()

    override suspend fun questionsAndAnswers(
        municipality: Municipality,
        questionFilter: (fi.lempimetsa.kuntavaalit2025.Question) -> Boolean
    ): Pair<List<fi.lempimetsa.kuntavaalit2025.Question>, SortedMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>>? {
        val election = Election.valueOfName(municipality.title)
        if (election == null) return null
        val sourceQuestions = questions(election)
        val sourceAnswers = answers(election, sourceQuestions)
        val questions = sourceQuestions.map {
            fi.lempimetsa.kuntavaalit2025.Question(
                Source.LVS,
                it.id,
                it.title.substringAfter(". ")
            )
        }
            .filter { questionFilter(it) }
        val answers =
            TreeMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>()
        sourceAnswers.forEach { (sourceCandidate, sourceAnswers) ->
            val party = Party.valueOfShortName(sourceCandidate.party.shortName)
                ?: throw IllegalStateException("Party ${sourceCandidate.party.shortName} not found")

            var candidate = fi.lempimetsa.kuntavaalit2025.Candidate(
                sourceCandidate.firstName.adjustFirstName(municipality, sourceCandidate.lastName.trim()),
                sourceCandidate.lastName.adjustLastName(municipality, party, sourceCandidate.firstName.trim()),
                sourceCandidate.candidateNumber.adjustNumber(
                    municipality,
                    sourceCandidate.firstName.adjustFirstName(municipality, sourceCandidate.lastName.trim()),
                    sourceCandidate.lastName.adjustLastName(municipality, party, sourceCandidate.firstName.trim())
                ),
                party
            )
            candidate = Candidates.putIfAbsent(municipality, candidate)
            val candidateAnswers = sourceAnswers.mapNotNull { (question, answer) ->
                val question = questions.find { it.id == question.id }
                if (question != null) {
                    val answer = fi.lempimetsa.kuntavaalit2025.Answer(question, answer.answer, answer.explanation)
                    Pair(question, answer)
                } else {
                    null
                }
            }.toMap()
            answers.computeIfAbsent(candidate) { TreeMap() }.putAll(candidateAnswers)
        }
        return Pair(questions, answers)
    }

    suspend fun questions(election: Election): List<Question> =
        client.questions(election.id).data

    suspend fun answers(
        election: Election,
        questions: List<Question>
    ): SortedMap<Candidate, SortedMap<Question, Answer>> {
        val answers = sortedMapOf<Candidate, SortedMap<Question, Answer>>()
        val candidates = client.candidates(election.id)
        candidates.forEach { candidate ->
            val candidateAnsers = answers.computeIfAbsent(candidate) { TreeMap() }
            questions.forEach { question ->
                val answer = candidate.answers.find { it.questionId == question.id } ?: Answer.Companion.EMPTY
                candidateAnsers.put(question, answer)
            }
        }
        return answers
    }
}
