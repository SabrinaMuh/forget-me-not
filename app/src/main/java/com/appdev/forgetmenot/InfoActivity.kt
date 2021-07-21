package com.appdev.forgetmenot

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_info)
        val closeButton: ImageView = findViewById(R.id.imageViewClose)
        closeButton.setOnClickListener {
            finish()
        }
    }
}