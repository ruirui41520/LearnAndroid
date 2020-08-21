package com.intelligence.allcameratest.recyclerviewutil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BindingViewHolder<T: ViewDataBinding>: RecyclerView.ViewHolder {
    val mBinding: T

    constructor(mBinding: T) : super(mBinding.root) {
        this.mBinding = mBinding
    }
}