package com.intelligence.allcameratest.databindingexample

import com.intelligence.allcameratest.recyclerviewutil.BaseDataBindingAdapter

interface CountryPickerPresenter:BaseDataBindingAdapter.Presenter {
    fun onCountryItemSelectEvent(countryItem: CountryPickerCountryItem?)
}