package com.example.capstoneandroid

import android.content.ClipData.Item
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.example.capstoneandroid.adapter.ItemAdapter
import com.example.capstoneandroid.databinding.ActivityMyFileBinding

class MyFileActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy { ActivityMyFileBinding.inflate(layoutInflater) }

    private val user = FirebaseAuth.getInstance().currentUser
    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.menu.getItem(1).isChecked=true // 해당 액티비티에 맞게 메뉴바 아이콘 색상선택

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        if (user != null) {
            val listRef = storage.reference.child(user!!.email.toString())
            listRef.listAll()
                .addOnSuccessListener { (items, prefixes) ->
                    recyclerView.adapter = ItemAdapter(this, items)
                    recyclerView.setHasFixedSize(true)

                    items.forEach { item ->
                        Log.d("list item", "${item.name.substring(0 until item.name.length - 4)}")
                    }
                }
                .addOnFailureListener {
                }
        } else {
            Toast.makeText(this, "로그인 후 이용하세요.", Toast.LENGTH_LONG).show()
            val intent = Intent(this@MyFileActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_home -> {
                val intent = Intent(this@MyFileActivity, selectDocsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_search -> {
                return true
            }
            R.id.action_account -> {
                val intent = Intent(this@MyFileActivity, AccountActivity::class.java)
                startActivity(intent)
                return true
            }
        }

        return false
    }
}
