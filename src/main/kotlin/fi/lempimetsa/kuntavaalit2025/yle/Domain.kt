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
    val answers: Map<String, Answer>?,
    val mediapartnerAnswers: Map<String, Answer>?,
) : Comparable<Candidate> {

    fun fullName() = fi.lempimetsa.kuntavaalit2025.fullName(firstName, lastName)

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
): Comparable<Question> {
    override fun compareTo(other: Question): Int = compareValuesBy(this, other) { it.id }
}

data class Answer(
    val answer: Int,
) {
    companion object {
        val EMPTY = Answer(0)
    }

    fun agree() = answer > 3
    fun disagree() = answer < 3
}
