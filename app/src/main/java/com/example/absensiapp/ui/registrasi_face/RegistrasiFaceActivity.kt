package com.example.absensiapp.ui.registrasi_face

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.absensiapp.R
import com.example.absensiapp.Drawing.MultiBoxTracker
import com.example.absensiapp.Drawing.OverlayView
import com.example.absensiapp.Face_Recognition.FaceClassifier
import com.example.absensiapp.Face_Recognition.TFLiteFaceRecognition
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RegistrasiFaceActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var registerButton: Button
    private lateinit var switchCameraButton: Button
    private lateinit var overlayView: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private var isUsingFrontCamera = true

    private val detector: FaceDetector by lazy {
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
        FaceDetection.getClient(highAccuracyOpts)
    }

    private var faceClassifier: FaceClassifier? = null
    private var registerFace = false
    private var isProcessingFrame = false
    private lateinit var tracker: MultiBoxTracker

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 121
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrasi_face)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.tracking_overlay)
        registerButton = findViewById(R.id.registerButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)

        cameraExecutor = Executors.newSingleThreadExecutor()
        tracker = MultiBoxTracker(this)

        // Initialize FACE Recognition
        CoroutineScope(Dispatchers.Main).launch {
            try {
                faceClassifier = TFLiteFaceRecognition.create(
                    assets,
                    "mobile_face_net.tflite",
                    112, // pastikan input size sesuai
                    false,
                    applicationContext
                )
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Classifier could not be initialized", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        checkAndRequestPermissions()

        registerButton.setOnClickListener {
            registerFace = true
        }

        switchCameraButton.setOnClickListener {
            isUsingFrontCamera = !isUsingFrontCamera
            bindCameraUseCases()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        } else {
            bindCameraUseCases()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                bindCameraUseCases()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindCameraUseCases() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(if (isUsingFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy -> processImageProxy(imageProxy) })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(imageProxy: ImageProxy) {
        if (isProcessingFrame) {
            imageProxy.close()
            return
        }
        isProcessingFrame = true

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = imageProxy.image ?: run {
            imageProxy.close()
            isProcessingFrame = false
            return
        }

        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.RED
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 5.0f
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                processFaces(faces, bitmap, image.width, image.height)
                imageProxy.close()
                isProcessingFrame = false
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                imageProxy.close()
                isProcessingFrame = false
            }
    }

    private fun processFaces(faces: List<Face>, bitmap: Bitmap, imageWidth: Int, imageHeight: Int) {
        if (faces.isEmpty()) {
            return
        }

        val face = faces.first()
        val bounds = face.boundingBox

        // Pastikan kotak batas berada dalam dimensi bitmap
        val left = bounds.left.coerceAtLeast(0)
        val top = bounds.top.coerceAtLeast(0)
        val right = bounds.right.coerceAtMost(bitmap.width)
        val bottom = bounds.bottom.coerceAtMost(bitmap.height)

        val width = right - left
        val height = bottom - top

        if (width <= 0 || height <= 0) {
            return
        }

        // Potong wajah dari bitmap asli
        val crop = Bitmap.createBitmap(bitmap, left, top, width, height)

        // Ubah ukuran bitmap sesuai dengan ukuran yang diharapkan oleh model (112x112 dalam kasus ini)
        val scaledBitmap = Bitmap.createScaledBitmap(crop, 112, 112, false)

        // Gunakan bitmap yang telah diubah ukurannya langsung dengan fungsi recognizeImage
        val result = faceClassifier?.recognizeImage(scaledBitmap, registerFace)

        val rectF = RectF(bounds)

        // Calculate scale to transform bounding box to screen coordinates
        val scaleX = overlayView.width / imageWidth.toFloat()
        val scaleY = overlayView.height / imageHeight.toFloat()

        // Apply scale to bounding box
        rectF.left *= scaleX
        rectF.top *= scaleY
        rectF.right *= scaleX
        rectF.bottom *= scaleY

        val recognition = FaceClassifier.Recognition(face.trackingId.toString(), "", 0f, rectF)

        tracker.setFrameConfiguration(imageWidth, imageHeight, 0)

        // Bersihkan callback sebelumnya sebelum menambahkan yang baru
        overlayView.clearCallbacks()

        tracker.trackResults(listOf(recognition), System.currentTimeMillis())
        overlayView.addCallback(object : OverlayView.DrawCallback {
            override fun drawCallback(canvas: Canvas) {
                tracker.draw(canvas)
            }
        })
        overlayView.postInvalidate()

        if (registerFace) {
            showRegisterDialog(scaledBitmap, result!!)
        }
    }

    private fun showRegisterDialog(bitmap: Bitmap, recognition: FaceClassifier.Recognition) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(R.layout.dialog_register)
            .create()

        dialog.setOnShowListener {
            val imageView: ImageView = dialog.findViewById(R.id.dialog_image)!!
            val registerButton: Button = dialog.findViewById(R.id.dialog_register_button)!!

            imageView.setImageBitmap(bitmap)
            registerButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    faceClassifier?.register("Face", recognition)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegistrasiFaceActivity, "Face Registered Successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        detector.close()
    }
}