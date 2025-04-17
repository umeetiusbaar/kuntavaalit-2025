package fi.lempimetsa.kuntavaalit2025

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import io.github.oshai.kotlinlogging.KotlinLogging

enum class Source {
    LVS,
    PIRKKALAINEN,
    TAMPERELAINEN,
    AAMULEHTI,
    YLE,
    MTV,
    FACEBOOK,
}

enum class Municipality(val title: String) {
    LEMPAALA("Lempäälä"),
    PIRKKALA("Pirkkala"),
    TAMPERE("Tampere"),
}

enum class Party() {
    AP,
    EOP,
    HA,
    KD,
    KTPYL,
    KESK,
    KOK,
    KRIP,
    KRISTALL,
    LIB,
    LIIK,
    PS,
    RA,
    RAYL,
    RKP,
    SDP,
    SKP,
    SIT,
    VAL,
    VKK,
    VL,
    VAS,
    VIHR;

    companion object {
        fun valueOfShortName(shortName: String) =
            entries.firstOrNull { it.name.contains(shortName.replace(".", "").uppercase(), true) }
    }
}

data class Question(
    val source: Source,
    val id: Int,
    val title: String,
) : Comparable<Question> {
    override fun compareTo(other: Question): Int = compareValuesBy(this, other, { it.source }, { it.id })
}

data class Candidate(
    val firstName: String,
    val lastName: String,
    val number: Int,
    val party: Party,
    val elected: Boolean? = null,
    val substitutePlace: Boolean? = null,
) : Comparable<Candidate> {

    fun fullName() = fullName(firstName, lastName)

    override fun compareTo(other: Candidate): Int =
        compareValuesBy(this, other, { it.lastName.lowercase() }, { it.firstName.lowercase() })
}

data class Answer(
    val question: Question,
    val value: Int,
    val explanation: String?,
) {
    override fun toString(): String {
        return if (value > 0) "${value}${if (!explanation.isNullOrBlank()) " : ${explanation.replace("\n", " - ")}" else ""}" else ""
    }
}

private val logger = KotlinLogging.logger {}

/**
 * Candidate collection for retrieving the correct candidate even when the number is missing or the names do not quite match.
 */
object Candidates {
    private val candidatesByNumber: MutableMap<Municipality, MutableMap<Int, Candidate>> = mutableMapOf()
    private val candidatesByParty: MutableMap<Municipality, MutableMap<Party, MutableSet<Candidate>>> = mutableMapOf()
    private val candidatesByLastName: MutableMap<Municipality, MutableMap<String, MutableSet<Candidate>>> =
        mutableMapOf()
    private val candidatesByFirstName: MutableMap<Municipality, MutableMap<String, MutableSet<Candidate>>> =
        mutableMapOf()

    fun putIfAbsent(municipality: Municipality, candidate: Candidate): Candidate {
        val municipalityCandidatesByNumber: MutableMap<Int, Candidate> =
            candidatesByNumber.computeIfAbsent(municipality) { mutableMapOf() }
        val municipalityCandidatesByParty: MutableMap<Party, MutableSet<Candidate>> =
            candidatesByParty.computeIfAbsent(municipality) { mutableMapOf() }
        val municipalityCandidatesByLastName: MutableMap<String, MutableSet<Candidate>> =
            candidatesByLastName.computeIfAbsent(municipality) { mutableMapOf() }
        val municipalityCandidatesByFirstName: MutableMap<String, MutableSet<Candidate>> =
            candidatesByFirstName.computeIfAbsent(municipality) { mutableMapOf() }
        if (candidate.lastName == "Kuumola") logger.debug { "Candidates 1 : $candidatesByNumber"  }
        val candidateByNumber = municipalityCandidatesByNumber[candidate.number]
        if (candidateByNumber != null) {
            return candidateByNumber
        }
        if (candidate.number > 0) {
            municipalityCandidatesByNumber[candidate.number] = candidate
            municipalityCandidatesByParty.computeIfAbsent(candidate.party) { mutableSetOf() }.add(candidate)
            municipalityCandidatesByLastName.computeIfAbsent(candidate.lastName.lowercase()) { mutableSetOf() }
                .add(candidate)
            municipalityCandidatesByFirstName.computeIfAbsent(candidate.firstName.lowercase()) { mutableSetOf() }
                .add(candidate)
            if (candidate.lastName == "Kuumola") logger.debug { "Candidates 2 : $candidatesByNumber"  }
            return candidate
        }
        if (candidate.lastName == "Kuumola") logger.debug { "Candidates 3 : $candidatesByNumber"  }
        val candidatesByParty = municipalityCandidatesByParty.computeIfAbsent(candidate.party) { mutableSetOf() }
        if (candidatesByParty.isEmpty()) {
            candidatesByParty.add(candidate)
            municipalityCandidatesByLastName.computeIfAbsent(candidate.party.name.lowercase()) { mutableSetOf() }
                .add(candidate)
            municipalityCandidatesByFirstName.computeIfAbsent(candidate.party.name.lowercase()) { mutableSetOf() }
                .add(candidate)
            if (candidate.lastName == "Kuumola") logger.debug { "Candidates 4 : $candidatesByNumber"  }
            return candidate
        }
        if (candidate.lastName == "Kuumola") logger.debug { "Candidates 5 : $candidatesByNumber"  }
        val candidateByName =
            candidatesByParty.find { it.lastName.lowercase() == candidate.lastName.lowercase() && it.firstName.lowercase() == candidate.firstName.lowercase() }
        if (candidateByName != null) {
            return candidateByName
        }
        return candidate
    }

