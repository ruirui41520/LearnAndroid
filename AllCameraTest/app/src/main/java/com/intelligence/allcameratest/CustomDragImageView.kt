package com.intelligence.allcameratest

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup


class CustomDragImageView : androidx.appcompat.widget.AppCompatImageView {
    // 点击事件区分
    var isDrag = false

    var viewWidth = 0
    var viewHeight = 0
    var screenWidth = 0
    var screenHeight = 0
    var startDownX = 0f
    var startDownY = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)

    init {
        screenHeight = UiUtil.getMaxHeight(context) - UiUtil.getStatusBarHeight(context)
        screenWidth = UiUtil.getMaxWidth(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = measuredWidth
        viewHeight = measuredHeight
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (this.isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDrag = false
                    startDownX = event.x // 点击触屏时的x坐标 用于离开屏幕时的x坐标作计算
                    startDownY = event.y // 点击触屏时的y坐标 用于离开屏幕时的y坐标作计算
                }
                MotionEvent.ACTION_MOVE -> {
                    val moveX = event.x - startDownX
                    val moveY = event.y - startDownY
                    var l: Int
                    var r: Int
                    var t: Int
                    var b: Int// 上下左右四点移动后的偏移量
                    if (Math.abs(moveX) > 5 || Math.abs(moveY) > 5) { // 偏移量的绝对值大于 5 为 滑动时间 并根据偏移量计算四点移动后的位置
                        l = (left + moveX).toInt()
                        r = l + width
                        t = (top + moveY).toInt()
                        b = t + height
                        if (l < 0) { // left 小于 0 就是滑出边界 赋值为 0 ; right 右边的坐标就是自身宽度 如果可以划出边界 left right top bottom 最小值的绝对值 不能大于自身的宽高
                            l = 0
                            r = l + width
                        } else if (r > screenWidth) { // 判断 right 并赋值
                            r = screenWidth
                            l = r - width
                        }
                        if (t < 0) { // top
                            t = 0
                            b = t + height
                        } else if (b > screenHeight) { // bottom
                            b = screenHeight
                            t = b - height
                        }
                        layout(l, t, r, b) // 重置view在layout 中位置
                        isDrag = true // 重置 拖动为 true
                    } else {
                        isDrag = false // 小于峰值5时 为点击事件
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPressed = false
                    // 解决刷新重回原点问题
                    try {
                        // linear/relative 为父布局
                        val lp = layoutParams as ViewGroup.MarginLayoutParams
                        lp.setMargins(left, top, 0, 0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return true
        }
        return false
    }
}