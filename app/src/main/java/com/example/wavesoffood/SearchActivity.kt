package com.example.wavesoffood

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.view.ViewGroup
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread
import java.text.SimpleDateFormat
import java.util.*

data class City(
    val name: String,
    val temperature: String,
    val weatherDescription: String,
    val updatedAt: String,
    val country: String
)

class SearchActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var cityInput: AutoCompleteTextView
    private lateinit var searchButton: Button
    private lateinit var sunriseTextView: TextView
    private lateinit var sunsetTextView: TextView
    private lateinit var loader: ProgressBar
    private lateinit var mainContainer: RelativeLayout
    private lateinit var addressText: TextView
    private lateinit var tempText: TextView
    private lateinit var statusText: TextView
    private lateinit var updatedAtText: TextView
    private lateinit var humidityText: TextView
    private lateinit var airQualityText: TextView
    private lateinit var windTextView: TextView
    private lateinit var forecastTextView: TextView
    private lateinit var favoriteButton: ImageView
    private lateinit var viewFavoritesButton: Button
    private lateinit var errorText: TextView
    private lateinit var weatherVideoView: VideoView

    private val cities = mutableListOf<City>()

    private val apiKey = "5cf7b0389e42f4cb0f79ed0472bca7de" // Replace with your actual API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        dbRef = FirebaseDatabase.getInstance().getReference("weather")
        cityInput = findViewById(R.id.cityInput)
        searchButton = findViewById(R.id.searchButton)
        loader = findViewById(R.id.loader)
        mainContainer = findViewById(R.id.mainContainer)
        addressText = findViewById(R.id.address)
        tempText = findViewById(R.id.temp)
        statusText = findViewById(R.id.status)
        updatedAtText = findViewById(R.id.updated_at)
        humidityText = findViewById(R.id.humidityText)
        airQualityText = findViewById(R.id.airQualityText)
        windTextView = findViewById(R.id.wind)
        forecastTextView = findViewById(R.id.forecastTextView)
        favoriteButton = findViewById(R.id.favorite_button)
        viewFavoritesButton = findViewById(R.id.viewFavoritesButton)
        errorText = findViewById(R.id.errorText)
        weatherVideoView = findViewById(R.id.weather_video_view)
        sunriseTextView = findViewById(R.id.sunrise)
        sunsetTextView = findViewById(R.id.sunset)

        loadCitiesFromJson()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            cities.map { "${it.name}, ${it.country}" })
        cityInput.setAdapter(adapter)

        searchButton.setOnClickListener {
            val cityName = cityInput.text.toString().trim()
            if (cityName.isNotEmpty()) {
                fetchWeatherDataByCityName(cityName)
            } else {
                Toast.makeText(this, "Please enter a city name.", Toast.LENGTH_SHORT).show()
            }
        }

        favoriteButton.setOnClickListener {
            val cityName = addressText.text.toString()
            val temperature = tempText.text.toString()
            val weatherDescription = statusText.text.toString()
            val updatedAt = updatedAtText.text.toString()
            if (cityName.isNotEmpty()) {
                addToFavorites(cityName, temperature, weatherDescription, updatedAt)
            } else {
                Toast.makeText(this, "No city weather data to save", Toast.LENGTH_SHORT).show()
            }
        }

        viewFavoritesButton.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        } else {
            fetchCurrentLocationWeather()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocationWeather() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                fetchWeatherData(latitude, longitude)
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCitiesFromJson() {
        try {
            val inputStream = assets.open("cities.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val gson = Gson()
            val cityListType = object : TypeToken<List<City>>() {}.type
            cities.clear()
            cities.addAll(gson.fromJson(reader, cityListType))
            reader.close()
        } catch (e: IOException) {
            Toast.makeText(this, "Error loading city data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchWeatherDataByCityName(cityName: String) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$apiKey"
        Log.d("WeatherFetch", "Fetching URL: $url")

        loader.visibility = View.VISIBLE
        mainContainer.visibility = View.GONE
        errorText.visibility = View.GONE
        weatherVideoView.visibility = View.GONE // Hide video view initially

        thread {
            try {
                val result = URL(url).readText()
                val jsonObj = JSONObject(result)
                Log.d("WeatherFetchResult", result)
                updateUIWithWeatherData(jsonObj)
            } catch (e: Exception) {
                Log.e("WeatherFetchError", "Error fetching data: ${e.localizedMessage}")
                runOnUiThread {
                    loader.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = "Error fetching data: ${e.localizedMessage}\nURL: $url"
                }
            }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey"
        Log.d("WeatherFetch", "Fetching URL: $url")

        loader.visibility = View.VISIBLE
        mainContainer.visibility = View.GONE
        errorText.visibility = View.GONE
        weatherVideoView.visibility = View.GONE // Hide video view initially

        thread {
            try {
                val result = URL(url).readText()
                val jsonObj = JSONObject(result)
                Log.d("WeatherFetchResult", result)
                updateUIWithWeatherData(jsonObj)
            } catch (e: Exception) {
                Log.e("WeatherFetchError", "Error fetching data: ${e.localizedMessage}")
                runOnUiThread {
                    loader.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = "Error fetching data: ${e.localizedMessage}\nURL: $url"
                }
            }
        }
    }

    private fun updateUIWithWeatherData(jsonObj: JSONObject) {
        try {
            val cityName = jsonObj.getString("name")
            val country = if (jsonObj.has("sys") && jsonObj.getJSONObject("sys").has("country")) {
                jsonObj.getJSONObject("sys").getString("country")
            } else {
                "Unknown Country"
            }
            val temperature = jsonObj.getJSONObject("main").getString("temp")
            val weatherDescription =
                jsonObj.getJSONArray("weather").getJSONObject(0).getString("description")
            val updatedAt = jsonObj.getLong("dt")
            val humidity = jsonObj.getJSONObject("main").getString("humidity")
            val windSpeed = jsonObj.getJSONObject("wind").getString("speed")
            val sunrise = jsonObj.getJSONObject("sys").getLong("sunrise")
            val sunset = jsonObj.getJSONObject("sys").getLong("sunset")

            val formattedUpdatedAt =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(updatedAt * 1000))
            val sunriseTime =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sunrise * 1000))
            val sunsetTime =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sunset * 1000))

            // Update the UI on the main thread
            runOnUiThread {
                addressText.text = "$cityName, $country"
                tempText.text = "$temperature°C"
                statusText.text = weatherDescription.capitalize()
                updatedAtText.text = "Last updated: $formattedUpdatedAt" // Updated this line
                humidityText.text = "Humidity: $humidity%"
                windTextView.text = "Wind: $windSpeed m/s"
                sunriseTextView.text = "Sunrise: $sunriseTime"
                sunsetTextView.text = "Sunset: $sunsetTime"

                // Add logic to set the video based on weather
                setWeatherVideo(weatherDescription)

                loader.visibility = View.GONE
                mainContainer.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("WeatherUpdateError", "Error updating UI: ${e.localizedMessage}")
            runOnUiThread {
                loader.visibility = View.GONE
                errorText.visibility = View.VISIBLE
                errorText.text = "Error updating UI: ${e.localizedMessage}"
            }
        }
    }

    private fun addToFavorites(
        cityName: String,
        temperature: String,
        weatherDescription: String,
        updatedAt: String
    ) {
        val favoriteCity = City(
            cityName,
            temperature,
            weatherDescription,
            updatedAt,
            "Country"
        ) // Adjust as necessary
        val key = dbRef.push().key
        if (key != null) {
            dbRef.child(key).setValue(favoriteCity).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "$cityName added to favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error adding to favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun setWeatherVideo(weatherDescription: String) {
        // Match the weather description to play the corresponding video
        val videoUri = when {
            weatherDescription.contains("clear", ignoreCase = true) ||
                    weatherDescription.contains("sunny", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/clearysky")

            weatherDescription.contains("cloud", ignoreCase = true) ||
                    weatherDescription.contains("overcast", ignoreCase = true) ||
                    weatherDescription.contains("partly cloudy", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/cloudy")

            weatherDescription.contains("rain", ignoreCase = true) ||
                    weatherDescription.contains("showers", ignoreCase = true) ||
                    weatherDescription.contains("drizzle", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/rain")

            weatherDescription.contains("storm", ignoreCase = true) ||
                    weatherDescription.contains("thunderstorm", ignoreCase = true) ||
                    weatherDescription.contains("hurricane", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/storm")

            weatherDescription.contains("snow", ignoreCase = true) ||
                    weatherDescription.contains("flurries", ignoreCase = true) ||
                    weatherDescription.contains("blizzard", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/snow")

            weatherDescription.contains("fog", ignoreCase = true) ||
                    weatherDescription.contains("mist", ignoreCase = true) ||
                    weatherDescription.contains("smoke", ignoreCase = true) ||
                    weatherDescription.contains("haze", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/fog")

            weatherDescription.contains("wind", ignoreCase = true) ||
                    weatherDescription.contains("breezy", ignoreCase = true) ||
                    weatherDescription.contains("gale", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/windy")

            weatherDescription.contains("dust", ignoreCase = true) ||
                    weatherDescription.contains("sandstorm", ignoreCase = true) ||
                    weatherDescription.contains("dust storm", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/dust")

            weatherDescription.contains("frost", ignoreCase = true) ||
                    weatherDescription.contains("cold", ignoreCase = true) ->
                Uri.parse("android.resource://${packageName}/raw/frost")

            else -> null
        }


        // Make the VideoView visible
        weatherVideoView.visibility = View.VISIBLE

        videoUri?.let {
            weatherVideoView.setVideoURI(it)
            weatherVideoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                mediaPlayer.start()

                // Set the video view to match the parent's width and height
                weatherVideoView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                weatherVideoView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                weatherVideoView.requestLayout()
                changeTextColors(weatherDescription)
            }
        } ?: run {
            weatherVideoView.visibility = View.GONE
        }
    }


    private fun changeTextColors(weatherDescription: String) {
        val textColor = when {
            weatherDescription.contains("clear", true) -> getColor(R.color.clear_sky_text_color)
            weatherDescription.contains("cloud", true) -> getColor(R.color.cloudy_text_color)
            weatherDescription.contains("rain", true) -> getColor(R.color.rain_text_color)
            weatherDescription.contains("storm", true) -> getColor(R.color.storm_text_color)
            weatherDescription.contains("snow", true) -> getColor(R.color.snow_text_color)
            weatherDescription.contains("fog", true) -> getColor(R.color.fog_text_color)
            weatherDescription.contains("wind", true) -> getColor(R.color.windy_text_color)
            weatherDescription.contains("dust", true) -> getColor(R.color.dust_text_color)
            weatherDescription.contains("smoke", true) -> getColor(R.color.smoke_text_color)
            else -> getColor(R.color.default_text_color)
        }

        // Apply the text color to all relevant TextViews
        addressText.setTextColor(textColor)  // Corresponds to R.id.address
        updatedAtText.setTextColor(textColor) // Corresponds to R.id.updated_at
        statusText.setTextColor(textColor) // Corresponds to R.id.status (weather status)
        tempText.setTextColor(textColor) // Corresponds to R.id.temp (temperature)
        humidityText.setTextColor(textColor) // Corresponds to R.id.humidityText
        airQualityText.setTextColor(textColor) // Corresponds to R.id.airQualityText
// Assuming you have sunrise and sunset defined
        windTextView.setTextColor(textColor) // Corresponds to R.id.wind
        forecastTextView.setTextColor(textColor)
    sunriseTextView.setTextColor(textColor)
        sunsetTextView.setTextColor(textColor)// Corresponds to R.id.forecastTextView
    }


}