    fun clear() {
        candidatesByNumber.clear()
        candidatesByParty.clear()
        candidatesByLastName.clear()
        candidatesByFirstName.clear()
    }
}

enum class FacebookQuestionOption {
    PUSKIAISTEN_OIKAISU,
    KEHATIEN_JATKO,
    JARJESTELYRATAPIHA,
    RAUTATIETUNNELI,
    EI_MITAAN;

    fun toQuestion(): Question = when(this) {
        PUSKIAISTEN_OIKAISU -> Question(Source.FACEBOOK, 1, "Mikä tai mitkä maakuntakaavan suurhankkeista tulisi sinun mielestä toteuttaa Lempäälän ja Pirkkalan väliselle LemPi-metsäalueelle? 1. Puskiaisten oikaisu ja sen verran kaavoitus")
        KEHATIEN_JATKO -> Question(Source.FACEBOOK, 2, "Mikä tai mitkä maakuntakaavan suurhankkeista tulisi sinun mielestä toteuttaa Lempäälän ja Pirkkalan väliselle LemPi-metsäalueelle? 2. 2-kehätien jatko Sääksjärveltä lentoasemalle ja sen varren kaavoitus")
        JARJESTELYRATAPIHA -> Question(Source.FACEBOOK, 3, "Mikä tai mitkä maakuntakaavan suurhankkeista tulisi sinun mielestä toteuttaa Lempäälän ja Pirkkalan väliselle LemPi-metsäalueelle? 3. Järjestelyratapiha, oikorata, multimodaali logistiikkakeskittymä")
        RAUTATIETUNNELI -> Question(Source.FACEBOOK, 4, "Mikä tai mitkä maakuntakaavan suurhankkeista tulisi sinun mielestä toteuttaa Lempäälän ja Pirkkalan väliselle LemPi-metsäalueelle? 4. Rautatietunneli Peltolammilta lentoasemalle")
        EI_MITAAN -> Question(Source.FACEBOOK, 5, "Mikä tai mitkä maakuntakaavan suurhankkeista tulisi sinun mielestä toteuttaa Lempäälän ja Pirkkalan väliselle LemPi-metsäalueelle? 5. Ei mitään näistä")
    }
}

enum class Result {
    ELECTED,
    ON_SUBSTITUTE_PLACE,
    NOT_ELECTED;

    companion object {
        fun valueOfResults(elected: Boolean, substitutePlace: Boolean): Result {
            return when {
                elected -> ELECTED
                substitutePlace -> ON_SUBSTITUTE_PLACE
                else -> NOT_ELECTED
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            ELECTED -> "X"
            ON_SUBSTITUTE_PLACE -> ""
            NOT_ELECTED -> ""
        }
    }
}

@JacksonXmlRootElement(localName = "postitoimipaikat")
data class PostalCodes(
    @JacksonXmlProperty(localName = "toimipaikka")
    val postalCode: List<PostalCode>
)

data class PostalCode(
    @JacksonXmlProperty(localName = "nimi")
    val name: String,
    @JacksonXmlProperty(localName = "postinumero")
    val postalCode: String,
    @JacksonXmlProperty(localName = "lakkauttamispäivämäärä")
    val suspendDate: String?,
) {
    fun isSuspended() = !suspendDate.isNullOrBlank()
}
