package com.example.myinputlog.ui.screens.label

import androidx.lifecycle.ViewModel
import com.example.myinputlog.data.repository.StorageDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LabelViewModel @Inject constructor(repository: StorageDataRepository) : ViewModel() {}