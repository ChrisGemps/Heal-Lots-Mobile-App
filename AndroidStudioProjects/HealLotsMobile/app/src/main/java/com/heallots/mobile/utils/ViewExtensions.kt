package com.heallots.mobile.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.heallots.mobile.R

/**
 * Convert DP to pixels
 */
fun Int.dp(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

/**
 * Convert DP to pixels (Float version)
 */
fun Float.dp(context: Context): Float = this * context.resources.displayMetrics.density

/**
 * Convert SP to pixels
 */
fun Int.sp(context: Context): Float = this.toFloat().sp(context)

/**
 * Convert SP to pixels (Float version)
 */
fun Float.sp(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics)

/**
 * Show view
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Hide view
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Show view conditionally
 */
fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Hide view conditionally
 */
fun View.hideIf(condition: Boolean) {
    visibility = if (condition) View.GONE else View.VISIBLE
}

/**
 * Toggle visibility
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Apply padding to all sides
 */
fun View.setPadding(paddingDp: Int) {
    val padding = paddingDp.dp(context)
    setPadding(padding, padding, padding, padding)
}

/**
 * Apply padding with different horizontal and vertical values
 */
fun View.setPadding(horizontalDp: Int, verticalDp: Int) {
    val horizontal = horizontalDp.dp(context)
    val vertical = verticalDp.dp(context)
    setPadding(horizontal, vertical, horizontal, vertical)
}

/**
 * Style button as outline style
 */
fun Button.styleOutlineButton(text: String, isRed: Boolean = false, context: Context? = null) {
    val ctx = context ?: this.context
    this.text = text
    background = GradientDrawable().apply {
        setColor(Color.WHITE)
        cornerRadius = 12f.dp(ctx)
        setStroke(1.dp(ctx), if (isRed) Color.parseColor("#FECACA") else Color.parseColor("#E8DDD0"))
    }
    setTextColor(if (isRed) Color.parseColor("#DC2626") else Color.parseColor("#44291A"))
}

/**
 * Style button with green gradient
 */
fun Button.styleGreenButton(text: String, context: Context? = null) {
    val ctx = context ?: this.context
    this.text = text
    background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#22C55E"), Color.parseColor("#15803D"))).apply {
        cornerRadius = 12f.dp(ctx)
    }
    setTextColor(Color.WHITE)
}

/**
 * Style button with blue gradient
 */
fun Button.styleBlueButton(text: String, context: Context? = null) {
    val ctx = context ?: this.context
    this.text = text
    background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#819DCD"), Color.parseColor("#0034A3"))).apply {
        cornerRadius = 12f.dp(ctx)
    }
    setTextColor(Color.WHITE)
}

/**
 * Style button with amber/orange gradient
 */
fun Button.styleAmberButton(text: String, context: Context? = null) {
    val ctx = context ?: this.context
    this.text = text
    background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#FEF3C7"), Color.parseColor("#F59E0B"))).apply {
        cornerRadius = 12f.dp(ctx)
        setStroke(1.dp(ctx), Color.parseColor("#D97706"))
    }
    setTextColor(Color.parseColor("#92400E"))
}

/**
 * Set button enabled state with alpha feedback
 */
fun Button.setEnabledWithAlpha(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

/**
 * Navigate to activity of type T
 */
inline fun <reified T : Activity> Context.navigateTo(flags: Int = 0) {
    Intent(this, T::class.java).apply {
        if (flags != 0) addFlags(flags)
    }.also { startActivity(it) }
}

/**
 * Navigate to activity with extras
 */
inline fun <reified T : Activity> Context.navigateToWithExtras(
    noinline extras: (Intent) -> Unit,
    flags: Int = 0
) {
    Intent(this, T::class.java).apply {
        if (flags != 0) addFlags(flags)
        extras(this)
    }.also { startActivity(it) }
}
