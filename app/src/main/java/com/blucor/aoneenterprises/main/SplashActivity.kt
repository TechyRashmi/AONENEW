package com.blucor.aoneenterprises.main

import Extra.AppController
import Helpers.C
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R


class SplashActivity : AppCompatActivity() {


    private lateinit var image: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        SetUI()

        startanimation(image)


        Log.e("testt",""+AppController.prefHelper.get(C.userid))
        Handler().postDelayed({
            if (AppController.prefHelper.get(C.userid).equals("")) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 3000)

    }

    fun SetUI()
    {
         image = findViewById(R.id.titleimage)




    }


    fun startanimation(imageView: ImageView)
    {
        val animZoomOut = AnimationUtils.loadAnimation(this,
                R.anim.fade_in
        )
        // assigning that animation to
        // the image and start animation
        imageView.startAnimation(animZoomOut)
    }

}