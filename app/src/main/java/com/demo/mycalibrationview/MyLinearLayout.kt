package com.demo.mycalibrationview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.ScrollView

class MyLinearLayout:LinearLayout {

    private var parentScrollview: ScrollView? = null

    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setParentScrollview(parentScrollview: ScrollView) {
        this.parentScrollview = parentScrollview
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        parentScrollview?.requestDisallowInterceptTouchEvent(true)
        return super.onInterceptTouchEvent(ev)
    }

}