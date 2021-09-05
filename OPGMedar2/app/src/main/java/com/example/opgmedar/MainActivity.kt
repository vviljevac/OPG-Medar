package com.example.opgmedar

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.opgmedar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivShop.setOnClickListener{ val shopIntent = Intent(this, ShopActivity::class.java)
            startActivity(shopIntent)
            }
        binding.ivContact.setOnClickListener{
            val contactIntent = Intent(this, ContactActivity::class.java)
            startActivity(contactIntent)}
        binding.ivInfo.setOnClickListener{
            TransitionManager.beginDelayedTransition(binding.root)
            binding.tvOnama.isVisible = !binding.tvOnama.isVisible

        }
    }
}