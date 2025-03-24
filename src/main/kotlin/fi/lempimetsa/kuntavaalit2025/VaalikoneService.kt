package fi.lempimetsa.kuntavaalit2025

import java.util.SortedMap

interface VaalikoneService {
    suspend fun questionsAndAnswers(
        municipality: Municipality,
        questionFilter: (Question) -> Boolean = { _ -> true }
    ): Pair<List<Question>, SortedMap<Candidate, SortedMap<Question, Answer>>>?
}
