package com.example.myinputlog.data.utils

import android.content.Context
import com.example.myinputlog.data.local.entities.CountryGroupEntity
import com.example.myinputlog.ui.models.getLocalizedGroupName
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

interface StringProvider {
    fun getLocalizedGroupName(entity: CountryGroupEntity): String
}

class AndroidStringProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) : StringProvider {
    override fun getLocalizedGroupName(entity: CountryGroupEntity): String {
        return context.getLocalizedGroupName(entity)
    }
}