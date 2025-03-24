package fi.lempimetsa.kuntavaalit2025.mtv

import com.google.gson.annotations.SerializedName

enum class Election(val id: Int) {
    KUNTAVAALIT(101)
}

enum class District(val id: Int, val title: String) {
    LEMPAALA(185, "Lempäälä"),
    PIRKKALA(389, "Pirkkala"),
    TAMPERE(348, "Tampere");

    companion object {
        fun valueOfName(name: String) = entries.firstOrNull { it.title.equals(name, true) }
    }
}

data class DistrictResponse(
    val parties: List<Party>,
    val candidates: List<Candidate>,
    val questionCategories: List<QuestionCategory>,
    val questions: List<Question>,
)

data class Party(
    val id: Int,
    @SerializedName("election_id")
    val electionId: Int,
    val name: Map<String, String>,
    @SerializedName("short_name")
    val shortName: Map<String, String>,
) : Comparable<Party> {
    fun name() = name["fi"] ?: name["sv"] ?: name["en"] ?: "Unknown"
    fun shortName() = shortName["fi"] ?: shortName["sv"] ?: shortName["en"] ?: "Unknown"

    override fun compareTo(other: Party): Int = compareValuesBy(this, other) { it.shortName() }

    companion object {
        val EMPTY = Party(0, 0, mapOf(), mapOf())
    }
}

data class CandidateResponse(
    val candidate: Candidate,
)

data class Candidate(
    val id: Int,
    @SerializedName("election_id")
    val electionId: Int,
    @SerializedName("party_id")
    val partyId: Int,
    val party: Party,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("election_number")
    val electionNumber: Int,
    val answers: List<Answer>,
) : Comparable<Candidate> {

    override fun compareTo(other: Candidate): Int =
        compareValuesBy(this, other, { it.lastName.lowercase() }, { it.firstName.lowercase() })
}

data class QuestionCategory(
    val id: Int,
    @SerializedName("election_id")
    val electionId: Int,
    val name: Map<String, String>,
) {
    fun name() = name["fi"] ?: name["sv"] ?: name["en"] ?: "Unknown"
}

data class Question(
    val id: Int,
    @SerializedName("election_id")
    val electionId: Int,
    @SerializedName("category_id")
    val categoryId: Int,
    val text: Map<String, String>,
) : Comparable<Question> {
    fun text() = text["fi"] ?: text["sv"] ?: text["en"] ?: "Unknown"

    override fun compareTo(other: Question): Int = compareValuesBy(this, other) { it.id }
}

data class Answer(
    @SerializedName("question_id")
    val questionId: Int,
    val answer: Int,
    val reasoning: Map<String, String>,
) {
    companion object {
        val EMPTY = Answer(0, 0, emptyMap())
    }

    fun explanation() = reasoning["fi"] ?: reasoning["sv"] ?: reasoning["en"]
}
