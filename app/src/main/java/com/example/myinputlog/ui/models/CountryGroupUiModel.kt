package com.example.myinputlog.ui.models

import android.content.Context
import com.example.myinputlog.R
import com.example.myinputlog.data.local.entities.CountryGroupEntity
import com.example.myinputlog.data.utils.StringProvider

data class CountryGroupUiModel(
    val id: String = "", val localizedName: String = "", val isoCodes: List<String> = emptyList()
)

fun CountryGroupEntity.toUiModel(stringProvider: StringProvider): CountryGroupUiModel {
    return CountryGroupUiModel(
        id = this.id,
        localizedName = stringProvider.getLocalizedGroupName(this),
        isoCodes = this.isoCodes
    )
}

fun Context.getLocalizedGroupName(entity: CountryGroupEntity): String {
    val resId = when (entity.nameKey) {
        "group_spanish_global" -> R.string.group_spanish_global
        "group_french_global" -> R.string.group_french_global
        "group_german_global" -> R.string.group_german_global
        "group_russian_global" -> R.string.group_russian_global
        "group_japanese_global" -> R.string.group_japanese_global
        else -> null
    }

    return resId?.let { getString(it) } ?: entity.fallbackName
}