package com.example.drip_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        imageView.startAnimation(animation)

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this,DripDashboard::class.java)
            startActivity(intent)
            finish()
        },3000)

    }


}



