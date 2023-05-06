package com.tuanhoang.chrome.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import com.tuanhoang.chrome.R


class RoundImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    private val radii: FloatArray


    private val path = Path()

    private val mainRectF = RectF()


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView, defStyleAttr, 0)

        this.adjustViewBounds = typedArray.getBoolean(R.styleable.RoundImageView_android_adjustViewBounds, true)

        val radius = typedArray.getDimension(R.styleable.RoundImageView_rv_radius, 0f)

        val topLeftRadius = typedArray.getDimension(R.styleable.RoundImageView_rv_topLeftRadius, radius)
        val topRightRadius = typedArray.getDimension(R.styleable.RoundImageView_rv_topRightRadius, radius)
        val bottomLeftRadius = typedArray.getDimension(R.styleable.RoundImageView_rv_bottomLeftRadius, radius)
        val bottomRightRadius = typedArray.getDimension(R.styleable.RoundImageView_rv_bottomRightRadius, radius)

        radii = floatArrayOf(topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius)

        typedArray.recycle()

        scaleType = ScaleType.MATRIX
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mainRectF.set(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun onDraw(canvas: Canvas) {

        path.reset()

        path.addRoundRect(mainRectF, radii, Path.Direction.CW)

        canvas.clipPath(path)

        super.onDraw(canvas)
    }
}