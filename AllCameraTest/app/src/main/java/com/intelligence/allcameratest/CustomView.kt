package com.intelligence.allcameratest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomView : View {
    var defaultSize = 100

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CustomView)
        defaultSize = typedArray.getDimensionPixelSize(R.styleable.CustomView_default_size, 100)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = getBestSize(defaultSize, widthMeasureSpec)
        var height = getBestSize(defaultSize, heightMeasureSpec)
        if (width > height)
            width = height
        else
            height = width
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //相对于画布位置，左上角坐标（0，0）
        val radius = measuredHeight.toFloat() / 2 - (paddingStart + paddingEnd) / 2
        val center = measuredHeight.toFloat() / 2
        val paint = Paint()
        paint.color = Color.GREEN
        canvas?.drawCircle(center, center, radius, paint)
    }

    fun getBestSize(defaultSize: Int, measureSpec: Int): Int {
        // 默认100,宽高相等
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        var finalSize = 0
        when (mode) {
            // 无定义,多大都可以,取默认
            MeasureSpec.UNSPECIFIED -> finalSize = defaultSize
            // 最大不超过size（wrap）
            MeasureSpec.AT_MOST -> finalSize = size
            // 系统认为应该取size（match/具体大小时）
            MeasureSpec.EXACTLY -> finalSize = size
        }
        return finalSize
    }
}