package com.colman.matconli.utilis

import android.view.View
import androidx.fragment.app.Fragment

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.toggleVisibility(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

inline fun Fragment.runOnUiThreadSafe(crossinline block: () -> Unit) {
    activity?.runOnUiThread {
        if (isAdded && view != null) {
            block()
        }
    }
}
