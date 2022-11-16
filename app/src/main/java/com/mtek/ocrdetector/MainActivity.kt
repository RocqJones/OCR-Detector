package com.mtek.ocrdetector

import android.R.attr
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.mtek.ocrdetector.databinding.ActivityMainBinding
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        try {
            binding.btnImage.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraImage()
                }
            }

            binding.btnUploadFromGallery.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    galleryImage()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun cameraImage() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 123)
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 123)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun galleryImage() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 456)
            } else {
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, 456)
                // selectImageIntent
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val selectImageIntent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val imageStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(imageStream)

            binding.image.setImageBitmap(bitmap)
            processCapturedImage(bitmap)
        } else {
            Toast.makeText(this, "Image selection cancelled!", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                123 -> {
                    // Extract the image
                    val bundle = data?.extras
                    val bitmap = bundle!!["data"] as Bitmap?
                    binding.image.setImageBitmap(bitmap)
                    processCapturedImage(bitmap)
                }
                456 -> {
                    val selectedImage = data!!.data
                    if (selectedImage != null) {
                        val imageStream: InputStream? = contentResolver.openInputStream(selectedImage)
                        val bitmap = BitmapFactory.decodeStream(imageStream)
                        binding.image.setImageBitmap(bitmap)
                        processCapturedImage(bitmap)
                    } else {
                        Toast.makeText(this, "Failed to get image!", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    Toast.makeText(this, "Error occurred!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processCapturedImage(bitmap: Bitmap?) {
        try {
            // Create a FirebaseVisionImage object from your image/bitmap.
            val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap!!)
            val firebaseVision = FirebaseVision.getInstance()
            val firebaseVisionTextRecognizer = firebaseVision.onDeviceTextRecognizer

            // Process the Image
            val task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
            task.addOnSuccessListener { firebaseVisionText: FirebaseVisionText ->
                //Set recognized text from image in our TextView
                val text = firebaseVisionText.text
                Log.d(TAG, "rawTxt: $text")
                binding.txtImage.text = text
            }

            task.addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}