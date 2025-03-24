package fi.lempimetsa.kuntavaalit2025.aamulehti

import fi.lempimetsa.kuntavaalit2025.Candidates
import fi.lempimetsa.kuntavaalit2025.Party
import fi.lempimetsa.kuntavaalit2025.Source
import fi.lempimetsa.kuntavaalit2025.VaalikoneService
import fi.lempimetsa.kuntavaalit2025.adjustFirstName
import fi.lempimetsa.kuntavaalit2025.adjustLastName
import java.util.SortedMap
import java.util.TreeMap

class AamulehtiService : VaalikoneService {

    private val client = AamulehtiClient()

    companion object {
        val SOURCE = Source.AAMULEHTI
    }

    override suspend fun questionsAndAnswers(
        municipality: fi.lempimetsa.kuntavaalit2025.Municipality,
        questionFilter: (fi.lempimetsa.kuntavaalit2025.Question) -> Boolean
    ): Pair<List<fi.lempimetsa.kuntavaalit2025.Question>, SortedMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>>? {
        val sourceMunicipality = Municipality.valueOfName(municipality.title)
        val sourceQuestions = questions(sourceMunicipality, Brand.AAMULEHTI)
        val sourceAnswers = answers(sourceMunicipality, sourceQuestions)
        val questions = sourceQuestions.map { fi.lempimetsa.kuntavaalit2025.Question(SOURCE, it.id, it.text) }
            .filter { questionFilter(it) }
        val answers =
            TreeMap<fi.lempimetsa.kuntavaalit2025.Candidate, SortedMap<fi.lempimetsa.kuntavaalit2025.Question, fi.lempimetsa.kuntavaalit2025.Answer>>()
        sourceAnswers.forEach { (sourceCandidate, sourceAnswers) ->
            val party = Party.valueOfShortName(sourceCandidate.partyObject.shortName)
                ?: throw IllegalStateException("Party ${sourceCandidate.partyObject.shortName} not found")
            var candidate = fi.lempimetsa.kuntavaalit2025.Candidate(
                sourceCandidate.firstName.adjustFirstName(municipality, sourceCandidate.lastName.trim()),
                sourceCandidate.lastName.adjustLastName(municipality, party, sourceCandidate.firstName.trim()),
                sourceCandidate.candidateNumber,
                party
            )
            candidate = Candidates.putIfAbsent(municipality, candidate)
            val candidateAnswers = sourceAnswers.mapNotNull { (question, answer) ->
                val question = questions.find { it.id == question.id }
                if (question != null) {
                    val answer = fi.lempimetsa.kuntavaalit2025.Answer(question, answer.value, answer.explanation)
                    Pair(question, answer)
                } else {
                    null
                }
            }.toMap()
            answers.computeIfAbsent(candidate) { TreeMap() }.putAll(candidateAnswers)
        }
        return Pair(questions, answers)
    }

    /**
     * Returns all questions that are common or related to the given municipality.
     */
    suspend fun questions(municipality: Municipality, brand: Brand): List<Question> =
        client.candidates().questions.filter { it.isMunicipalityTheme(municipality) && it.isBrand(brand) }
            .sortedBy { it.id }

    /**
     * Returns answers for the given municipality and questions.
     */
    suspend fun answers(
        municipality: Municipality,
        questions: List<Question> = emptyList()
    ): SortedMap<Candidate, SortedMap<Question, Answer>> {
        val answers = sortedMapOf<Candidate, SortedMap<Question, Answer>>()
        val parties = client.candidates().parties.associateBy { it.id }
        val candidates = client.candidateSearch(municipality)
        candidates.forEach { candidate ->
            val candidateQuestions = client.candidateQuestions(candidate)
            val candidateNumber = candidateQuestions.transformedCandidateInformation.candidateNumber
            val party =
                parties[candidate.party] ?: error("Party ${candidate.party} not found for candidate ${candidate.id}")
            val candidate = candidate.copy(candidateNumber = candidateNumber, partyObject = party)
            answers.put(
                candidate,
                candidateQuestions.answersForQuestion(questions)
            )
        }
        return answers
    }
}
