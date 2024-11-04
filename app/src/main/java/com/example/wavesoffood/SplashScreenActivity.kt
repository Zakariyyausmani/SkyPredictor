package com.example.wavesoffood

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var text1: TextView
    private lateinit var text2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Initialize the TextViews
        text1 = findViewById(R.id.text1)
        text2 = findViewById(R.id.text2)

        // Load animations
        val slideInFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        val slideInFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        // Start animations for each text view
        text1.startAnimation(slideInFromLeft)
        text2.startAnimation(slideInFromRight)

        // Set animation listener to fade out after initial animations
        slideInFromRight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Start fade out effect
                text1.startAnimation(fadeOut)
                text2.startAnimation(fadeOut)

                // Transition to SearchActivity after fade-out completes
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this@SplashScreenActivity, SearchActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish() // Close SplashScreenActivity so it doesn’t remain in the back stack
                }, fadeOut.duration)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}
