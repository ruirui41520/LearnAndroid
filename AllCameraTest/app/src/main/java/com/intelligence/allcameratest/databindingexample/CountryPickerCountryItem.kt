package com.intelligence.allcameratest.databindingexample

import androidx.databinding.ObservableField
import com.intelligence.allcameratest.R
import com.intelligence.allcameratest.recyclerviewutil.BaseDataBinding
import androidx.databinding.library.baseAdapters.BR

class CountryPickerCountryItem: BaseDataBinding {
    val countryInfo = ObservableField<Country>()

    override fun getLayoutId(): Int {
        return R.layout.country_pick_base_item
    }

    override fun getBaseDataItemBrId(): Int {
        return BR.countryPickerCountryItem
    }

    override fun getPresenterId(): Int {
        return BR.countryPickerPresenter
    }
}