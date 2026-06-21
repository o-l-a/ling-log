package com.example.myinputlog.ui.models

import android.annotation.SuppressLint
import android.content.Context
import com.example.myinputlog.data.local.entities.CountryGroupEntity

data class CountryGroupUiModel(
    val id: String, val localizedName: String, val isoCodes: List<String>
)

fun CountryGroupEntity.toUiModel(context: Context): CountryGroupUiModel {
    return CountryGroupUiModel(
        id = this.id, localizedName = context.getLocalizedGroupName(this), isoCodes = this.isoCodes
    )
}

@SuppressLint("DiscouragedApi")
fun Context.getLocalizedGroupName(entity: CountryGroupEntity): String {
    val resId = resources.getIdentifier(entity.nameKey, "string", packageName)
    return if (resId != 0) getString(resId) else entity.fallbackName
}