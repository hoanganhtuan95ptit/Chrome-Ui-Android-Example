package com.tuanhoang.chrome.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import com.tuanhoang.chrome.R


class TopCropImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    private val radii: FloatArray


    private val path = Path()

    private val mainRectF = RectF()


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TopCropImageView, defStyleAttr, 0)

        this.adjustViewBounds = typedArray.getBoolean(R.styleable.TopCropImageView_android_adjustViewBounds, true)

        val radius = typedArray.getDimension(R.styleable.TopCropImageView_radius, 0f)

        val topLeftRadius = typedArray.getDimension(R.styleable.TopCropImageView_topLeftRadius, radius)
        val topRightRadius = typedArray.getDimension(R.styleable.TopCropImageView_topRightRadius, radius)
        val bottomLeftRadius = typedArray.getDimension(R.styleable.TopCropImageView_bottomLeftRadius, radius)
        val bottomRightRadius = typedArray.getDimension(R.styleable.TopCropImageView_bottomRightRadius, radius)

        radii = floatArrayOf(topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius)

        typedArray.recycle()

        scaleType = ScaleType.MATRIX
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mainRectF.set(0f + paddingLeft, 0f + paddingTop, width.toFloat() - paddingRight, height.toFloat() - paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {

        path.reset()

        path.addRoundRect(mainRectF, radii, Path.Direction.CW)

        canvas.clipPath(path)

        super.onDraw(canvas)
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {

        if (drawable == null) {

            return super.setFrame(l, t, r, b)
        }

        val matrix: Matrix = imageMatrix

        val scale: Float

        val viewWidth: Int = width - paddingLeft - paddingRight
        val viewHeight: Int = height - paddingTop - paddingBottom

        val drawableWidth: Int = drawable.intrinsicWidth
        val drawableHeight: Int = drawable.intrinsicHeight

        scale = if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            viewHeight.toFloat() / drawableHeight.toFloat()
        } else {
            viewWidth.toFloat() / drawableWidth.toFloat()
        }

        matrix.setScale(scale, scale)

        imageMatrix = matrix

        return super.setFrame(l, t, r, b)
    }
}