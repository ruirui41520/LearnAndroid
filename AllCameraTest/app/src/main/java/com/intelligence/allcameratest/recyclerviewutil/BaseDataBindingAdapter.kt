package com.intelligence.allcameratest.recyclerviewutil

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BaseDataBindingAdapter : RecyclerView.Adapter<BindingViewHolder<ViewDataBinding>> {
    interface Presenter {}

    private val mLayoutInflater: LayoutInflater
    private var mCollection: ArrayList<BaseDataBinding>? = null
    private var mPresenter: Presenter? = null

    constructor(context: Context) {
        mCollection = ArrayList()
        mLayoutInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<ViewDataBinding> {
        return BindingViewHolder(DataBindingUtil.inflate(mLayoutInflater, viewType, parent, false))
    }

    override fun onBindViewHolder(holder: BindingViewHolder<ViewDataBinding>, position: Int) {
        mCollection?.let {
            val item = it[position]
                holder.mBinding.setVariable(item.getBaseDataItemBrId(),item)
                holder.mBinding.setVariable(item.getPresenterId(),mPresenter)
                holder.mBinding.executePendingBindings()
            }

    }

    override fun getItemCount(): Int {
        return mCollection?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return mCollection?.let { it[position].getLayoutId() } ?: -1
    }

    fun remove(position: Int) {
        mCollection?.let {
            it.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    fun remove(viewModel: BaseDataBinding) {
        mCollection?.let {
            remove(it.indexOf(viewModel))
        }

    }

    fun clear() {
        mCollection?.let {
            it.clear()
            notifyDataSetChanged()
        }
    }

    fun setPresenter(presenter: Presenter) {
        mPresenter = presenter
    }

    fun add(viewModel: BaseDataBinding) {
        mCollection?.let {
            it.add(viewModel)
            notifyDataSetChanged()
        }
    }

    fun add(position: Int, viewModel: BaseDataBinding) {
        mCollection?.let {
            it.add(position, viewModel)
            notifyDataSetChanged()
        }
    }

    fun set(viewModels: List<BaseDataBinding>) {
        mCollection?.let {
            it.clear()
            addAll(viewModels)
        }
    }

    fun move(fromPosition: Int, toPosition: Int) {
        mCollection?.let {
            if (fromPosition > toPosition) {
                it.add(toPosition, it.removeAt(fromPosition))
            } else {
                it.add(toPosition - 1, it.removeAt(fromPosition))
            }

        }

    }

    fun addList(viewModels: List<BaseDataBinding>?) {
        mCollection?.let {
            it.clear()
            if (viewModels != null){
                addAll(viewModels)
            }
        }
    }

    fun addAll(viewModels: List<BaseDataBinding>?) {
        mCollection?.let {
            if (viewModels != null){
                it.addAll(viewModels)
            }
            notifyDataSetChanged()
        }
    }

    fun getCollection(): ArrayList<BaseDataBinding>? {
        return mCollection
    }

    fun getItemSize(): Int? {
        return mCollection?.size
    }

    fun getPresenter(): Presenter? {
        return mPresenter
    }
}