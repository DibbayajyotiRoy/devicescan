package com.devicelens.app.domain.scanner

import android.content.Context
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IrDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val brightSpotThreshold = 220
    private var cameraProvider: ProcessCameraProvider? = null

    fun startAnalysis(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onBrightSpotsDetected: (List<Pair<Float, Float>>) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(context)
            ) { imageProxy ->
                val yPlane = imageProxy.planes[0]
                val buffer = yPlane.buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)

                val brightSpots = mutableListOf<Pair<Float, Float>>()
                val width = imageProxy.width
                val height = imageProxy.height

                for (y in 0 until height step 8) {
                    for (x in 0 until width step 8) {
                        val idx = y * yPlane.rowStride + x
                        if (idx < data.size) {
                            val luminance = data[idx].toInt() and 0xFF
                            if (luminance > brightSpotThreshold) {
                                brightSpots.add(
                                    Pair(x.toFloat() / width, y.toFloat() / height)
                                )
                            }
                        }
                    }
                }

                onBrightSpotsDetected(brightSpots)
                imageProxy.close()
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                // Camera binding failed
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopAnalysis() {
        cameraProvider?.unbindAll()
    }
}
