package com.demo.mycalibrationview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast


class MyCalibrationView : View {

    private var mContext: Context? = null

    private var tempPaint: Paint? = null

    private var paint: Paint? = null
    private var pointPaint: Paint? = null
    private var textPaint: TextPaint? = null
    private var moveTextPaint: TextPaint? = null

    //控件的宽
    private var viewWidth: Float = 0f
    //控件的高
    private var viewHeight: Float = 0f

    //刻度线水平主线的颜色值
    private var lineColor: Int = 0
    //刻度上显示选中位置的点的颜色
    private var pointColor: Int = 0

    //刻度旁边，端点处文字的大小
    private var tSize: Int = 0
    //刻度旁边，端点处文字的颜色
    private var tColor: Int = 0

    //刻度上文字的大小
    private var moveTSize: Int = 10
    //刻度上文字的颜色
    private var moveTColor: Int = 0

    //线的高度
    private var lineHeight: Int = 0

    private var ascent: Float = 0f
    private var descent: Float = 0f
    private var textOffset: Float = 0f

    private var moveTextAscent: Float = 0f
    private var moveTextDescent: Float = 0f
    private var moveTextOffset: Float = 0f
    //移动文字的高度。
    private var moveTextHeight: Float = 0f

    //线和文字的垂直方向的距离
    private var lineTextDistance = 0f
    //横线距离控件顶部，文字距离控件底部的距离
    private var topBottomDistance = 0f

    //线的长度。
    private var lineLength: Float = 0f
    //一小段的距离。如：1 到 2，这一段的距离
    private var cellLineLength: Float = 0f

    //基础间距，文字和线段之间的间距
    private var baseDistance: Float = 0f

    private var startTextWidth: Float = 0f
    private var endTextWidth: Float = 0f

    //基线的Y轴坐标
    private var baseLineY: Float = 0f

    //圆点半径
    private var radius: Float = 0f

    //当前位置
    private var currentIndex: Int = 2

    private var pointX_0: Float = 0f
    private var pointX_1: Float = 0f
    private var pointX_2: Float = 0f
    private var pointX_3: Float = 0f

    //绘制的圆点的圆心，对应的X轴坐标
    private var drawPointX: Float = 0f

    //随圆点移动的文字
    private var drawText: Int = 2

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context

        val ta = context?.obtainStyledAttributes(attrs, R.styleable.MyCalibrationView)
        lineColor = ta?.getColor(R.styleable.MyCalibrationView_lineColor, 0) ?: 0
        pointColor = ta?.getColor(R.styleable.MyCalibrationView_pointColor, 0) ?: 0

        tSize = ta?.getInteger(R.styleable.MyCalibrationView_tSize, 0) ?: 0
        tColor = ta?.getColor(R.styleable.MyCalibrationView_tColor, 0) ?: 0

        moveTSize = ta?.getInteger(R.styleable.MyCalibrationView_moveTSize, 10) ?: 10
        moveTColor = ta?.getColor(R.styleable.MyCalibrationView_moveTColor, 0) ?: 0

        ta?.recycle()

