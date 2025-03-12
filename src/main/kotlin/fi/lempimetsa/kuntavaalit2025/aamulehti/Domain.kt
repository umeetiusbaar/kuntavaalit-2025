package fi.lempimetsa.kuntavaalit2025.aamulehti

import java.util.SortedMap

val COMMONT_THEMES = listOf(1, 2, 3, 4, 52, 53, 54, 55, 56, 57, 58)

enum class Municipality(val code: Int, val themes: List<Int>, val label: String) {
    LEMPAALA(418, COMMONT_THEMES + 16, "Lempäälä"),
    PIRKKALA(604, COMMONT_THEMES + 21, "Pirkkala"),
    TAMPERE(837, COMMONT_THEMES + listOf(8, 64), "Tampere");
}

enum class Brand(val shortName: String) {
    AAMULEHTI("al"),
}

data class Theme(
    val id: Int,
    val name: String,
    val description: String,
)

data class Party(
    val id: String,
    val name: String,
    val shortName: String,
)

data class Question(
    val id: Int,
    val text: String,
    val theme: Int,
    val brand: List<String>,
) : Comparable<Question> {
    fun isMunicipalityTheme(municipality: Municipality): Boolean = municipality.themes.contains(theme)

    fun isBrand(brand: Brand) = this.brand.contains(brand.shortName)

    override fun compareTo(other: Question): Int {
        return compareValuesBy(this, other) { it.id }
    }
}

data class Candidate(
    val id: String,
    val lastName: String,
    val firstName: String,
    val party: String,
) : Comparable<Candidate> {
    override fun compareTo(other: Candidate): Int {
        return compareValuesBy(this, other, { it.lastName.trim() }, { it.firstName.trim() })
    }

    fun fullName() = fi.lempimetsa.kuntavaalit2025.fullName(firstName, lastName)
}

enum class Answer(val value: Int) {
    NOT_ANSWERED(0),
    STRONGLY_DISAGREE(1),
    SOMEWHAT_DISAGREE(2),
    NEITHER_AGREE_NOR_DISAGREE(3),
    SOMEWHAT_AGREE(4),
    STRONGLY_AGREE(5), ;

    companion object {
        fun valueOfNumber(number: Int): Answer {
            return try {
                entries.first { it.value == number }
            } catch (_: NoSuchElementException) {
                throw IllegalArgumentException("No Answer for number $number")
            }
        }
    }
}

data class CandidatesResponse(
    val pageProps: CandidatesResponsePageProps
)

data class CandidatesResponsePageProps(
    val themes: List<Theme>,
    val parties: List<Party>,
    val questions: List<Question>,
)

data class CandidateSearchResponse(
    val kind: String,
    val item: CandidateResponseItem,
    val success: Boolean,
)

data class CandidateResponseItem(
    val hits: List<Candidate>,
    val total: CandidateResponseItemTotal,
)

data class CandidateResponseItemTotal(
    val value: Int,
)

data class CandidateResponse(
    val pageProps: CandidateResponsePageProps,
)

data class CandidateResponsePageProps(
    val answers: List<CandidateAnswer>,
    val transformedCandidateInformation: TransformedCandidateInformation,
) {
    /**
     * Returns answers for the given candidate and questions.
     */
    fun answersForQuestion(questions: List<Question>): SortedMap<Question, Answer> =
        questions.associateWith { question ->
            val candidateQuestion =
                answers.flatMap { it.questions }.find { it.questionId == question.id } ?: CandidateQuestion.EMPTY
            Answer.valueOfNumber(candidateQuestion.answer)
        }.toSortedMap()
}

data class CandidateAnswer(
    val theme: Theme,
    val questions: List<CandidateQuestion>,
)

data class CandidateQuestion(
    val id: String,
    val candidateId: String,
    val questionId: Int,
    val text: String,
    val answer: Int,
    val explanation: String,
) {
    companion object {
        val EMPTY = CandidateQuestion("", "", 0, "", 0, "")
    }
}

data class TransformedCandidateInformation(
    val id: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val municipality: String,
    val party: String,
    val gender: String,
)

