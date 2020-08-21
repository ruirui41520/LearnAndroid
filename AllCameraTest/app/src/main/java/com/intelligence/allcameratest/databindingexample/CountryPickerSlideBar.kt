package com.intelligence.allcameratest.databindingexample

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import com.intelligence.allcameratest.R
import kotlin.math.max

class CountryPickerSlideBar @JvmOverloads constructor(
    context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val indexes = ArrayList<String>()
    var onLetterChangeListener: OnLetterChangeListener? = null
    private val paint: Paint
    private val textHeight: Float
    private var cellWidth = 0
    private var cellHeight = 0
    private var currentIndex = -1
    private val letterColor: Int
    private val selectColor: Int
    private var letterSize: Int
    private var topMarginOffset = 0

    fun addIndex(indexStr: String, position: Int) {
        indexes.add(position, indexStr)
        invalidate()
    }

    fun removeIndex(indexStr: String?) {
        indexes.remove(indexStr)
        invalidate()
    }

    fun updateIndexes(letters: ArrayList<String>) {
        indexes.clear()
        indexes.addAll(letters)
        invalidate()
    }

    fun setLetterSize(letterSize: Int) {
        if (this.letterSize == letterSize) return
        this.letterSize = letterSize
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellWidth = measuredWidth
        val validHeight = max((measuredHeight * 0.8).toInt(), minimumHeight)
        cellHeight = validHeight / indexes.size
        topMarginOffset = (measuredHeight - validHeight) / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.textSize = letterSize.toFloat()
        for (i in indexes.indices) {
            val letter = indexes[i].toUpperCase()
            val textWidth = paint.measureText(letter)
            val x = (cellWidth - textWidth) * 0.5f
            val y = (cellHeight + textHeight) * 0.5f + cellHeight * i + topMarginOffset
            if (i == currentIndex) {
                paint.color = selectColor
            } else {
                paint.color = letterColor
            }
            canvas.drawText(letter, x, y, paint)
        }
    }

    fun getLetter(position: Int): String {
        return if (position < 0 || position >= indexes.size) "" else indexes[position]
    }

    interface OnLetterChangeListener {
        fun onLetterChange(letter: String?)
        fun onReset()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val downY = event.y.toInt()
                currentIndex = (downY - topMarginOffset) / cellHeight
                if (currentIndex < 0 || currentIndex > indexes.size - 1) {
                } else {
                    onLetterChangeListener?.onLetterChange(indexes[currentIndex])
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val moveY = event.y.toInt()
                currentIndex = (moveY - topMarginOffset) / cellHeight
                if (currentIndex < 0 || currentIndex > indexes.size - 1) {
                } else {
                    onLetterChangeListener?.onLetterChange(indexes[currentIndex])
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                currentIndex = -1
                invalidate()
                onLetterChangeListener?.onReset()
            }
        }
        return true
    }

    init {
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.CountryPickerSlideBar,
            defStyleAttr,
            0
        )
        letterColor = ta.getColor(
            R.styleable.CountryPickerSlideBar_letterColor,
            context.resources.getColor(R.color.grey_02)
        )
        selectColor = ta.getColor(R.styleable.CountryPickerSlideBar_selectColor, Color.CYAN)
        letterSize = ta.getDimensionPixelSize(R.styleable.CountryPickerSlideBar_letterSize, 11)
        ta.recycle()
        paint = Paint()
        paint.isAntiAlias = true
        paint.typeface = Typeface.DEFAULT_BOLD
        val fontMetrics = paint.fontMetrics
        textHeight = Math.ceil(fontMetrics.descent - fontMetrics.ascent.toDouble()).toFloat()
        val letters = arrayOf(
            "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        )
        indexes.addAll(listOf(*letters))
    }

}