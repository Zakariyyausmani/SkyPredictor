package com.example.wavesoffood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FavoritesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // Load FavoritesFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FavoritesFragment())
            .commit()
    }
}
