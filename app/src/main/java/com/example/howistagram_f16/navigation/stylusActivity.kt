package com.example.howistagram_f16.navigation

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.example.howistagram_f16.PaintView
import com.example.howistagram_f16.PaintView.Companion.colorList
import com.example.howistagram_f16.PaintView.Companion.currentBrush
import com.example.howistagram_f16.PaintView.Companion.pathList
import com.example.howistagram_f16.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class stylusActivity : AppCompatActivity() {
    companion object{
        var path = Path()
        var paintBrush = Paint()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stylus)
        supportActionBar?.hide()

        var redBtn = findViewById<ImageButton>(R.id.redColor)
        var blueBtn = findViewById<ImageButton>(R.id.blueColor)
        var blackBtn = findViewById<ImageButton>(R.id.blackColor)
        var eraser = findViewById<ImageButton>(R.id.whiteColor)

        redBtn.setOnClickListener {
            Toast.makeText(this, "Red !", Toast.LENGTH_SHORT).show()
            paintBrush.color = Color.RED
            currentColor(paintBrush.color)
        }

        blueBtn.setOnClickListener {
            Toast.makeText(this, "Blue !", Toast.LENGTH_SHORT).show()
            paintBrush.color = Color.BLUE
            currentColor(paintBrush.color)
        }

        blackBtn.setOnClickListener {
            Toast.makeText(this, "Black !", Toast.LENGTH_SHORT).show()
            paintBrush.color = Color.BLACK
            currentColor(paintBrush.color)
        }

        eraser.setOnClickListener {
            Toast.makeText(this, "Eraser !", Toast.LENGTH_SHORT).show()
            pathList.clear()
            colorList.clear()
            path.reset()
        }

        /////////////  SCREENSHOT CODE START  ///////////////////////////////////////////////////////////////////////////
        ///////// https://www.geeksforgeeks.org/how-to-capture-screenshot-of-a-view-and-save-it-to-gallery-in-android/
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        val cardView = findViewById<CardView>(R.id.cardView)

        // on click of this button it will capture
        // screenshot and save into gallery
        val captureButton = findViewById<Button>(R.id.SCbutton)
        captureButton.setOnClickListener {
            // get the bitmap of the view using
            // getScreenShotFromView method it is
            // implemented below
            val bitmap = getScreenShotFromView(cardView)

            // if bitmap is not null then
            // save it to gallery
            if (bitmap != null) {
                saveMediaToStorage(bitmap)
            }
        }
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            // inflate screenshot object
            // with Bitmap.createBitmap it
            // requires three parameters
            // width and height of the view and
            // the background color
            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("GFG", "Failed to capture screenshot because:" + e.message)
        }
        // return the bitmap
        return screenshot
    }


    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }
    //////////////////////////////SCREENSHOT CODE END///////////////////////

    private fun currentColor(color: Int){
        currentBrush = color
        path = Path()
    }
}