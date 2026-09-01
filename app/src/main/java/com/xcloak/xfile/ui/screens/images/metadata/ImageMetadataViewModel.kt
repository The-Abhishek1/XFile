package com.xcloak.xfile.ui.screens.images.metadata

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.xfile.core.image.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageMetadataUiState(
    val selectedUri: Uri? = null,
    val metadataSummary: Map<String, String> = emptyMap(),
    val isProcessing: Boolean = false,
    val successFileUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class ImageMetadataViewModel @Inject constructor(
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageMetadataUiState())
    val uiState: StateFlow<ImageMetadataUiState> = _uiState.asStateFlow()

    fun selectImage(uri: Uri) {
        val summary = imageProcessor.getMetadataSummary(uri)
        _uiState.value = _uiState.value.copy(
            selectedUri = uri,
            metadataSummary = summary,
            successFileUri = null,
            error = null
        )
    }

    fun stripMetadata() {
        val uri = _uiState.value.selectedUri ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            val result = imageProcessor.stripMetadata(
                uri, "clean_${System.currentTimeMillis()}.jpg"
            )
            result.onSuccess { file ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    successFileUri = Uri.fromFile(file)
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to strip metadata"
                )
            }
        }
    }
}
