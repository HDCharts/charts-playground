package codegen.common

fun escapeKotlinString(value: String): String =
    buildString {
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> append("\\$")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else if (character.code < 0x20) -> {
                    append("\\u")
                    append(
                        character.code
                            .toString(16)
                            .uppercase()
                            .padStart(4, '0'),
                    )
                }
                else -> append(character)
            }
        }
    }
