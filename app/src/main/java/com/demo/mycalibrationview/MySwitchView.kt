package com.demo.mycalibrationview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.Toast

class MySwitchView : LinearLayout {

    private var mContext: Context? = null

    //椭圆背景画笔
    private var bgPaint: Paint? = null
    //滑动控件中的 圆 的画笔
    private var circlePaint: Paint? = null

    private var defaultOpenColor: Int = Color.parseColor("#4A79FD")
    private var defaultCloseColor: Int = Color.parseColor("#dddddd")
    private var defaultCircleColor: Int = Color.WHITE

    private var bgOpenColor: Int = defaultOpenColor
    private var bgCloseColor: Int = defaultCloseColor
    private var circleColor: Int = defaultCircleColor

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var center: Float = 0f

    //圆点半径
    private var radius: Float = 0f

    //手指抬起后，小圆是否在自己滑动。如果是，就要先停止触摸事件
    private var isAnimation: Boolean = false

    //是否允许触摸。某些条件下，将其改为false，可以屏蔽触摸
    private var isCanTouch: Boolean = true

    //屏蔽触摸后，用于提示的内容。 可有可无
    private var tipString = ""

    //小圆圆心当前的X轴坐标
    private var currentX: Float = 0f

    private val TO_CLOSE: Int = 100
    private val TO_OPEN: Int = 200

    private val TO_SHOW: Int = 300

    private var isOpen: Boolean = true

    private var leftLimitValue: Float = 0f
    private var rightLimitValue: Float = 0f

    private var offsetValue: Float = 0f

    private var mHandler = object : Handler() {

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            when (msg?.what) {

                TO_CLOSE -> {

                    if (currentX > leftLimitValue) {
                        currentX -= 5
                        isAnimation = true
                        sendEmptyMessage(TO_CLOSE)

                    } else {
                        currentX = leftLimitValue
                        isAnimation = false
                        removeMessages(TO_CLOSE)
                    }
                }

                TO_OPEN -> {

                    if (currentX < rightLimitValue) {
                        currentX += 5
                        isAnimation = true
                        sendEmptyMessage(TO_OPEN)
                    } else {
                        currentX = rightLimitValue
                        isAnimation = false
                        removeMessages(TO_OPEN)
                    }

                }

                TO_SHOW -> {
                    if (isOpen) {
                        bgPaint?.color = bgOpenColor
                        currentX = rightLimitValue
                    } else {
                        bgPaint?.color = bgCloseColor
                        currentX = leftLimitValue
                    }
                    removeMessages(TO_SHOW)
                }

            }

            invalidate()
        }
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        setBackgroundColor(Color.TRANSPARENT)

        mContext = context

        val ta = context?.obtainStyledAttributes(attrs, R.styleable.MySwitchView)
        bgOpenColor = ta?.getColor(R.styleable.MySwitchView_bgOpenColor, defaultOpenColor)
            ?: defaultOpenColor
        bgCloseColor = ta?.getColor(R.styleable.MySwitchView_bgCloseColor, defaultCloseColor)
            ?: defaultCloseColor
        circleColor = ta?.getColor(R.styleable.MySwitchView_circleColor, defaultCircleColor)
            ?: defaultCircleColor

        ta?.recycle()

