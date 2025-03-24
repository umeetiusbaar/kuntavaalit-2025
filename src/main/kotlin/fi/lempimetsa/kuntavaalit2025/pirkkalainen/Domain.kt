package fi.lempimetsa.kuntavaalit2025.pirkkalainen

import com.google.gson.annotations.SerializedName

enum class Election(val id: Int, val title: String) {
    PIRKKALA(51, "Pirkkala");

    companion object {
        fun valueOfName(name: String) = entries.firstOrNull { it.title.equals(name, true) }
    }
}

data class Candidate(
    val id: Int,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("candidate_number")
    val candidateNumber: Int,
    val party: Party,
    val answers: List<Answer>,
) : Comparable<Candidate> {

    override fun compareTo(other: Candidate): Int =
        compareValuesBy(this, other, { it.lastName.lowercase() }, { it.firstName.lowercase() })
}

data class Party(
    val id: Int,
    val name: String,
    @SerializedName("short_name")
    val shortName: String,
) : Comparable<Party> {

    override fun compareTo(other: Party): Int = compareValuesBy(this, other) { it.shortName }
}

data class Answer(
    val id: Int,
    @SerializedName("question_id")
    val questionId: Int,
    val answer: Int,
    @SerializedName("candidate_answer_detail")
    val explanation: String,
) {
    companion object {
        val EMPTY = Answer(0, 0, 0, "")
    }
}

data class QuestionsResponse(
    val data: List<Question>,
)

data class Question(
    val id: Int,
    val title: String,
) : Comparable<Question> {
    override fun compareTo(other: Question): Int = compareValuesBy(this, other) { it.id }
}
