package com.xcloak.xfile.ui.screens.pdf.split

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

data class PdfSplitUiState(
    val selectedUri: Uri? = null,
    val pageRange: String = "",
    val isSplitting: Boolean = false,
    val splitFiles: List<Uri> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PdfSplitViewModel @Inject constructor(
    private val pdfProcessor: PdfProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfSplitUiState())
    val uiState: StateFlow<PdfSplitUiState> = _uiState.asStateFlow()

    fun selectFile(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedUri = uri, splitFiles = emptyList())
    }

    fun updateRange(range: String) {
        _uiState.value = _uiState.value.copy(pageRange = range)
    }

    fun splitPdf() {
        val uri = _uiState.value.selectedUri ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSplitting = true, error = null)
            val result = pdfProcessor.splitPdf(uri)
            result.onSuccess { files ->
                _uiState.value = _uiState.value.copy(
                    isSplitting = false,
                    splitFiles = files.map { Uri.fromFile(it) }
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSplitting = false,
                    error = e.message ?: "Failed to split PDF"
                )
            }
        }
    }
}
