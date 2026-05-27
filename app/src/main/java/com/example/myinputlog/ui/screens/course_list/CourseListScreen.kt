package com.example.myinputlog.ui.screens.course_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
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
            title = "",
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
                courses = currentState.userCourses,
                onCourseClicked = navigateToUserCourse
            )
        }
    }
}

@Composable
private fun CourseListBody(
    modifier: Modifier = Modifier, courses: List<UserCourse>, onCourseClicked: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    val isScrollEnabled by remember {
        derivedStateOf {
            scrollState.canScrollForward || scrollState.canScrollBackward
        }
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
        contentPadding = PaddingValues(
            end = MaterialTheme.spacing.extraExtraSmall
        ),
        state = scrollState,
        userScrollEnabled = isScrollEnabled
    ) {
        items(items = courses, key = { it.id }) { course ->
            CourseContainer(
                course = course, onCourseClicked = onCourseClicked
            )
        }
    }
}


@Composable
private fun CourseContainer(
    modifier: Modifier = Modifier, course: UserCourse, onCourseClicked: (String) -> Unit
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = MaterialTheme.spacing.small),
        headlineContent = {
            Text(
                text = course.name, style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.course_goal_card_text, course.goalInHours),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.course_other_source_hours_card_text, course.otherSourceHours
                ), style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            IconButton(onClick = { onCourseClicked(course.id) }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.edit_text)
                )
            }
        })
}