        init()

    }


    private fun init() {

        //背景画笔
        bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint?.strokeWidth = 20f
        bgPaint?.style = Paint.Style.FILL
        bgPaint?.color = bgOpenColor

        //小圆画笔
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint?.strokeWidth = 20f
        circlePaint?.style = Paint.Style.FILL
        circlePaint?.color = circleColor

    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        super.onLayout(p0, p1, p2, p3, p4)

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        ev ?: return super.onInterceptTouchEvent(ev)

        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            // 将父控件的滚动事件拦截
            requestDisallowInterceptTouchEvent(true)
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            // 把滚动事件恢复给父控件
            requestDisallowInterceptTouchEvent(false)
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewWidth = w
        viewHeight = h

        offsetValue = viewHeight / 10f

        radius = (viewHeight - offsetValue * 2) / 2f

        leftLimitValue = offsetValue + radius

        rightLimitValue = viewWidth - offsetValue - radius

        currentX = rightLimitValue

        center = viewWidth / 2f

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)



        if (bgPaint != null && circlePaint != null) {

            canvas?.drawRoundRect(
                0f,
                0f,
                viewWidth.toFloat(),
                viewHeight.toFloat(),
                viewHeight / 2f,
                viewHeight / 2f,
                bgPaint!!
            )

            canvas?.drawCircle(currentX, viewHeight / 2f, radius, circlePaint!!)

        }

    }

    private var downX: Float = 0f
    //isScroll = true 表示发生了滑动
    private var isScroll: Boolean = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event == null || isAnimation) {
            return super.onTouchEvent(event)
        }

        if (!isCanTouch) {

            mySvOnClickListener?.onClick(isOpen)

            if (tipString.isEmpty().not()) {
                Toast.makeText(mContext!!, "$tipString", Toast.LENGTH_SHORT).show()
            }

            return super.onTouchEvent(event)
        }

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                downX = event.x

            }

            MotionEvent.ACTION_MOVE -> {

                currentX = event.x

                isScroll = Math.abs(currentX - downX) > 5

                if (isScroll) {
                    if (currentX < leftLimitValue || currentX < 0) {
                        currentX = leftLimitValue
                    } else if (currentX > rightLimitValue || currentX > viewWidth) {
                        currentX = rightLimitValue
                    }

                    isOpen = currentX >= center

                    if (isOpen) {
                        bgPaint?.color = bgOpenColor
                    } else {
                        bgPaint?.color = bgCloseColor
                    }

                    invalidate()
                }

            }


            MotionEvent.ACTION_UP -> {

                currentX = event.x

                if (isScroll) {
                    isOpen = currentX >= center

                    if (currentX < leftLimitValue || currentX < 0) {
                        currentX = leftLimitValue
                    } else if (currentX > rightLimitValue || currentX > viewWidth) {
                        currentX = rightLimitValue
                    }

                    if (isOpen) {
                        bgPaint?.color = bgOpenColor
                    } else {
                        bgPaint?.color = bgCloseColor
                    }

                    if (isOpen) {
                        goOpen()
                    } else {
                        goClose()
                    }
                } else {

                    //这里，要做颜色的替换
                    if (isOpen) {
                        currentX = rightLimitValue
                        bgPaint?.color = bgCloseColor
                    } else {
                        currentX = leftLimitValue
                        bgPaint?.color = bgOpenColor
                    }

                    isOpen = !isOpen

                    if (isOpen) {
                        goOpen()
                    } else {
                        goClose()
                    }

                }

                mySvOnClickListener?.onClick(isOpen)

            }

        }

        return true

    }

    //设置是否开启，true表示开启
    fun setIsOpen(b: Boolean) {
        isOpen = b

        mHandler.sendEmptyMessageDelayed(TO_SHOW, 40)


    }

    fun getOpen(): Boolean {

        return isOpen
    }

    /**
     * 设置是否可以点击、滑动
     *
     * b：true 表示可以点击、滑动（即：进行交互）
     * tip：要提示的语句，一般和 b=false 结合使用（即：屏幕交互后，给出提示）
     */
    fun setIsCanTouch(b: Boolean, tip: String = "") {

        isCanTouch = b
        tipString = tip
    }

    private fun goClose() {
        isOpen = false
        mHandler.sendEmptyMessage(TO_CLOSE)
    }

    private fun goOpen() {
        isOpen = true
        mHandler.sendEmptyMessage(TO_OPEN)
    }


    interface MySvOnClickListener {
        fun onClick(isOpen: Boolean)
    }

    private var mySvOnClickListener: MySvOnClickListener? = null

    fun setMySvOnClickListener(listener: MySvOnClickListener) {
        mySvOnClickListener = listener
    }


}