package com.xcloak.xfile.ui.screens.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.xfile.core.database.FileDao
import com.xcloak.xfile.core.database.ProcessedFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val fileDao: FileDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType

    val processedFiles: StateFlow<List<ProcessedFile>> = combine(
        fileDao.getAllProcessedFiles(),
        _searchQuery,
        _selectedType
    ) { files, query, type ->
        files.filter { file ->
            val matchesQuery = file.name.contains(query, ignoreCase = true)
            val matchesType = type == "All" || file.type == type.uppercase()
            matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedType(type: String) {
        _selectedType.value = type
    }

    fun deleteFile(file: ProcessedFile) {
        viewModelScope.launch {
            // Delete the physical file from storage (cacheDir)
            try {
                val path = android.net.Uri.parse(file.uri).path
                if (path != null) {
                    val f = File(path)
                    if (f.exists()) f.delete()
                }
            } catch (e: Exception) {
                // Ignore failures to delete from disk
            }
            fileDao.deleteFile(file)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            // Delete all physical files in history
            try {
                val files = fileDao.getAllProcessedFiles().first()
                files.forEach { file ->
                    val path = android.net.Uri.parse(file.uri).path
                    if (path != null) {
                        val f = File(path)
                        if (f.exists()) f.delete()
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
            fileDao.clearHistory()
        }
    }
}
