package com.example.wavesoffood

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class CityDetailActivity : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var favoriteButton: ImageView
    private lateinit var cityNameTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherConditionTextView: TextView
    private lateinit var updatedAtTextView: TextView
    private lateinit var forecastTextView: TextView // New TextView for forecast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_detail)

        // Initialize Firebase Database reference
        dbRef = FirebaseDatabase.getInstance().getReference("weather")

        // Initialize views
        favoriteButton = findViewById(R.id.favorite_button)
        cityNameTextView = findViewById(R.id.cityNameTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        weatherConditionTextView = findViewById(R.id.weatherConditionTextView)
        updatedAtTextView = findViewById(R.id.updatedAtTextView)
        forecastTextView = findViewById(R.id.forecastTextView) // Initialize forecast TextView

        // Get the city details from the intent
        val cityName = intent.getStringExtra("cityName") ?: "Unknown City"
        val temperature = intent.getStringExtra("temperature") ?: "N/A"
        val weatherCondition = intent.getStringExtra("weatherCondition") ?: "N/A"
        val updatedAt = intent.getStringExtra("updatedAt") ?: "N/A"

        // Extract city name and country
        val cityParts = cityName.split(", ")
        val displayCityName = if (cityParts.size == 2) {
            "${cityParts[0]}, ${cityParts[1]}" // "City, Country"
        } else {
            cityName // Fallback
        }

        // Set data to the views
        cityNameTextView.text = displayCityName
        temperatureTextView.text = getString(R.string.temperature_format, temperature) // Use formatted string
        weatherConditionTextView.text = getString(R.string.condition_format, weatherCondition) // Use formatted string
        updatedAtTextView.text = getString(R.string.updated_at_format, updatedAt) // Use formatted string

        // Fetch and display the weather forecast
        fetchWeatherForecast(cityParts[0]) // Call method to fetch forecast using city name

        // Set click listener for the favorite button
        favoriteButton.setOnClickListener {
            addToFavorites(displayCityName) // Pass city name to add to favorites
        }
    }

    private fun fetchWeatherForecast(city: String) {
        // Use OpenWeather API to fetch forecast
        val apiKey = "fab4605e645439f544454328fa635b39" // Replace with your actual API key
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$city&units=metric&appid=$apiKey"

        Thread {
            try {
                val result = URL(url).readText(Charsets.UTF_8)
                val jsonObj = JSONObject(result)
                val list = jsonObj.getJSONArray("list")

                // Get the forecast for the next day (for example)
                if (list.length() > 0) {
                    val firstForecast = list.getJSONObject(1) // Change index for different days
                    val forecastTemperature = firstForecast.getJSONObject("main").getString("temp") + "°C"
                    val forecastWeatherDescription = firstForecast.getJSONArray("weather").getJSONObject(0).getString("description").capitalize(Locale.ROOT)

                    // Update the forecast TextView
                    val forecastText = "Tomorrow's forecast: $forecastTemperature, $forecastWeatherDescription"
                    runOnUiThread {
                        forecastTextView.text = forecastText
                    }
                } else {
                    runOnUiThread {
                        forecastTextView.text = "No forecast data available"
                    }
                }
            } catch (e: Exception) {
                Log.e("CityDetailActivity", "Error fetching forecast data: ${e.message}")
                runOnUiThread {
                    forecastTextView.text = "Forecast data not available"
                }
            }
        }.start()
    }

    private fun addToFavorites(cityName: String) {
        // Generate a unique key for each favorite
        val favoriteId = dbRef.push().key
        if (favoriteId != null) {
            val favoriteData = mapOf(
                "id" to cityName,
                "timestamp" to System.currentTimeMillis() // Optional: Store a timestamp for the favorite
            )

            // Store the favorite in Firebase
            dbRef.child(favoriteId).setValue(favoriteData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                    Log.d("CityDetailActivity", "Favorite added: $cityName")
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Error adding to favorites: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("CityDetailActivity", "Error adding favorite: ${error.message}")
                }
        } else {
            Toast.makeText(this, "Error generating favorite ID", Toast.LENGTH_SHORT).show()
            Log.e("CityDetailActivity", "Favorite ID generation failed")
        }
    }
}
