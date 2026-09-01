package com.xcloak.xfile.ui.screens.images.resize

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

data class ImageResizeUiState(
    val selectedUri: Uri? = null,
    val width: String = "",
    val height: String = "",
    val isResizing: Boolean = false,
    val resizedFileUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class ImageResizeViewModel @Inject constructor(
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageResizeUiState())
    val uiState: StateFlow<ImageResizeUiState> = _uiState.asStateFlow()

    fun selectImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedUri = uri)
    }

    fun updateDimensions(width: String, height: String) {
        _uiState.value = _uiState.value.copy(width = width, height = height)
    }

    fun resizeImage() {
        val uri = _uiState.value.selectedUri ?: return
        val w = _uiState.value.width.toIntOrNull() ?: return
        val h = _uiState.value.height.toIntOrNull() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResizing = true, error = null)
            val result = imageProcessor.resizeImage(
                uri, w, h, "resized_${System.currentTimeMillis()}.jpg"
            )
            result.onSuccess { file ->
                _uiState.value = _uiState.value.copy(
                    isResizing = false,
                    resizedFileUri = Uri.fromFile(file)
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isResizing = false,
                    error = e.message ?: "Failed to resize image"
                )
            }
        }
    }
}
