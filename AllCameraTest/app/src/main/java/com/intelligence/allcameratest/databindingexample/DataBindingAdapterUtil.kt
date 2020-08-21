package com.intelligence.allcameratest.databindingexample

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter

object DataBindingAdapterUtil {

    @JvmStatic
    @BindingAdapter("textFormat", "textParam")
    fun setFormatText(view: TextView?, textFormat: String?, textParam: String?) {
        if (view == null) {
            return
        }
        view.text = String.format(textFormat ?: "", textParam)
    }

    @JvmStatic
    @BindingAdapter("iconSrc")
    fun setIconDrawable(imageView: ImageView, iconSrc: Int?) {
        if (iconSrc != null && iconSrc != 0) {
            imageView.setImageDrawable(imageView.context.getDrawable(iconSrc))
        }
    }
}