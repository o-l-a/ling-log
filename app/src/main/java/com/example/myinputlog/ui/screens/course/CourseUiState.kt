package com.example.myinputlog.ui.screens.course

import com.example.myinputlog.ui.models.CountryGroupUiModel
import com.example.myinputlog.ui.models.CourseUiModel

sealed interface CourseUiState {
    data object Loading : CourseUiState
    data object Error : CourseUiState
    data class Success(
        val courseId: String = "",
        val courseFields: CourseFields,
        val allCountryGroups: List<CountryGroupUiModel> = emptyList(),
        val isFormValid: Boolean = false,
        val isDialogVisible: Boolean = false
    ) : CourseUiState
}

data class CourseFields(
    val name: String = "",
    val goalInHours: String = "",
    val otherSourceHours: String = "",
    val countryGroup: CountryGroupUiModel? = null
)

fun CourseFields.toUserCourse(id: String): CourseUiModel = CourseUiModel(
    id = id,
    name = name,
    goalInHours = goalInHours.toLongOrNull() ?: 0L,
    otherSourceHours = otherSourceHours.toLongOrNull() ?: 0L,
    countryGroup = countryGroup!!
)