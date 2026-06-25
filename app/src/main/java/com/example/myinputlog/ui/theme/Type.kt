package com.example.myinputlog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val RobotoFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Roboto"), fontProvider = provider)
)

private val defaultTypography = Typography()
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = RobotoFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = RobotoFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = RobotoFontFamily),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = RobotoFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = RobotoFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = RobotoFontFamily),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = RobotoFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = RobotoFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = RobotoFontFamily),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = RobotoFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = RobotoFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = RobotoFontFamily),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = RobotoFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = RobotoFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = RobotoFontFamily)
)