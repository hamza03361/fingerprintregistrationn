package com.example.fingerprint

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView

class CustomImageDialog(context: Context) : Dialog(context) {

    private lateinit var imageView: ImageView
    private lateinit var closeButton: Button
    private var imageBitmap: Bitmap? = null
    private var dismissListener: DismissListener? = null

    interface DismissListener {
        fun onDismiss()
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageBitmap = bitmap
    }

    fun setDismissListener(listener: DismissListener) {
        dismissListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom_image)

        imageView = findViewById(R.id.dialogImageView)
        closeButton = findViewById(R.id.closeButton)

        // Set the image to the ImageView
        imageBitmap?.let {
            imageView.setImageBitmap(it)
        }

        closeButton.setOnClickListener {
            dismiss()
            dismissListener?.onDismiss()
        }
    }
}