        init()
    }

    //先于 onMeasure 执行
    private fun init() {

        if (lineColor == 0 || pointColor == 0 || mContext == null) {
            //线的颜色，点的颜色，上下文。任何一个为空，必要数据缺失，就不往下走了
            return
        }

        tempPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tempPaint?.color = Color.BLACK
        paint?.strokeWidth = 10f

        lineTextDistance = UiUtils.dp2px(mContext!!, 10f).toFloat()

        topBottomDistance = UiUtils.dp2px(mContext!!, 15f).toFloat()

        baseDistance = UiUtils.dp2px(mContext!!, 5f).toFloat()

        lineHeight = UiUtils.dp2px(mContext!!, 5f)

        radius = (lineHeight / 2 + UiUtils.dp2px(mContext!!, 3f)).toFloat()

        //设置线的画笔相关属性
        paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint?.color = lineColor

        //设置画笔样式为：填充
        paint?.style = Paint.Style.FILL

        //设置画笔宽度
        paint?.strokeWidth = lineHeight.toFloat()

        /**
         * Paint.Cap.BUTT：默认类型
         *
         * Paint.Cap.SQUARE：以线条宽度为大小，在开头和结尾分别添加半个正方形
         *
         * Paint.Cap.ROUND：以线条宽度为直径，在开头和结尾分别添加一个半圆
         */
        paint?.strokeCap = Paint.Cap.ROUND

        //点的画笔
        pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        pointPaint?.color = pointColor

        pointPaint?.style = Paint.Style.FILL

        pointPaint?.strokeWidth = 10f


        //设置文字画笔相关
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        textPaint?.textSize = UiUtils.dp2px(mContext!!, tSize.toFloat()).toFloat()
        textPaint?.textAlign = Paint.Align.CENTER

        if (tColor == 0) {
            textPaint?.color = Color.BLACK
        } else {
            textPaint?.color = tColor
        }

        ascent = textPaint?.ascent() ?: 0f
        descent = textPaint?.descent() ?: 0f

        //偏移量，用于辅助文字在竖直方向居中
        textOffset = (ascent + descent) / 2

        startTextWidth = textPaint!!.measureText("关闭")
        endTextWidth = textPaint!!.measureText("3")

        //设置移动文字画笔相关
        moveTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        moveTextPaint?.textSize = UiUtils.dp2px(mContext!!, moveTSize.toFloat()).toFloat()
        moveTextPaint?.textAlign = Paint.Align.CENTER

        if (moveTColor == 0) {
            moveTextPaint?.color = Color.BLACK
        } else {
            moveTextPaint?.color = moveTColor
        }

        moveTextAscent = moveTextPaint?.ascent() ?: 0f
        moveTextDescent = moveTextPaint?.descent() ?: 0f

        //偏移量，用于辅助文字在竖直方向居中
        moveTextOffset = (moveTextAscent + moveTextDescent) / 2

        moveTextHeight = moveTextDescent - moveTextAscent

        //计算基线Y轴坐标
        baseLineY = topBottomDistance + moveTextHeight + lineTextDistance + lineHeight / 2

    }

    //先于 onSizeChanged 执行
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(
            widthMeasureSpec,
            (topBottomDistance * 2 + moveTextHeight + lineTextDistance + lineHeight).toInt()
        )

    }

    //先于 onDraw 执行
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        Log.e("w is $w ", "h is $h")

        viewWidth = w.toFloat()

        viewHeight = h.toFloat()

        lineLength = viewWidth - baseDistance * 2 - startTextWidth - endTextWidth + lineHeight

        cellLineLength = lineLength / 3

        pointX_0 = startTextWidth + baseDistance + radius - lineHeight / 2
        pointX_1 = startTextWidth + baseDistance + lineLength / 3
        pointX_2 = startTextWidth + baseDistance + lineLength / 3 * 2
        pointX_3 = startTextWidth + baseDistance + lineLength + lineHeight / 2 - radius

        drawPointX = getCurrentPointX(currentIndex)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (lineColor != 0 && pointColor != 0 && viewWidth != 0f) {

            //绘制主横线
            canvas?.drawLine(
                startTextWidth + baseDistance,
                baseLineY,
                viewWidth - endTextWidth - baseDistance,
                baseLineY,
                paint!!
            )

            //绘制起始文字
            canvas?.drawText(
                "关闭",
                startTextWidth / 2,
                baseLineY - textOffset,
                textPaint!!
            )

            //绘制终点文字
            canvas?.drawText(
                "3",
                viewWidth - endTextWidth / 2,
                baseLineY - textOffset,
                textPaint!!
            )

            //绘制移动文字
            canvas?.drawText(
                drawText.toString(),
                drawPointX,
                topBottomDistance + moveTextHeight / 2 - moveTextOffset,
                moveTextPaint!!
            )

            //绘制圆点
            canvas?.drawCircle(
                drawPointX,
                baseLineY,
                radius,
                pointPaint!!
            )


            //========= 以下是辅助点，无实际作用 ==========


            tempPaint?.color = Color.BLUE
            canvas?.drawCircle(
                pointX_0,
                baseLineY,
                radius,
                tempPaint!!
            )
            canvas?.drawCircle(
                pointX_1,
                baseLineY,
                radius,
                tempPaint!!
            )
            canvas?.drawCircle(
                pointX_2,
                baseLineY,
                radius,
                tempPaint!!
            )
            canvas?.drawCircle(
                pointX_3,
                baseLineY,
                radius,
                tempPaint!!
            )

            tempPaint?.color = Color.BLACK
            canvas?.drawCircle(
                pointX_1 - cellLineLength / 2,
                baseLineY,
                radius,
                tempPaint!!
            )
            canvas?.drawCircle(
                pointX_1 + cellLineLength / 2,
                baseLineY,
                radius,
                tempPaint!!
            )
            canvas?.drawCircle(
                pointX_2 + cellLineLength / 2,
                baseLineY,
                radius,
                tempPaint!!
            )


        }

    }

    fun setIndex(index: Int) {

        currentIndex = index

        if (currentIndex < 0) {
            currentIndex = 0
        } else if (currentIndex > 3) {
            currentIndex = 3
        }

        drawPointX = getCurrentPointX(index)
        drawText = getDrawText(drawPointX)

        invalidate()

        Toast.makeText(mContext!!, "$currentIndex", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentPointX(index: Int): Float {

        when (index) {

            0 -> {
                return pointX_0
            }
            1 -> {
                return pointX_1
            }
            2 -> {
                return pointX_2
            }
            3 -> {
                return pointX_3
            }
            else -> {
                //默认是2
                return pointX_2
            }

        }
    }

    private fun getDrawText(x: Float): Int {

        var result: Int = 2

        if (x < pointX_1 - cellLineLength / 2) {

            result = 0

        } else if (x >= pointX_1 - cellLineLength / 2 && x < pointX_1 + cellLineLength / 2) {

            result = 1

        } else if (x >= pointX_2 - cellLineLength / 2 && x < pointX_2 + cellLineLength / 2) {

            result = 2

        } else {

            result = 3

        }


        return result
    }

    private var downX: Float = 0f
    private var downY: Float = 0f

    private var moveX: Float = 0f
    private var moveY: Float = 0f

    //isScroll = true 表示发生了滑动
    private var isScroll: Boolean = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event ?: return super.onTouchEvent(event)

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                downX = event.x
                downY = event.y

            }

            MotionEvent.ACTION_MOVE -> {

                moveX = event.x
                moveY = event.y

                //如果滑动位置和点击位置，相差超过20像素，就标记一下。记录为"滑动"
                isScroll = Math.abs(moveX - downX) > 5

                if (moveX < pointX_0) {

                    moveX = pointX_0

                } else if (moveX > pointX_3) {

                    moveX = pointX_3

                }

                if (isScroll) {

                    drawPointX = moveX

                    drawText = getDrawText(drawPointX)

                    invalidate()

                }

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                moveX = event.x

                if (isScroll) {
                    //发生了滑动，抬起手后，要定位到距离最近的位置

                    if (moveX < pointX_0) {

                        setIndex(0)

                    } else if (moveX > pointX_3) {

                        setIndex(3)

                    } else {

                        drawText = getDrawText(moveX)
                        setIndex(drawText)
                    }


                } else {
                    /**
                     * 没有发生滑动，要进行判断
                     *
                     * 如果点击位置在圆点的左边，就 -1
                     *
                     * 如果点击位置在圆点的右边，就 +1
                     *
                     * 前提：不能超过临界值
                     */

                    if (moveX < drawPointX) {

                        drawText--
                        setIndex(drawText)

                    } else {

                        drawText++
                        setIndex(drawText)

                    }


                }

                isScroll = false
            }


        }

        return true
    }


}