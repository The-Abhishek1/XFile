package com.xcloak.xfile.ui.screens.pdf.merge

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.xfile.core.pdf.PdfProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PdfMergeUiState(
    val selectedUris: List<Uri> = emptyList(),
    val isMerging: Boolean = false,
    val mergedFileUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class PdfMergeViewModel @Inject constructor(
    private val pdfProcessor: PdfProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfMergeUiState())
    val uiState: StateFlow<PdfMergeUiState> = _uiState.asStateFlow()

    fun addFiles(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            selectedUris = _uiState.value.selectedUris + uris
        )
    }

    fun removeFile(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedUris = _uiState.value.selectedUris - uri
        )
    }

    fun moveFile(from: Int, to: Int) {
        val list = _uiState.value.selectedUris.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        _uiState.value = _uiState.value.copy(selectedUris = list)
    }

    fun mergeFiles() {
        if (_uiState.value.selectedUris.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMerging = true, error = null)
            val result = pdfProcessor.mergePdfs(
                _uiState.value.selectedUris,
                "merged_${System.currentTimeMillis()}.pdf"
            )
            result.onSuccess { file ->
                _uiState.value = _uiState.value.copy(
                    isMerging = false,
                    mergedFileUri = Uri.fromFile(file)
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isMerging = false,
                    error = e.message ?: "Failed to merge PDFs"
                )
            }
        }
    }
}
