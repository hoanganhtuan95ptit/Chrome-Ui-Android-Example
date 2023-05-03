package com.tuanhoang.chrome.utils.ext

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform

fun View.updateMarginHorizontal(marginHorizontal: Int) {

    updateMarginHorizontal(marginHorizontal, marginHorizontal)
}

fun View.updateMarginHorizontal(marginStart: Int?, marginEnd: Int?) {

    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return


    if (marginStart == null && marginEnd == null) return
    if (params.marginStart == marginStart && params.marginEnd == marginEnd) return


    if (marginStart != null) params.marginStart = marginStart
    if (marginEnd != null) params.marginEnd = marginEnd


    layoutParams = params
}

fun ViewGroup.show(startView: View, endView: View) {

    val transform = MaterialContainerTransform().apply {

        duration = 350

        this.startView = startView
        this.endView = endView

        addTarget(endView)
        setPathMotion(MaterialArcMotion())

        interpolator = FastOutSlowInInterpolator()
        scrimColor = Color.TRANSPARENT
    }

    TransitionManager.beginDelayedTransition(this, transform)
}