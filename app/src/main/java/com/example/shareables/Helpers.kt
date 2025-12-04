package com.example.shareables

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

fun getGradientColors(bitmap: Bitmap): GradientColors {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val palette = Palette.from(mutableBitmap).generate()

    return GradientColors(
        top = (palette.darkVibrantSwatch?.rgb ?: 0).toHexString(),
        bottom = (palette.lightVibrantSwatch?.rgb ?: 0).toHexString()
    )
}

fun Int.toHexString(): String {
    return String.format("#%06X", this and 0x00FFFFFF)
}

data class GradientColors(val top: String, val bottom: String)