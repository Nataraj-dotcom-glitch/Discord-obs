package com.darkalise.obs.recorder

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraOverlayManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null

    private val _isCameraActive = MutableStateFlow(false)
    val isCameraActive: StateFlow<Boolean> = _isCameraActive.asStateFlow()

    private val _cameraFacingFront = MutableStateFlow(true)
    val cameraFacingFront: StateFlow<Boolean> = _cameraFacingFront.asStateFlow()

    fun bindCameraPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onCameraBound: ((Boolean) -> Unit)? = null
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val selector = if (_cameraFacingFront.value) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                previewUseCase = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    previewUseCase
                )
                _isCameraActive.value = true
                onCameraBound?.invoke(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _isCameraActive.value = false
                onCameraBound?.invoke(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun unbindCamera() {
        try {
            cameraProvider?.unbindAll()
            _isCameraActive.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleCameraFacing(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _cameraFacingFront.value = !_cameraFacingFront.value
        bindCameraPreview(lifecycleOwner, previewView)
    }
}
