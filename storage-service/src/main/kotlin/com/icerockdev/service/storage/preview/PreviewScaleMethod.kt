package com.icerockdev.service.storage.preview

import com.sksamuel.scrimage.ScaleMethod

enum class PreviewScaleMethod {
    FastScale,
    Lanczos3,
    BSpline,
    Bilinear,
    Bicubic;

    fun getScale(): ScaleMethod {
        return when(this) {
            FastScale -> ScaleMethod.FastScale
            Lanczos3 -> ScaleMethod.Lanczos3
            BSpline -> ScaleMethod.BSpline
            Bilinear -> ScaleMethod.Bilinear
            Bicubic -> ScaleMethod.Bicubic
        }
    }
}
