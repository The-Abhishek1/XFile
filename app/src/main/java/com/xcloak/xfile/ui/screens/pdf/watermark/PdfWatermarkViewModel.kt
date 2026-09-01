package com.xcloak.xfile.ui.screens.pdf.watermark

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

data class PdfWatermarkUiState(
    val selectedUri: Uri? = null,
    val watermarkText: String = "",
    val opacity: Float = 0.5f,
    val rotation: Int = 45,
    val isProcessing: Boolean = false,
    val resultUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class PdfWatermarkViewModel @Inject constructor(
    private val pdfProcessor: PdfProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfWatermarkUiState())
    val uiState: StateFlow<PdfWatermarkUiState> = _uiState.asStateFlow()

    fun selectUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedUri = uri, resultUri = null, error = null)
    }

    fun updateWatermarkText(text: String) {
        _uiState.value = _uiState.value.copy(watermarkText = text)
    }

    fun updateOpacity(opacity: Float) {
        _uiState.value = _uiState.value.copy(opacity = opacity)
    }

    fun updateRotation(rotation: Int) {
        _uiState.value = _uiState.value.copy(rotation = rotation)
    }

    fun applyWatermark() {
        val state = _uiState.value
        val uri = state.selectedUri ?: return
        if (state.watermarkText.isBlank()) {
            _uiState.value = state.copy(error = "Watermark text cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            val result = pdfProcessor.addWatermark(
                uri = uri,
                text = state.watermarkText,
                outputFileName = "watermarked_${System.currentTimeMillis()}.pdf",
                opacity = state.opacity,
                rotation = state.rotation
            )
            result.onSuccess { file ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    resultUri = Uri.fromFile(file)
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to apply watermark"
                )
            }
        }
    }
    
    fun resetResult() {
        _uiState.value = _uiState.value.copy(resultUri = null)
    }
}
