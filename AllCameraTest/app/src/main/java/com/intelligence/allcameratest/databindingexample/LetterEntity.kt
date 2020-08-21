package com.intelligence.allcameratest.databindingexample

class LetterEntity(val letter: String) : CountryName {
    override fun getEnName(): String {
        return letter.toLowerCase()
    }

    override fun getSortName(): String? {
        return letter
    }

    override fun selectedItem(): Boolean {
        return false
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as LetterEntity
        return letter.toLowerCase() == that.letter.toLowerCase()
    }

    override fun hashCode(): Int {
        return letter.toLowerCase().hashCode()
    }
}