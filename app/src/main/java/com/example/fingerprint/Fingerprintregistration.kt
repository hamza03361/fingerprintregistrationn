package com.example.fingerprint

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Fingerprintregistration : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var middlesmallImageView: ImageView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var smallImageViewContainer: FrameLayout
    private lateinit var captureButton: Button
    private lateinit var progressBar: ProgressBar
    private val capturedImagePaths = mutableListOf<String>()
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fingerprintregistration)

        previewView = findViewById(R.id.previewView)
        middlesmallImageView = findViewById(R.id.middlesmallImageView)
        captureButton = findViewById(R.id.Capturebutton)
        progressBar = findViewById(R.id.progressBar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.secondthemecolor)
        }

        smallImageViewContainer = findViewById(R.id.smallImageViewContainer)
        smallImageViewContainer.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        captureButton.setOnClickListener {
            imageCapture?.let { capturePhoto(it, "captured_image_${capturedImagePaths.size + 1}") }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    @OptIn(ExperimentalCamera2Interop::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val cameraSelector = selectMacroCameraSelector(cameraProvider) ?: CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageCaptureBuilder = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(ImageCapture.FLASH_MODE_ON)
                .setJpegQuality(100)

            val macroMode = cameraSelector != CameraSelector.DEFAULT_BACK_CAMERA

            if (macroMode) {
                Camera2Interop.Extender(imageCaptureBuilder)
                    .setCaptureRequestOption(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_MACRO
                    )
            } else {
                Camera2Interop.Extender(imageCaptureBuilder)
                    .setCaptureRequestOption(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
            }

            imageCapture = imageCaptureBuilder.build()

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                enableAutoFocus(camera)
                setupTouchToFocus(camera) // Add touch-to-focus functionality
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupTouchToFocus(camera: Camera) {
        previewView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val meteringPoint = previewView.meteringPointFactory.createPoint(event.x, event.y)
                val focusMeteringAction = FocusMeteringAction.Builder(meteringPoint, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(5, TimeUnit.SECONDS)
                    .build()

                camera.cameraControl.startFocusAndMetering(focusMeteringAction).addListener({
                    Log.d(TAG, "Focus and metering started successfully at (${event.x}, ${event.y})")
                }, ContextCompat.getMainExecutor(this))
                true
            } else {
                false
            }
        }
    }

    private fun selectMacroCameraSelector(cameraProvider: ProcessCameraProvider): CameraSelector? {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList

        for (cameraId in cameraIds) {
            try {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val availableCapabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                val hasMacroLens = availableCapabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) == true

                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasMacroLens && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera characteristics for cameraId $cameraId", e)
            }
        }

        return null
    }

    private fun enableAutoFocus(camera: Camera) {
        val cameraControl = camera.cameraControl

        // Enable continuous auto-focus
        val focusMeteringAction = FocusMeteringAction.Builder(
            previewView.meteringPointFactory.createPoint(0.5f, 0.5f),
            FocusMeteringAction.FLAG_AF
        ).setAutoCancelDuration(5, TimeUnit.SECONDS) // Auto-cancel after 5 seconds if not successful
            .build()

        cameraControl.startFocusAndMetering(focusMeteringAction).addListener({
            Log.d(TAG, "Focus and metering started successfully")
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto(imageCapture: ImageCapture, filename: String) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImagePaths.add(file.absolutePath)

                    // Calculate NFIQ score
                    val nfiqScore = calculateNFIQScore(file.absolutePath)
                    Log.d(TAG, "NFIQ Score: $nfiqScore")

                    // Correct image orientation and show dialog with the captured image
                    correctImageOrientation(file.absolutePath) { correctedBitmap ->
                        showImagePreviewDialog(correctedBitmap, file.absolutePath, object : DialogDismissListener {
                            override fun onDialogDismissed() {
                                // Optionally, do something after dialog dismissal
                            }
                        })
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }


    // Native method declaration
    external fun calculateNFIQScore(imagePath: String): Int



    private fun correctImageOrientation(imagePath: String, callback: (Bitmap) -> Unit) {
        val bitmapOptions = BitmapFactory.Options().apply {
            inSampleSize = 1  // Adjust this based on your image quality and memory considerations
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, bitmapOptions)
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        }

        val correctedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        callback(correctedBitmap)
    }

    // Show a dialog to preview the image
    private fun showImagePreviewDialog(imageBitmap: Bitmap, imagePath: String, listener: DialogDismissListener) {
        val dialog = CustomImageDialog(this)
        dialog.setImageBitmap(imageBitmap)
        dialog.setDismissListener(object : CustomImageDialog.DismissListener {
            override fun onDismiss() {
                listener.onDialogDismissed()
            }
        })
        dialog.show()
    }

    interface DialogDismissListener {
        fun onDialogDismissed()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                finish() // Close the app if permissions are not granted
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "FingerprintRegistration"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        init {
            // Load the native library
            System.loadLibrary("nfiq")
        }
    }
}
