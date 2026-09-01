package com.xcloak.xfile.ui.screens.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.xfile.core.database.FileDao
import com.xcloak.xfile.core.database.ProcessedFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val fileDao: FileDao
) : ViewModel() {

    val processedFiles: StateFlow<List<ProcessedFile>> = fileDao.getAllProcessedFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteFile(file: ProcessedFile) {
        viewModelScope.launch {
            fileDao.deleteFile(file)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            fileDao.clearHistory()
        }
    }
}
