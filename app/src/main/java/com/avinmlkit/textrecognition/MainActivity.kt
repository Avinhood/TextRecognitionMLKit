package com.avinmlkit.textrecognition

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.avinmlkit.textrecognition.databinding.ActivityMainBinding
import com.avinmlkit.textrecognition.textdetector.BitmapUtils
import com.avinmlkit.textrecognition.textdetector.TextRecognitionProcessor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val TAG = "TextRecProcessor"
    private var imageProcessor: TextRecognitionProcessor? = null
    private var imageUri: Uri? = null
//    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    lateinit var binding: ActivityMainBinding

    private val REQUEST_IMAGE_CAPTURE=1

    private var imageBitmap: Bitmap? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)

        binding.apply {

            captureImage.setOnClickListener {
//                takeImage()
                startCameraIntentForResult()
                textView.text = ""
            }

        }
    }


//    private fun takeImage(){
//        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        try {
//            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE)
//        }
//        catch (e:Exception){
//            e.printStackTrace()
//        }
//    }

    private fun startCameraIntentForResult() { // Clean up last time's image
        imageUri = null
        binding.imageView.setImageBitmap(null)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(
                takePictureIntent,
                REQUEST_IMAGE_CAPTURE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==REQUEST_IMAGE_CAPTURE && resultCode== RESULT_OK){
//            val extras: Bundle? = data?.extras
//            imageBitmap= extras?.get("data") as Bitmap
//            if (imageBitmap!=null) {
//                binding.imageView.setImageBitmap(imageBitmap)
//                processImage()
//            }
            tryReloadAndDetectInImage()
        }
    }


    private fun tryReloadAndDetectInImage() {
        Log.d(
            TAG,
            "Try reload and detect image"
        )
        try {
            if (imageUri == null) {
                return
            }


            val imageBitmap = BitmapUtils.getBitmapFromContentUri(contentResolver, imageUri) ?: return


            binding.imageView.setImageBitmap(imageBitmap)
            processImage(imageBitmap)
        } catch (e: IOException) {
            Log.e(TAG, "Error retrieving saved image")
            imageUri = null
        }
    }

    private fun processImage(imageBitmap: Bitmap){
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
        imageProcessor = TextRecognitionProcessor(this, TextRecognizerOptions.Builder().build())
        Log.i(TAG, "Using on-device Text recognition Processor for English")

        if (imageBitmap!=null) {

            val image = imageBitmap?.let {
                InputImage.fromBitmap(it, 0)
            }

            if (image != null) {
                imageProcessor?.detectInImage(image)?.
                addOnSuccessListener { visionText ->
                    binding.textView.text = visionText.text
                }?.addOnFailureListener{
                    it.printStackTrace()
                }
            }

//            image?.let {
//                recognizer.process(it)
//                    .addOnSuccessListener { visionText ->
//
//                        binding.textView.text = visionText.text
//
//                    }
//                    .addOnFailureListener { e ->
//
//                    }
//            }

        }

        else{

            Toast.makeText(this, "Please select photo", Toast.LENGTH_SHORT).show()

        }


    }

    override fun onPause() {
        super.onPause()
        imageProcessor?.run { this.stop() }
    }


    override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }


}