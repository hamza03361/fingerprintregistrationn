package com.example.fingerprint

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView

class MainActivity : ComponentActivity() {

    private lateinit var getstartedbutton: Button
    private lateinit var loadingOverlay: RelativeLayout
    private lateinit var animationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)

        // Set the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.themecolor) // Change to your color resource
        }

        animationView = findViewById(R.id.animationView)

        // Initialize and set up the button
        getstartedbutton = findViewById(R.id.getstarted)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        getstartedbutton.setOnClickListener {
            // Check permissions
            if (checkPermissions()) {
                startFaceRegistrationActivity()
            } else {
                requestPermissions()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        val readStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                audioPermission == PackageManager.PERMISSION_GRANTED &&
                readStoragePermission == PackageManager.PERMISSION_GRANTED &&
                writeStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startFaceRegistrationActivity() {
        // Show the animation
        animationView.visibility = View.VISIBLE

        // Simulate some process
        // For demonstration, we will use a Handler to hide the animation after 2 seconds and then start the new activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Hide the animation
            animationView.visibility = View.GONE

            // Start the new activity
            val intent = Intent(this, Fingerprintregistration::class.java)
            startActivity(intent)
            // Optionally, finish the current activity if you don't want the user to return to it
            finish()
        }, 1000) // 2000 milliseconds = 2 seconds
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startFaceRegistrationActivity()
            } else {
                // Show a message to the user explaining why the permissions are necessary
                AlertDialog.Builder(this)
                    .setTitle("Permissions required")
                    .setMessage("This app requires Camera, Audio, and Storage permissions to function properly.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    override fun onBackPressed() {
        // Create an AlertDialog with the custom style
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Exit")
        builder.setMessage("Are you sure you want to exit?")

        // Set up the buttons
        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
            // If the user confirms, close the app
            finishAffinity()
            super.onBackPressed()
        }
        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            // If the user cancels, dismiss the dialog
            dialog.dismiss()
        }

        // Show the dialog
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}
