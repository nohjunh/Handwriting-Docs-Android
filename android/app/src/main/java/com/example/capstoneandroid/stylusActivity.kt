package com.example.capstoneandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.capstoneandroid.DTO.CraftResponseDTO
import com.example.capstoneandroid.PaintView.Companion.colorList
import com.example.capstoneandroid.PaintView.Companion.currentBrush
import com.example.capstoneandroid.PaintView.Companion.pathList
import com.example.capstoneandroid.databinding.ActivityStylusBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.theartofdev.edmodo.cropper.CropImage
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.reflect.Field
import kotlin.math.ceil


@Suppress("DEPRECATION")
class stylusActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    private val binding by lazy { ActivityStylusBinding.inflate(layoutInflater) }

    companion object {
        var path = Path()
        var paintBrush = Paint()
    }

    // 제스처 이벤트 감지하는 변수
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var scaleFactor = 1.0f
    private lateinit var Mainlayout: MaterialCardView

    var detector: GestureDetector? = null //무슨 제스쳐를 했는지 감지
    var xValue = 0
    var yValue = 0
    var URII: Uri? = null
    var PICK_IMAGE_FROM_ALBUM = 0
    var PICK_IMAGE_FROM_ALBUM_Vaild = 0
    var transTextSize = 26
    var code = 0
    var file = ""
    private val user = FirebaseAuth.getInstance().currentUser
    private val storage = Firebase.storage

    private var predictor: Predictor = Predictor()

    val retrofit: Retrofit = Retrofit.Builder().baseUrl("http://192.168.0.16:5000")
        .addConverterFactory(GsonConverterFactory.create()).build();
    val service: RetrofitService = retrofit.create(RetrofitService::class.java);
    var BoxResult: CraftResponseDTO.BoxInfo? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        if (predictor.isLoaded()) {
            predictor.releaseModel()
        }

        predictor.init(
            this@stylusActivity,
            "models/ko_PP-OCRv3",
            "labels/korean_dict.txt",
            0,
            4,
            "LITE_POWER_HIGH",
            960,
            0.1F
        )

        Mainlayout = findViewById(R.id.cardView)

        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        // POPUP
        binding.popupID.setOnClickListener { showPopup(binding.popupID) }
        // PEN_POPUP
        binding.penColor.setOnClickListener { showPopupColor(binding.penColor) }
        // Setting_POPUP
        binding.StylusSetting.setOnClickListener { showAlertSetting(binding.StylusSetting) }

        val secondIntent = intent
        val checkstylus = secondIntent.getIntExtra("번호", 0)
        val fileName = secondIntent.getStringExtra("이미지")
        code = checkstylus
        if (fileName != null) {
            file = fileName
        }

        // stylusAcitivity 실행될 때마다 필기체 초기화하고 시작
        pathList.clear()
        colorList.clear()
        path.reset()

        if (checkstylus == 8) { // Addfileon // 8이 아니라면, 빈 문서를 선택한 것(newCreate)이므로 첫 시작에 앨범 열 필요 X
            // Open the album
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        } else if (checkstylus == 7) {
            val storageRef = storage.reference
            val pathReference = storageRef.child(user!!.email + '/' + fileName)

            val ONE_MEGABYTE: Long = 1024 * 1024
            pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                binding.ImageUpdate.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
            }.addOnFailureListener {
                // Handle any errors
            }
        }


        val captureButton = findViewById<Button>(R.id.SCbutton)
        val MouseCaptureButton = findViewById<Button>(R.id.MouseCapture)
        val resetButton = findViewById<Button>(R.id.resetPen)

        // val StylusSettingButton = findViewById<Button>(R.id.StylusSetting)

        var currentAction = ""
        detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            //화면을 손가락으로 오랫동안 눌렀을 경우
            override fun onLongPress(e: MotionEvent) {
                xValue = e.getX().toInt()
                yValue = e.getY().toInt()
                currentAction = "isLongPressed"
                super.onLongPress(e)
            }
        })

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

        val cardView = findViewById<MaterialCardView>(R.id.cardView)

        val gestureListener = View.OnTouchListener(function = { view, event ->
            detector!!.onTouchEvent(event)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                when (currentAction) {
                    "isLongPressed" -> {
                        MouseCaptureButton.performClick()
                        currentAction = ""
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
            URII = getImageUriFromBitmap(this, bitmap!!)

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(URII)
                .start(this);
        }

        resetButton.setOnClickListener {
            // 필기체 초기화
            pathList.clear()
            colorList.clear()
            path.reset()
        }
    }

    override fun onDestroy() {
        if (predictor != null) {
            predictor.releaseModel()
        }
        super.onDestroy()
    }

    // 제스처 이벤트가 발생하면 실행되는 메소드
    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {

        // 제스처 이벤트를 처리하는 메소드를 호출
        mScaleGestureDetector!!.onTouchEvent(motionEvent)
        return true
    }
    // 제스처 이벤트를 처리하는 클래스
    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {

            scaleFactor *= scaleGestureDetector.scaleFactor

            // 최소 1, 최대 6배
            scaleFactor = Math.max(1f, Math.min(scaleFactor, 6f))

            // 이미지에 적용
            Mainlayout.scaleX = scaleFactor
            Mainlayout.scaleY = scaleFactor

            return true
        }
    }

    // Cropper Activity execute GoGO!!
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                var bitmap: Bitmap? = null
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), Uri.parse(
                            resultUri.toString()
                        )
                    )
                } catch (e: java.lang.Exception) {
                    //handle exception
                }
                if (bitmap != null) {
                    saveMediaToStorage(bitmap)
                    val texts1 = TextView(this)
                    val file = bitmapToByteArray(bitmap)
                    val baos = ByteArrayOutputStream()
                    val bitmapList: ArrayList<Bitmap> = ArrayList() // crop된 비트맵이 저장된 List
                    baos.write(file)
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)

                    // 필기체 초기화
                    pathList.clear()
                    colorList.clear()
                    path.reset()

                    service.getBoxInfo(body).enqueue(object : Callback<CraftResponseDTO.BoxInfo> {
                        override fun onResponse(call: Call<CraftResponseDTO.BoxInfo>, response: Response<CraftResponseDTO.BoxInfo>) {
                            if(response.isSuccessful){
                                BoxResult = response.body()
                                Log.d("YMC", "onResponse 성공: " + BoxResult?.bbox.toString());

                                bitmapList.clear()

                                val bounded = BoxResult!!.bbox.sortedWith(compareBy { it[0][0] })

                                for (i in bounded) {
                                    val cropX = i[0][0]
                                    val cropY = i[0][1]
                                    val cropWidth = i[1][0] - cropX
                                    val cropHeight = i[3][1] - cropY
                                    val cropBitmap = Bitmap.createBitmap(bitmap, ceil(cropX).toInt() - 10, ceil(cropY).toInt() - 13, ceil(cropWidth).toInt() + 10, ceil(cropHeight).toInt() + 13)
                                    bitmapList.add(cropBitmap)
                                    Log.i("cropX", cropX.toString())
                                }

                                predictor.setInputImages(bitmapList)
                                predictor.runModel(0, 0, 1)

                                // 변환된 텍스트 띄어줌
                                val Mainlayout = findViewById<MaterialCardView>(R.id.cardView)
                                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.gravity = Gravity.CENTER;
                                layoutParams.setMargins(xValue + 25, yValue + 25, 0, 0)
                                texts1.setLayoutParams(layoutParams)
                                texts1.setText(predictor.outputResult)
                                texts1.setTextColor(Color.BLACK)
                                texts1.setTextSize(TypedValue.COMPLEX_UNIT_SP, transTextSize.toFloat())
                                val typeface = Typeface.createFromAsset(
                                    assets,
                                    "asfont/opensanslight.otf"
                                ) // font 폴더내에 있는 opensanslight.otf 파일을 typeface로 설정
                                texts1.typeface = typeface // texts1는 TextView 변수

                                Mainlayout.addView(texts1)

                                // URI을 얻기위해 임시로 만든 image를 mediastore에서 삭제시킴.
                                contentResolver.delete(URII!!, null, null)
                            } else {
                                Log.d("YMC", "onResponse 실패")
                            }
                        }
                        override fun onFailure(call: Call<CraftResponseDTO.BoxInfo>, t: Throwable) {
                            Log.d("YMC", "onFailure 에러: " + t.message.toString());
                        }
                    })
                }

                Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT)
                    .show()
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }

        var photoUrl: Uri?
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                // 선택된 이미지의 경로 값 저장
                photoUrl = data?.data // 변수에다가 path data 담아줌
                binding.ImageUpdate.setImageURI(photoUrl) // ImageView에다가 이미지 GOGO~
            }
        }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        // URI을 Bitmap으로 바꾸기위해 mediastore에 "Title"이라는 name의 image로 저장해놓지만
        // 후에 이 image를 삭제함.
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "Title",
            null
        )
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
                val imageUri: Uri? = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }
    //////////////////////////////SCREENSHOT CODE END///////////////////////

    private fun saveMyAccount(name: String) {
        val storageRef = storage.reference

        val fileRef = storageRef.child(user!!.email.toString() + '/' + name + ".jpg")

        val imageRef = storageRef.child("image/" + user!!.email.toString() + '/' + name + ".jpg")

        fileRef.name == imageRef.name // true
        fileRef.path == imageRef.path // false

        val cardView = findViewById<MaterialCardView>(R.id.cardView)
        val bitmap = getScreenShotFromView(cardView)
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = fileRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(this, "저장 실패!", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(this, "저장되었습니다!", Toast.LENGTH_LONG).show()
        }
    }

    private fun savePNGToStorage() {
        val cardView = findViewById<MaterialCardView>(R.id.cardView)
        val bitmap = getScreenShotFromView(cardView)
        if (bitmap != null) {
            saveMediaToStorage(bitmap)
        }
    }

    private fun savePDFToStorage() {
        val filename = "resume_${System.currentTimeMillis()}.pdf"
        val cardView = findViewById<MaterialCardView>(R.id.cardView)
        val bitmap = getScreenShotFromView(cardView)

        if (bitmap != null) {
            val pdfDocument = PdfDocument();
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            canvas.drawBitmap(bitmap, 0F, 0F, paint)

            pdfDocument.finishPage(page)

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                filename
            )

            try {
                pdfDocument.writeTo(FileOutputStream(file))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showPopup(v: View) {
        val ctw = ContextThemeWrapper(this, R.style.MyPopupMenu);
        val popup = PopupMenu(ctw, v) // PopupMenu 객체 선언
        popup.menuInflater.inflate(R.menu.popup, popup.menu) // 메뉴 레이아웃 inflate
        popup.setOnMenuItemClickListener(this) // 메뉴 아이템 클릭 리스너 달아주기
        // Force icons to show
        val menuHelper: Any?
        val argTypes: Array<Class<*>?>
        try {
            val fMenuHelper: Field = PopupMenu::class.java.getDeclaredField("mPopup")
            fMenuHelper.isAccessible = true
            menuHelper = fMenuHelper.get(popup)
            argTypes = arrayOf(Boolean::class.javaPrimitiveType)
            menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes)
                .invoke(menuHelper, true)
        } catch (e: java.lang.Exception) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            popup.show()
            return
        }
        popup.show() // 팝업 보여주기
    }

    private fun showPopupColor(v: View) {
        val ctw = ContextThemeWrapper(this, R.style.MyPopupMenu);
        val popupColor = PopupMenu(ctw, v) // PopupMenu 객체 선언
        popupColor.menuInflater.inflate(R.menu.penpopup, popupColor.menu) // 메뉴 레이아웃 inflate
        popupColor.setOnMenuItemClickListener(this) // 메뉴 아이템 클릭 리스너 달아주기
        // Force icons to show
        val menuHelper: Any?
        val argTypes: Array<Class<*>?>
        try {
            val fMenuHelper1: Field = PopupMenu::class.java.getDeclaredField("mPopup")
            fMenuHelper1.isAccessible = true
            menuHelper = fMenuHelper1.get(popupColor)
            argTypes = arrayOf(Boolean::class.javaPrimitiveType)
            menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes)
                .invoke(menuHelper, true)
        } catch (e: java.lang.Exception) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            popupColor.show()
            return
        }
        popupColor.show() // 팝업 보여주기
    }

    private fun showAlertSetting(v: View) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.alertdialog, null)
        val dialogText = dialogView.findViewById<EditText>(R.id.dialogEt)

        builder.setView(dialogView)
            .setPositiveButton("확인") { dialogInterface, i ->
                Toast.makeText(this, "변경된 사이즈: ${dialogText.text.toString()}", Toast.LENGTH_LONG)
                    .show()
                /* 확인일 때 main의 View의 값에 dialog View에 있는 값을 적용 */
                transTextSize = dialogText.text.toString().toInt()

            }
            .setNegativeButton("취소") { dialogInterface, i ->
                /* 취소일 때 아무 액션이 없으므로 빈칸 */
            }
            .show()
    }

    // 팝업 메뉴 아이템 클릭 시 실행되는 메소드
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) { // 메뉴 아이템에 따라 동작 다르게 하기
            R.id.saveInApp -> {
                if (user != null && code != 7) {
                    val alert = AlertDialog.Builder(this)
                    val input = EditText(this)
                    input.hint = "파일 이름"
                    input.inputType = InputType.TYPE_CLASS_TEXT
                    alert.setTitle("내 계정에 저장")
                        .setView(input)
                        .setPositiveButton("OK") { dialog, which ->
                            var fileName = input.text.toString()
                            saveMyAccount(fileName)
                        }
                        .setNegativeButton("Cancel") { dialog, which ->
                            dialog.cancel()
                        }
                    alert.show()
                } else if (user != null && code == 7) {
                    saveMyAccount(file.substring(0 until file.length - 4))
                } else {
                    Toast.makeText(this, "로그인 후 이용하세요!!", Toast.LENGTH_LONG).show()
                }
            }
            R.id.captureBitmap -> {
                Toast.makeText(this, "저장완료", Toast.LENGTH_LONG).show()
                savePNGToStorage()
            }
            R.id.capturePDF -> {
                Toast.makeText(this, "저장완료", Toast.LENGTH_LONG).show()
                savePDFToStorage()
            }

            R.id.bluecolor -> {
                paintBrush.color = Color.BLUE
                currentColor(paintBrush.color)
            }
            R.id.blackcolor -> {
                paintBrush.color = Color.BLACK
                currentColor(paintBrush.color)
            }
            R.id.redcolor -> {
                paintBrush.color = Color.RED
                currentColor(paintBrush.color)
            }
        }
        return item != null // 아이템이 null이 아닌 경우 true, null인 경우 false 리턴
    }

    private fun currentColor(color: Int) {
        currentBrush = color
        path = Path()
    }


}
