package com.intelligence.allcameratest

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class CustomViewGroup : ViewGroup {
    private var maxWidth = 0
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewGroup)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var marginStart = 0
        var totalMarginTop = 0
        var marginEnd = 0
        var totalMarginBottom = 0
        var groupWidth = 0
        var groupHeight = 0
        var totalHeight = 0
        if (childCount == 0) setMeasuredDimension(0, 0)
        else {
            // viewGroup的宽 = view宽+ viewGroup的（paddingStart+ paddingEnd） + view的（marginStart + marginEnd）
            // viewGroup的高 = view高 + viewGroup的（paddingTop + paddingBottom） + view的（marginTop + marginBottom)
            (0 until childCount).asSequence().forEach {
                val view = getChildAt(it)
                val marginLayoutParams = view.layoutParams as MarginLayoutParams
                measureChild(view, widthMeasureSpec, heightMeasureSpec)
                maxWidth = Math.max(maxWidth, view.measuredWidth)
                marginStart = Math.max(marginStart, marginLayoutParams.marginStart)
                totalMarginTop += marginLayoutParams.topMargin
                marginEnd = Math.max(marginEnd, marginLayoutParams.marginEnd)
                totalMarginBottom += marginLayoutParams.bottomMargin
                totalHeight += view.measuredHeight
            }
            groupWidth = maxWidth + paddingStart + paddingEnd + marginStart + marginEnd
            groupHeight = totalHeight + paddingTop + paddingBottom + totalMarginTop + totalMarginBottom
            setMeasuredDimension(measureWidthAccord(widthMeasureSpec, groupWidth), measureHeightAccord(heightMeasureSpec, groupHeight))
        }

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //ViewGroup的onLayout四个参数是其父View传过来的，表示当前View相对于父View的相对位置，
        //子View的layout的参数应该是子View相对于当前ViewGroup的位置，最起码要处理当前View的边距
        if (!changed)return
        var currentHeight = paddingTop
        (0 until childCount).asSequence().forEach {
            val view = getChildAt(it)
            val marginLayoutParams = view.layoutParams as MarginLayoutParams
            // view 安装的位置：（left: group的paddingStart + view的marginStart，top: group的paddingTop + view的marginTop,right: group的paddingStart + view.measuredWidth
            // + view.marginStart,bottom: group的paddingStart + view的height +view.marginTop）
            view.layout(
                paddingStart + marginLayoutParams.marginStart,
                currentHeight + marginLayoutParams.topMargin,
                paddingStart + marginLayoutParams.marginStart + view.measuredWidth,
                currentHeight + marginLayoutParams.topMargin +view.measuredHeight
            )
            currentHeight += (view.measuredHeight + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin)
        }

    }

    private fun measureWidthAccord(measureSpec: Int, groupWidth: Int): Int {
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.AT_MOST -> Math.min(MeasureSpec.getSize(measureSpec), groupWidth)
            else -> MeasureSpec.getSize(measureSpec)
        }
    }

    private fun measureHeightAccord(measureSpec: Int, groupHeight: Int): Int {
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.AT_MOST -> Math.min(MeasureSpec.getSize(measureSpec), groupHeight)
            else -> MeasureSpec.getSize(measureSpec)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }
}