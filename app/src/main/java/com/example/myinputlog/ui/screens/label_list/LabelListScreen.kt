package com.example.myinputlog.ui.screens.label_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.screens.utils.composable.ClickableLabelChip
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
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
            LabelListUiState.Loading -> {
                LoadingBox()
            }

            is LabelListUiState.Empty -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.empty_label_collection_body
                )
            }

            is LabelListUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }


            is LabelListUiState.Success -> LabelListBody(
                modifier = Modifier.padding(
                    innerPadding
                ), labels = currentState.groupedLabels, onLabelClicked = navigateToLabel
            )
        }
    }
}

@Composable
private fun LabelListBody(
    modifier: Modifier = Modifier,
    labels: Map<String, List<LabelUiModel>>,
    onLabelClicked: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        contentPadding = PaddingValues(
            MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall,
        ),
        state = scrollState
    ) {
        labels.forEach { (firstLetter, labels) ->
            item(key = firstLetter) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .padding(
                            top = MaterialTheme.spacing.small,
                            bottom = MaterialTheme.spacing.extraExtraSmall
                        ), text = firstLetter, style = MaterialTheme.typography.titleMedium
                )
            }

            item(key = "${firstLetter}_group") {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        labels.forEach { label ->
                            ClickableLabelChip(
                                onClick = { onLabelClicked(label.id) },
                                title = label.title,
                                backgroundColor = Color(label.color),
                                textColor = Color(label.textColor)
                            )
                        }
                    }
                }
            }
        }
    }
}