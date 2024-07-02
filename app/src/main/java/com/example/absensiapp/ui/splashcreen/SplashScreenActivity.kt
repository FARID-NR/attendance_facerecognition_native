package com.example.absensiapp.ui.splashcreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.absensiapp.R
import com.example.absensiapp.ui.login.LoginActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            // Kode Anda untuk dieksekusi setelah penundaan
            startActivity(Intent(this, LoginActivity::class.java))
            this?.finish()
        }, 3000)
    }
}