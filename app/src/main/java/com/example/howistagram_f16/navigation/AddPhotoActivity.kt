package com.example.howistagram_f16.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.howistagram_f16.R
import com.example.howistagram_f16.databinding.ActivityAddPhotoBinding
import com.example.howistagram_f16.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddPhotoBinding.inflate(layoutInflater) }

    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUrl : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        // Initiate storage
        storage= FirebaseStorage.getInstance()
        auth= FirebaseAuth.getInstance()
        firestore= FirebaseFirestore.getInstance()

        // Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                // 선택된 이미지의 경로 값 저장
                photoUrl= data?.data // 변수에다가 path data 담아줌
                binding.addphotoImage.setImageURI(photoUrl) // ImageView에다가 이미지 GOGO~
            }
        }else{
            // 이미지를 선택하는거 없이 떠날때
            finish() // 그냥 액티비티를 닫아줌.
        }
    }

    fun contentUpload(){
        // filename을 만들어주는 코드
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName= "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)


        //Promise method
        storageRef?.putFile(photoUrl!!)?.continueWithTask(){ task : com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

            // Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userID
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = binding.addphotoEditExplain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK) // 정상적인 상태임을 알려주는 코드

            finish()
        }
    }
}
