package com.example.myinputlog.ui.screens.course_list

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.screens.common.composable.SettingsCard
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogTopAppBar
import com.example.myinputlog.ui.screens.common.composable.input.MyInputLogDropdownField
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.theme.spacing

@Composable
fun CourseListScreen(
    modifier: Modifier = Modifier,
    courseListViewModel: CourseListViewModel,
    navigateToUserCourseEntry: () -> Unit,
    navigateToUserCourse: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    val courseListUiState by courseListViewModel.courseListUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        MyInputLogTopAppBar(
            title = stringResource(R.string.course_list_nav_description),
            canNavigateBack = true,
            hasAddAction = true,
            onAdd = navigateToUserCourseEntry,
            navigateUp = onNavigateUp,
            scrollBehavior = scrollBehavior
        )
    }) { innerPadding ->
        when (val currentState = courseListUiState) {
            CourseListUiState.Empty -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.empty_course_collection_body_course_tab
                )
            }

            CourseListUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            CourseListUiState.Loading -> {
                LoadingBox()
            }

            is CourseListUiState.Success -> CourseListBody(
                modifier = Modifier.padding(
                    innerPadding
                ),
                currentCourse = currentState.selectedCourse,
                courses = currentState.userCourses,
                onActiveCourseChange = courseListViewModel::changeCurrentCourseId,
                onCourseClicked = navigateToUserCourse
            )
        }
    }
}

@Composable
private fun CourseListBody(
    modifier: Modifier = Modifier,
    currentCourse: CourseUiModel?,
    courses: List<CourseUiModel>,
    onActiveCourseChange: (CourseUiModel) -> Unit,
    onCourseClicked: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(MaterialTheme.spacing.mediumPlus),
        state = scrollState
    ) {
        item {
            MyInputLogDropdownField(
                value = currentCourse,
                onValueChange = onActiveCourseChange,
                options = courses,
                label = R.string.course_active_course,
                isInTopBar = false,
                isEditable = true
            )
        }
        items(items = courses, key = { it.id }) { course ->
            SettingsCard(headlineContent = { Text(course.name) }, supportingContent = {
                Text(
                    text = stringResource(
                        R.string.course_goal_card_text, course.goalInHours
                    ), style = MaterialTheme.typography.bodyMedium
                )
            }, onClick = { onCourseClicked(course.id) })
        }
    }
}