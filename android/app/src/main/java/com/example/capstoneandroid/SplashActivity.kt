package com.example.capstoneandroid

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@SplashActivity, selectDocsActivity::class.java)
            startActivity(intent)
        }, 3000)
    }
}