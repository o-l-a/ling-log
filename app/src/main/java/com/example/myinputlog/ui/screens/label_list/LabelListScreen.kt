package com.example.myinputlog.ui.screens.label_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.SettingsCard
import com.example.myinputlog.ui.theme.spacing


@Composable
fun LabelListScreen(
    modifier: Modifier = Modifier,
    labelListViewModel: LabelListViewModel,
    navigateToLabelEntry: () -> Unit,
    navigateToLabel: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    val labelListUiState by labelListViewModel.labelListUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        MyInputLogTopAppBar(
            title = "",
            canNavigateBack = true,
            hasAddAction = true,
            onAdd = navigateToLabelEntry,
            navigateUp = onNavigateUp,
            scrollBehavior = scrollBehavior
        )
    }) { innerPadding ->
        when (val currentState = labelListUiState) {
            LabelListUiState.Empty -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.empty_course_collection_body_course_tab
                )
            }

            LabelListUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            LabelListUiState.Loading -> {
                LoadingBox()
            }

            is LabelListUiState.Success -> LabelListBody(
                modifier = Modifier.padding(
                    innerPadding
                ), labels = currentState.userLabels, onLabelClicked = navigateToLabel
            )
        }
        Text("Label list", modifier = Modifier.padding(innerPadding))
    }
}

@Composable
private fun LabelListBody(
    modifier: Modifier = Modifier, labels: List<LabelUiModel>, onLabelClicked: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    val isScrollEnabled by remember {
        derivedStateOf {
            scrollState.canScrollForward || scrollState.canScrollBackward
        }
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(
            MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall,
        ),
        state = scrollState,
        userScrollEnabled = isScrollEnabled
    ) {
        items(items = labels, key = { it.id }) { label ->
            SettingsCard(
                headlineContent = { Text(label.title) },
                onClick = { onLabelClicked(label.id) })
        }
    }
}