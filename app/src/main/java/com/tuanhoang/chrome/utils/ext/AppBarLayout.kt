package com.tuanhoang.chrome.utils.ext

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

fun AppBarLayout.setDrag(_behavior: AppBarLayout.Behavior? = null, drag: Boolean): Boolean {

    val behavior = _behavior ?: ((layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior) ?: return false

    behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {

        override fun canDrag(appBarLayout: AppBarLayout): Boolean {
            return drag
        }
    })

    requestLayout()

    return true
}