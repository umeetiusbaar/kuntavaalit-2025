package fi.lempimetsa.kuntavaalit2025

fun med(list: List<Int>) = list.sorted().let {
    val size = it.size
    if (size % 2 == 0) {
        (it[size / 2 - 1] + it[size / 2]) / 2
    } else {
        it[size / 2]
    }
}

fun fullName(firstName: String, lastName: String) : String {
    var formattedFirstName =
        firstName.trim().split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { it.uppercaseChar() } }
    formattedFirstName =
        formattedFirstName.split("-").joinToString("-") { it.trim().replaceFirstChar { it.uppercaseChar() } }
    var formattedLastName =
        lastName.trim().split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { it.uppercaseChar() } }
    formattedLastName =
        formattedLastName.split("-").joinToString("-") { it.trim().replaceFirstChar { it.uppercaseChar() } }
    return "$formattedFirstName $formattedLastName"
}
