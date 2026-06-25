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

val NunitoFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider)
)

private val defaultTypography = Typography()
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = NunitoFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = NunitoFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = NunitoFontFamily),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = NunitoFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = NunitoFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = NunitoFontFamily),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = NunitoFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = NunitoFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = NunitoFontFamily),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = NunitoFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = NunitoFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = NunitoFontFamily),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = NunitoFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = NunitoFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = NunitoFontFamily)
)