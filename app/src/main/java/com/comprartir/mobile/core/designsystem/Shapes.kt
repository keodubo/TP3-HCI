package com.comprartir.mobile.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ComprartirPillShape = RoundedCornerShape(999.dp)
val ComprartirCardShape = RoundedCornerShape(16.dp)
val ComprartirDialogShape = RoundedCornerShape(24.dp)

fun comprartirShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = ComprartirPillShape, // Buttons & chips default to pill corners.
    medium = ComprartirCardShape,
    large = ComprartirDialogShape,
    extraLarge = ComprartirDialogShape,
)
