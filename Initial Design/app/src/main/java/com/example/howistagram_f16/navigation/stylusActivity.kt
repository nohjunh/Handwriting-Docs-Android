package com.example.howistagram_f16.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.howistagram_f16.PaintView.Companion.colorList
import com.example.howistagram_f16.PaintView.Companion.pathList
import com.example.howistagram_f16.R
import com.example.howistagram_f16.databinding.ActivityStylusBinding
import com.google.android.material.card.MaterialCardView
import com.theartofdev.edmodo.cropper.CropImage
import java.io.ByteArrayOutputStream
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
    var xValue= 0
    var yValue= 0
    var URII : Uri? = null
    var PICK_IMAGE_FROM_ALBUM = 0
    var PICK_IMAGE_FROM_ALBUM_Vaild = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        val captureButton = findViewById<Button>(R.id.SCbutton)
        val MouseCaptureButton = findViewById<Button>(R.id.MouseCapture)
        val resetButton = findViewById<Button>(R.id.resetPen)

        var currentAction= ""
        detector = GestureDetector(this,object : GestureDetector.SimpleOnGestureListener() {
            //화면을 손가락으로 오랫동안 눌렀을 경우
            override fun onLongPress(e: MotionEvent) {
                xValue= e.getX().toInt()
                yValue= e.getY().toInt()
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
        // denpendency로 가져온 paintView용 글자크기 설정
        // binding.paintVIEW1.setStrokeWidth(4f)
        ////////////////////////////////////////////////////

        /////////////  SCREENSHOT CODE START  ///////////////////////////////////////////////////////////////////////////
        ///////// https://www.geeksforgeeks.org/how-to-capture-screenshot-of-a-view-and-save-it-to-gallery-in-android/
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        val cardView= findViewById<MaterialCardView>(R.id.cardView)

//        val gestureListener = View.OnTouchListener(function = { view,event ->
//            detector!!.onTouchEvent(event)
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                when (currentAction) {
//                    "isLongPressed" -> {
//                        var viewNamelength = view.toString().length
//                        var unionName = view.toString().substring(
//                            viewNamelength - 11,
//                            viewNamelength - 1
//                        ) + "card"
//                        //Toast.makeText(this, "$unionName", Toast.LENGTH_SHORT).show()
//                        var resID = resources.getIdentifier("$unionName","id",packageName)
//                        val bitmap = getScreenShotFromView(findViewById(resID))
//                        if (bitmap != null) {
//                            saveMediaToStorage(bitmap)
//                        }
//                        currentAction = ""
//                    }
//                }
//            }
//            false
//        })
        //binding.paintVIEW1.setOnTouchListener(gestureListener)
        //binding.paintVIEW2.setOnTouchListener(gestureListener)

        // on click of this button it will capture
        // screenshot and save into gallery
//        captureButton.setOnClickListener {
//            // get the bitmap of the view using
//            // getScreenShotFromView method it is
//            // implemented below
//            val bitmap = getScreenShotFromView(cardView)
//            // if bitmap is not null then
//            // save it to gallery
//            if (bitmap != null) {
//                saveMediaToStorage(bitmap)
//            }
//        }
        val gestureListener = View.OnTouchListener(function = { view,event ->
            detector!!.onTouchEvent(event)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                when (currentAction) {
                    "isLongPressed" -> {
                        MouseCaptureButton.performClick()
                        currentAction=""
                    }
                }
            }
            false
        })
        binding.paintVIEW1.setOnTouchListener(gestureListener)

        // 수정된 화면 전체 캡쳐
        captureButton.setOnClickListener {
            val bitmap = getScreenShotFromView(cardView)
            if (bitmap != null) {
                saveMediaToStorage(bitmap)
            }
        }

        // 마우스 왼쪽 클릭 시 강제 버튼이벤트를 발생시키기 위함.
        MouseCaptureButton.setOnClickListener {
            val bitmap = getScreenShotFromView(cardView)
            URII = getImageUriFromBitmap(this,bitmap!!)

        // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(URII)
                .start(this);
        }

        resetButton.setOnClickListener{
            // 필기체 초기화
            pathList.clear()
            colorList.clear()
            path.reset()
        }
    }
    // Cropper Activity execute GoGO!!
    override fun onActivityResult(requestCode: Int,resultCode: Int,data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                var bitmap: Bitmap? = null
                try {
                    bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),Uri.parse(
                        resultUri.toString()
                    ))
                } catch (e: java.lang.Exception) {
                    //handle exception
                }
                if (bitmap != null) {
                    saveMediaToStorage(bitmap)
                }
                Toast.makeText(this,"Captured View and saved to Gallery",Toast.LENGTH_SHORT).show()

                // 필기체 초기화
                pathList.clear()
                colorList.clear()
                path.reset()

                // 변환된 텍스트 띄어줌
                val Mainlayout = findViewById<MaterialCardView>(R.id.cardView)
                val texts1 = TextView(this)
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.setMargins(xValue+30, yValue+30,0,0)
                texts1.setLayoutParams(layoutParams)
                texts1.setText("변환된 Text")
                texts1.setTextColor(Color.BLACK)
                texts1.setTextSize(TypedValue.COMPLEX_UNIT_SP,26F)
                Mainlayout.addView(texts1)

                // URI을 얻기위해 임시로 만든 image를 mediastore에서 삭제시킴.
                contentResolver.delete(URII!!, null, null)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }

        var photoUrl : Uri? = null
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                // 선택된 이미지의 경로 값 저장
                photoUrl= data?.data // 변수에다가 path data 담아줌
                binding.ImageUpdate.setImageURI(photoUrl) // ImageView에다가 이미지 GOGO~
            }
        }
    }

    fun getImageUriFromBitmap(context: Context,bitmap: Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)
        // URI을 Bitmap으로 바꾸기위해 mediastore에 "Title"이라는 name의 image로 저장해놓지만
        // 후에 이 image를 삭제함.
        val path = MediaStore.Images.Media.insertImage(context.contentResolver,bitmap,"Title",null)
        return Uri.parse(path.toString())
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
            screenshot = Bitmap.createBitmap(
                v.measuredWidth,
                v.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("GFG","Failed to capture screenshot because:" + e.message)
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
                    put(MediaStore.MediaColumns.DISPLAY_NAME,filename)
                    put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir,filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,it)
            Toast.makeText(this,"Captured View and saved to Gallery",Toast.LENGTH_SHORT).show()
        }
    }
    //////////////////////////////SCREENSHOT CODE END///////////////////////

}