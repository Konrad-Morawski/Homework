package morawski.mvi.views

import android.view.View

// just some syntax sugar

fun View.show() { visibility = View.VISIBLE }

fun View.hide() { visibility = View.GONE }

fun View.showIf(condition: Boolean) = if (condition) show() else hide()