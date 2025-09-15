package com.adikob.galaxychat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.adikob.galaxychat.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val SpaceGrotesk = FontFamily(
    Font(resId = R.font.space_grotesk_medium, weight = FontWeight.Medium),
    Font(resId = R.font.space_grotesk_regular, weight = FontWeight.Normal),
    Font(resId = R.font.space_grotesk_semi_bold, weight = FontWeight.SemiBold),
    Font(resId = R.font.space_grotesk_bold, weight = FontWeight.Bold),
    Font(resId = R.font.space_grotesk_light, weight = FontWeight.Light)
)

val WorkSans = FontFamily(
    Font(resId = R.font.work_sans_medium, weight = FontWeight.Medium),
    Font(resId = R.font.work_sans_regular, weight = FontWeight.Normal),
    Font(resId = R.font.work_sans_semi_bold, weight = FontWeight.SemiBold),
    Font(resId = R.font.work_sans_thin, weight = FontWeight.Thin),
    Font(resId = R.font.work_sans_bold, weight = FontWeight.Bold),
    Font(resId = R.font.work_sans_extra_bold, weight = FontWeight.ExtraBold),
    Font(resId = R.font.work_sans_light, weight = FontWeight.Light)
)