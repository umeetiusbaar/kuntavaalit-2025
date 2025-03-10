package fi.lempimetsa.kuntavaalit2025

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

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
