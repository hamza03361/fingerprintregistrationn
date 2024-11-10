package com.example.fingerprint

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.activity.ComponentActivity
import java.util.Calendar
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.util.PatternsCompat
import java.io.File
import java.util.*

class Registrationform1 : ComponentActivity() {

    private lateinit var nextButton: Button
    private lateinit var smallImageViewContainer: FrameLayout

    private lateinit var capturedImageView1: ImageView
    private lateinit var capturedImageView2: ImageView
    private lateinit var capturedImageView3: ImageView
    private lateinit var capturedImageView4: ImageView
    private lateinit var capturedImageView5: ImageView

    private var capturedImagePaths: ArrayList<String>? = null  // Use ArrayList to store file paths

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrationform1)

        // Set up the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.secondthemecolor)
        }

        // Initialize captured image views
        capturedImageView1 = findViewById(R.id.capturedImageView1)
        capturedImageView2 = findViewById(R.id.capturedImageView2)
        capturedImageView3 = findViewById(R.id.capturedImageView3)
        capturedImageView4 = findViewById(R.id.capturedImageView4)
        capturedImageView5 = findViewById(R.id.capturedImageView5)

        // Initialize capturedImagePaths
        capturedImagePaths = intent.getStringArrayListExtra("capturedImagePaths")

        // Log the received image paths
        Log.d("Registrationform1", "Received image paths: $capturedImagePaths")

        // Display images in the ImageViews
        capturedImagePaths?.let {
            if (it.size > 0) {
                Log.d("Registrationform1", "Loading image 1: ${it[0]}")
                loadImageIntoView(it[0], capturedImageView1)
            }
            if (it.size > 1) {
                Log.d("Registrationform1", "Loading image 2: ${it[1]}")
                loadImageIntoView(it[1], capturedImageView2)
            }
            if (it.size > 2) {
                Log.d("Registrationform1", "Loading image 3: ${it[2]}")
                loadImageIntoView(it[2], capturedImageView3)
            }
            if (it.size > 3) {
                Log.d("Registrationform1", "Loading image 4: ${it[3]}")
                loadImageIntoView(it[3], capturedImageView4)
            }
            if (it.size > 4) {
                Log.d("Registrationform1", "Loading image 5: ${it[4]}")
                loadImageIntoView(it[4], capturedImageView5)
            }
        }

        // Initialize UI elements
        smallImageViewContainer = findViewById(R.id.smallImageViewContainer)
        nextButton = findViewById(R.id.nextButton)

        smallImageViewContainer.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Handle next button click
        nextButton.setOnClickListener {

        }
    }

    private fun loadImageIntoView(imagePath: String, imageView: ImageView) {
        val file = File(imagePath)
        if (file.exists()) {
            Log.d("Registrationform1", "File exists: $imagePath")
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap != null) {
                Log.d("Registrationform1", "Bitmap loaded successfully for path: $imagePath")
                val rotatedBitmap = getRotatedBitmap(bitmap, imagePath)
                imageView.setImageBitmap(rotatedBitmap)
            } else {
                Log.d("Registrationform1", "Failed to load bitmap for path: $imagePath")
            }
        } else {
            Log.d("Registrationform1", "File does not exist: $imagePath")
        }
    }

    private fun getRotatedBitmap(bitmap: Bitmap, imagePath: String): Bitmap {
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigatetomainactivity()
    }

    private fun navigatetomainactivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
