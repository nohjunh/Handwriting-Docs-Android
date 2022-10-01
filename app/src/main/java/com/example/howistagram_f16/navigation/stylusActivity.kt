package com.example.howistagram_f16.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.example.howistagram_f16.PaintView
import com.example.howistagram_f16.PaintView.Companion.colorList
import com.example.howistagram_f16.PaintView.Companion.currentBrush
import com.example.howistagram_f16.PaintView.Companion.pathList
import com.example.howistagram_f16.R
import com.example.howistagram_f16.databinding.ActivityStylusBinding
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Suppress("DEPRECATION")
class stylusActivity : AppCompatActivity() {
    private val binding by lazy { ActivityStylusBinding.inflate(layoutInflater) }

    companion object{
        var path = Path()
        var paintBrush = Paint()
    }

    var detector: GestureDetector? = null //무슨 제스쳐를 했는지 감지

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        val captureButton = findViewById<Button>(R.id.SCbutton)

        var currentAction= ""

        detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            //화면을 손가락으로 오랫동안 눌렀을 경우
            override fun onLongPress(e: MotionEvent) {
                currentAction = "isLongPressed"
                super.onLongPress(e)
            }
        })
//////////////////////맨처음 했던 방법 삭제해도됨.
//        var redBtn = findViewById<ImageButton>(R.id.redColor)
//        var blueBtn = findViewById<ImageButton>(R.id.blueColor)
//        var blackBtn = findViewById<ImageButton>(R.id.blackColor)
//        var eraser = findViewById<ImageButton>(R.id.whiteColor)
//
//        redBtn.setOnClickListener {
//            Toast.makeText(this, "Red !", Toast.LENGTH_SHORT).show()
//            paintBrush.color = Color.RED
//            currentColor(paintBrush.color)
//        }
//
//        blueBtn.setOnClickListener {
//            Toast.makeText(this, "Blue !", Toast.LENGTH_SHORT).show()
//            paintBrush.color = Color.BLUE
//            currentColor(paintBrush.color)
//        }
//
//        blackBtn.setOnClickListener {
//            Toast.makeText(this, "Black !", Toast.LENGTH_SHORT).show()
//            paintBrush.color = Color.BLACK
//            currentColor(paintBrush.color)
//        }
//
//        eraser.setOnClickListener {
//            Toast.makeText(this, "Eraser !", Toast.LENGTH_SHORT).show()
//            pathList.clear()
//            colorList.clear()
//            path.reset()
//        }

        //////글씨크기, 바인딩 하나만 해줬는데 왜 다 적용??/////
        binding.paintVIEW1.setStrokeWidth(4f)
        //////////

        /////////////  SCREENSHOT CODE START  ///////////////////////////////////////////////////////////////////////////
        ///////// https://www.geeksforgeeks.org/how-to-capture-screenshot-of-a-view-and-save-it-to-gallery-in-android/
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        val cardView= findViewById<ImageView>(R.id.docsImage)
        //val TouchViewcardView = findViewById<MaterialCardView>(R.id.cardView2)
        val gestureListener = View.OnTouchListener(function = { view, event ->
            detector!!.onTouchEvent(event)
            if(event.getAction() == MotionEvent.ACTION_UP) {
                when (currentAction) {
                    "isLongPressed" -> {
                        var viewNamelength= view.toString().length
                        var unionName= view.toString().substring(viewNamelength-11, viewNamelength-1)+"card"
                        //Toast.makeText(this, "$unionName", Toast.LENGTH_SHORT).show()
                        var resID = resources.getIdentifier("$unionName", "id", packageName)
                        val bitmap = getScreenShotFromView(findViewById(resID))
                        if (bitmap != null) {
                            saveMediaToStorage(bitmap)
                        }
                        currentAction = ""
                    }
                }
            }
            false
        })
        binding.paintVIEW1.setOnTouchListener(gestureListener)
        binding.paintVIEW2.setOnTouchListener(gestureListener)

        // on click of this button it will capture
        // screenshot and save into gallery
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

    private fun printString() {
        Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()
    }

    fun concat(s1: String, s2: String): String {
        return s1 + s2
    }
}