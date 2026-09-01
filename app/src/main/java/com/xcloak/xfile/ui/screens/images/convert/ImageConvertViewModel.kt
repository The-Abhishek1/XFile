package com.xcloak.xfile.ui.screens.images.convert

import android.graphics.Bitmap
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

data class ImageConvertUiState(
    val selectedUri: Uri? = null,
    val targetFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    val isConverting: Boolean = false,
    val convertedFileUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class ImageConvertViewModel @Inject constructor(
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageConvertUiState())
    val uiState: StateFlow<ImageConvertUiState> = _uiState.asStateFlow()

    fun selectImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedUri = uri, convertedFileUri = null)
    }

    fun setFormat(format: Bitmap.CompressFormat) {
        _uiState.value = _uiState.value.copy(targetFormat = format)
    }

    fun convertImage() {
        val uri = _uiState.value.selectedUri ?: return
        val format = _uiState.value.targetFormat
        val ext = when(format) {
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP -> "webp"
            else -> "jpg"
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConverting = true, error = null)
            val result = imageProcessor.convertImage(
                uri, format, "converted_${System.currentTimeMillis()}.$ext"
            )
            result.onSuccess { file ->
                _uiState.value = _uiState.value.copy(
                    isConverting = false,
                    convertedFileUri = Uri.fromFile(file)
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isConverting = false,
                    error = e.message ?: "Failed to convert image"
                )
            }
        }
    }
}
