package com.tupausa.utils

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class CameraHelper(
    private val context: Context,
    private val owner: LifecycleOwner,
    private val onPhotoTaken: (File) -> Unit,
    private val onError: (Exception) -> Unit
) {
    private var imageCapture: ImageCapture? = null

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Configuramos para capturar imagen
            imageCapture = ImageCapture.Builder().build()

            // Usamos cámara frontal (Selfie)
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                // Vinculamos al ciclo de vida de la actividad
                cameraProvider.bindToLifecycle(
                    owner, cameraSelector, imageCapture
                )
            } catch (exc: Exception) {
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Crear archivo temporal
        val photoFile = File(
            context.filesDir, // Guardamos en almacenamiento interno de la app
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    onError(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onPhotoTaken(photoFile)
                }
            }
        )
    }
}