package com.success.examprep.data.model

/**
 * Standard multiple choice option letters.
 */
enum class OptionLetter(val index: Int) {
    A(0),
    B(1),
    C(2),
    D(3);

    companion object {
        fun fromIndex(index: Int): OptionLetter {
            return entries.firstOrNull { it.index == index } ?: A
        }

        fun fromString(letter: String?): OptionLetter {
            return entries.firstOrNull { it.name.equals(letter, ignoreCase = true) } ?: A
        }
    }
}
