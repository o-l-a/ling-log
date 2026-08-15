package com.example.myinputlog.ui.models

import androidx.annotation.StringRes
import com.example.myinputlog.R

enum class RankingCategory(@get:StringRes val labelRes: Int) {
    LABEL(R.string.label_list_nav_description),
    CHANNEL(R.string.channel_list_screen_title),
    COUNTRY(R.string.countries_description)
}

enum class RankingLimit(val limit: Int) {
    TOP_3(3),
    TOP_5(5),
    TOP_10(10),
    TOP_30(30),
    TOP_100(100)
}