package fi.lempimetsa.kuntavaalit2025

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class PostalCodeService {

    private val xmlMapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    }).registerKotlinModule()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    private val postalCodes = mutableMapOf<String, String>()

    init {
        val postalCodes = javaClass.getResourceAsStream("/postitoimipaikat.xml")
            .use { xmlMapper.readValue(it, PostalCodes::class.java) }
        postalCodes.postalCode.forEach { postalCode ->
            if (!postalCode.isSuspended()) {
                this.postalCodes[postalCode.postalCode] = postalCode.name
            }
        }
    }

    fun postOffice(postalCode: String): String {
        return postalCodes[postalCode] ?: ""
    }
}