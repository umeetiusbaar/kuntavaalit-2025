package fi.lempimetsa.kuntavaalit2025

fun List<Int>?.avg() = this?.average() ?: 0.0

fun fullName(firstName: String, lastName: String): String = "$firstName $lastName"

fun String.adjustFirstName(municipality: Municipality, lastName: String): String {
    return when {
        (this.trim() == "EelisSebastian" || this.trim() == "Hoffrén") -> "Eelis Sebastian"
        (this.trim() == "Rauno (Rane)") -> "Rauno"
        (municipality == Municipality.TAMPERE && lastName.trim() == "Blom" && this.trim() == "Marko") -> "Petteri"
        (municipality == Municipality.TAMPERE && lastName.trim() == "Laitinen" && this.trim() == "Kari") -> "Kimmo"
        (municipality == Municipality.TAMPERE && lastName.trim() == "Lohiniemi" && this.trim() == "Terhi") -> "Susanna"
        (municipality == Municipality.TAMPERE && lastName.trim() == "Nyman" && this.trim() == "Maria") -> "Elina"
        (municipality == Municipality.TAMPERE && lastName.trim() == "Sirkesalo" && this.trim() == "Johanna") -> "Sohvi"
        (municipality == Municipality.TAMPERE && lastName.trim() == "Osinnaike") -> "Samuel"
        else ->
            this.trim().replace(".", "").split(" ")
                .joinToString(" ") { it.lowercase().replaceFirstChar { it.uppercaseChar() } }.split("-")
                .joinToString("-") { it.trim().replaceFirstChar { it.uppercaseChar() } }
    }
}

fun String.adjustLastName(municipality: Municipality, party: Party, firstName: String): String {
    return when {
        this.trim() == "Eelis sebastian" -> "Hoffrén"
        municipality == Municipality.TAMPERE && party == Party.VIHR && this == "Silven" && firstName == "Paula" -> "Silvén"
        else ->
            this.trim().split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { it.uppercaseChar() } }
                .split("-")
                .joinToString("-") { it.trim().replaceFirstChar { it.uppercaseChar() } }
    }
}

fun Int.adjustNumber(municipality: Municipality, firstName: String, lastName: String): Int {
    return when {
        municipality == Municipality.LEMPAALA && firstName == "Ari" && lastName == "Niemelä" -> 43
        municipality == Municipality.TAMPERE && firstName == "Seppo" && lastName == "Lehto" -> 573
        else -> this
    }
}
