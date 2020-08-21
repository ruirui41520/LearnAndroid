package com.intelligence.allcameratest.databindingexample

import android.database.Observable
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class CountryPickerViewModel : ViewModel(), CountryPickerPresenter,
    CountryPickerSlideBar.OnLetterChangeListener {
    private val _allCountriesFromSystem = MutableLiveData<ArrayList<Country>>()
    private val allCountriesFromSystem: LiveData<ArrayList<Country>>
        get() = _allCountriesFromSystem

    private val _selectedCountry = MutableLiveData<String>()
    val selectedCountry: LiveData<String>
        get() = _selectedCountry

    private val _selectedPosition = MutableLiveData<Int>()
    val selectedPosition: LiveData<Int>
        get() = _selectedPosition

    var letterField = ObservableField<String?>()

    init {
        Country.getAll(null)?.let {
            _allCountriesFromSystem.value = it
        }
    }

    // todo pickerItems must be observed
    val pickerItems: LiveData<ArrayList<CountryPickerCountryItem>> =
        Transformations.map(_allCountriesFromSystem) {
            val pickerItems = ArrayList<CountryPickerCountryItem>()
            it.forEach {
                Log.e("test","country = " + it.name)
                val pickItem = CountryPickerCountryItem()
                pickItem.countryInfo.set(it)
                pickerItems.add(pickItem)
            }
            pickerItems
        }

    // todo entityList must be observed
    val entityList: LiveData<ArrayList<String>> = Transformations.map(_allCountriesFromSystem) {
        val entities = ArrayList<String>()
        CountryPickerHelper.sortCountryListWithLetter(it)?.let {
            entities.addAll(it)
        }
        entities
    }

    // add select country event
    override fun onCountryItemSelectEvent(countryItem: CountryPickerCountryItem?) {
        countryItem?.countryInfo?.get()?.name?.let {
            _selectedCountry.value = it
        }
    }

    override fun onLetterChange(letter: String?) {
        letter?.let {
            letterField.set(it)
            _selectedPosition.value = entityList.value?.indexOf(letter)
        }
    }

    override fun onReset() {
        letterField.set(null)
    }

}