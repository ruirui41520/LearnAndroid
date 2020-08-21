package com.intelligence.allcameratest.databindingexample

import androidx.databinding.ObservableField
import com.intelligence.allcameratest.CameraTestApplication
import java.util.*
import kotlin.collections.ArrayList

data class Country(val name: String?, val sortKey: String?, val locale: String, val flag: Int) : CountryName {
    val selectedCountry = ObservableField<Boolean>()

    override fun toString(): String {
        return "Country{" +
                "flag='" + flag + '\'' +
                ", name='" + name + '\'' +
                '}'
    }

    companion object {
        private var countries: ArrayList<Country>? = null
        fun getAll(notIncludedCountryCodeList: List<String>?): ArrayList<Country>? {
            countries = getAvailableCountryList(notIncludedCountryCodeList)
            return countries
        }

        private fun getAvailableCountryList(notIncludedCountryCodeList: List<String>?): ArrayList<Country>? {
            val countryList = ArrayList<Country>()
            val selectedCountries = HashSet<Country>()
            val availableLocales: Array<String> = Locale.getISOCountries()
            availableLocales.forEach {
                if (notIncludedCountryCodeList?.contains(it) == true) return@forEach
                val locale = Locale("", it)
                val flag = CameraTestApplication.getInstance()!!.resources.getIdentifier("flag_" + locale.country.toLowerCase(), "drawable", CameraTestApplication.getInstance()!!.packageName)
                val displayCountry = locale.getDisplayCountry(Locale.ENGLISH)
                selectedCountries.add(Country(displayCountry, convertToSortKey(displayCountry), locale.country, flag))
            }
            countryList.addAll(selectedCountries)
            return countryList
        }

        fun convertToSortKey(displayName: String): String? {
            return if (displayName.startsWith("Å")) displayName.replaceFirst("Å", "A") else displayName
        }
    }

    fun destroy() {
        countries = null
    }

    override fun getEnName(): String? {
        return name
    }

    override fun getSortName(): String? {
        return sortKey
    }

    override fun selectedItem(): Boolean {
        return selectedCountry.get() ?: false
    }
}