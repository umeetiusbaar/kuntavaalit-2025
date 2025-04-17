package fi.lempimetsa.kuntavaalit2025.yle

import com.google.gson.annotations.SerializedName

data class Constituency(
    val id: Int,
    @SerializedName("name_fi")
    val name: String,
    @SerializedName("official_id")
    val officialId: String,
) {
    companion object {
        val LEMPAALA = Constituency(114, "Lempäälä", "418")
        val PIRKKALA = Constituency(301, "Pirkkala", "604")
        val TAMPERE = Constituency(228, "Tampere", "837")
        val CONSTITUENCIES = listOf(LEMPAALA, PIRKKALA, TAMPERE)

        fun valueOfName(name: String) = CONSTITUENCIES.first { it.name.equals(name, true) }
    }
}

data class Candidate(
    val id: Int,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("party_id")
    val partyId: Int,
    val party: Party,
    @SerializedName("election_number")
    val electionNumber: Int,
    val answers: Map<String, Answer>?,
    val mediapartnerAnswers: Map<String, Answer>?,
) : Comparable<Candidate> {
    override fun compareTo(other: Candidate): Int = compareValuesBy(this, other, { it.lastName }, { it.firstName })
}

data class Party(
    val id: Int,
    @SerializedName("short_name_fi")
    val shortName: String,
) : Comparable<Party> {

    companion object {
        val EMPTY = Party(0, "")
    }

    override fun compareTo(other: Party): Int = compareValuesBy(this, other) { it.shortName }
}

data class Category(
    val id: Int,
    val questions: List<Question>,
)

data class Question(
    val id: Int,
    @SerializedName("text_fi")
    val text: String,
) : Comparable<Question> {
    override fun compareTo(other: Question): Int = compareValuesBy(this, other) { it.id }
}

data class Answer(
    val answer: Int,
    @SerializedName("explanation_fi")
    val explanationFi: String?,
    @SerializedName("explanation_sv")
    val explanationSv: String?,
    @SerializedName("explanation_en")
    val explanationEn: String?,
) {
    companion object {
        val EMPTY = Answer(0, null, null, null)
    }

    fun explanation() = explanationFi ?: explanationSv ?: explanationEn
}

data class CandidateResultsResponse(
    val data: CandidateResultsResponseData,
)

data class CandidateResultsResponseData(
    val candidateResults: List<CandidateResult>
)

data class CandidateResult(
    val caid: Int,
    val electedInformation: ElectedInformation,
)

enum class ElectedInformation(val isElected: Boolean, val isOnSubstitutePlace: Boolean) {
    ELECTED(true, false),
    ON_SUBSTITUTE_PLACE(false, true),
    NOT_ELECTED(false, false);
}
