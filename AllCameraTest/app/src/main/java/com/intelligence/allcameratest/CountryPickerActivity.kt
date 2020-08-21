package com.intelligence.allcameratest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intelligence.allcameratest.databinding.ActivityCountryPickerBinding
import com.intelligence.allcameratest.databindingexample.CountryPickerViewModel
import com.intelligence.allcameratest.recyclerviewutil.BaseDataBindingAdapter

class CountryPickerActivity : AppCompatActivity() {
    private val viewModel: CountryPickerViewModel by lazy {
        this.let { ViewModelProvider(it, MyViewModelFactory(null)).get(CountryPickerViewModel::class.java) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding: ActivityCountryPickerBinding? =
            DataBindingUtil.setContentView(this, R.layout.activity_country_picker)
        val targetAdapter = BaseDataBindingAdapter(this)
        targetAdapter.setPresenter(viewModel)
        val manager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        dataBinding?.let {
            it.countryPickerViewModel = viewModel
            it.adapter = targetAdapter
            it.manager = manager
            it.countrySlideBar.onLetterChangeListener = viewModel
        }
        // fragment lifecycleOwner
        viewModel.pickerItems.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                targetAdapter.addAll(it)
            }
        })
        viewModel.selectedCountry.observe(this, Observer {
            Log.e("test", "selectedCountry : $it")
        })
        viewModel.entityList.observe(this, Observer {
            dataBinding?.countrySlideBar?.updateIndexes(it)
        })
        viewModel.selectedPosition.observe(this, Observer {
            if (it != -1) {
                manager.scrollToPositionWithOffset(it, 0)
            } else {
                manager.scrollToPosition(0)
            }
        })

    }

    inner class MyViewModelFactory(parameter: String?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CountryPickerViewModel() as T
        }
    }
}