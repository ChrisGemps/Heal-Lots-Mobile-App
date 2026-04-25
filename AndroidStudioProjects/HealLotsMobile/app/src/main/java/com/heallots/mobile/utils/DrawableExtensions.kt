package com.heallots.mobile.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

/**
 * Parse color string and return color integer
 */
fun String.toColorInt(): Int = Color.parseColor(this)

/**
 * Create a rounded rectangle drawable with fill color
 */
fun createRoundedRect(
    fillColor: String,
    radiusDp: Int,
    context: Context
): GradientDrawable = GradientDrawable().apply {
    setColor(Color.parseColor(fillColor))
    cornerRadius = radiusDp.dp(context).toFloat()
}

/**
 * Create a rounded rectangle with stroke
 */
fun createRoundedRectWithStroke(
    fillColor: String,
    radiusDp: Int,
    strokeWidthDp: Int,
    strokeColor: String,
    context: Context
): GradientDrawable = GradientDrawable().apply {
    setColor(Color.parseColor(fillColor))
    cornerRadius = radiusDp.dp(context).toFloat()
    setStroke(strokeWidthDp.dp(context), Color.parseColor(strokeColor))
}

/**
 * Create a gradient drawable
 */
fun createGradientDrawable(
    orientation: GradientDrawable.Orientation,
    startColor: String,
    endColor: String,
    radiusDp: Int,
    context: Context
): GradientDrawable = GradientDrawable(
    orientation,
    intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
).apply {
    cornerRadius = radiusDp.dp(context).toFloat()
}

/**
 * Create a top-to-bottom gradient
 */
fun createGradientTTB(
    startColor: String,
    endColor: String,
    radiusDp: Int,
    context: Context
): GradientDrawable = createGradientDrawable(
    GradientDrawable.Orientation.TOP_BOTTOM,
    startColor,
    endColor,
    radiusDp,
    context
)

/**
 * Create a diagonal gradient (top-left to bottom-right)
 */
fun createGradientDiagonal(
    startColor: String,
    endColor: String,
    radiusDp: Int,
    context: Context
): GradientDrawable = createGradientDrawable(
    GradientDrawable.Orientation.TL_BR,
    startColor,
    endColor,
    radiusDp,
    context
)

/**
 * Apply corner radius to GradientDrawable
 */
fun GradientDrawable.withCornerRadius(radiusDp: Int, context: Context? = null): GradientDrawable {
    if (context != null) {
        cornerRadius = radiusDp.dp(context).toFloat()
    } else {
        cornerRadius = radiusDp.toFloat()
    }
    return this
}

/**
 * Apply stroke to GradientDrawable
 */
fun GradientDrawable.withStroke(
    widthDp: Int,
    colorHex: String,
    context: Context? = null
): GradientDrawable {
    val widthPx = if (context != null) widthDp.dp(context) else widthDp
    setStroke(widthPx, Color.parseColor(colorHex))
    return this
}

/**
 * Set color on GradientDrawable
 */
fun GradientDrawable.withColor(colorHex: String): GradientDrawable {
    setColor(Color.parseColor(colorHex))
    return this
}

/**
 * Apply multiple properties to GradientDrawable
 */
fun GradientDrawable.configure(
    fillColor: String? = null,
    radiusDp: Int? = null,
    strokeWidthDp: Int? = null,
    strokeColor: String? = null,
    context: Context? = null
): GradientDrawable {
    fillColor?.let { setColor(Color.parseColor(it)) }
    radiusDp?.let { cornerRadius = if (context != null) it.dp(context).toFloat() else it.toFloat() }
    if (strokeWidthDp != null && strokeColor != null) {
        val widthPx = if (context != null) strokeWidthDp.dp(context) else strokeWidthDp
        setStroke(widthPx, Color.parseColor(strokeColor))
    }
    return this
}
