package com.example.wavesoffood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class FavoritesFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var dbRef: DatabaseReference
    private lateinit var favoritesAdapter: ArrayAdapter<String>
    private val cityWeatherData = mutableListOf<String>()
    private val cityWeatherDetails = mutableMapOf<String, Map<String, String>>() // Stores details for each city

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.favorites_list_view, container, false)
        listView = view.findViewById(R.id.favorites_list_view)

        favoritesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, cityWeatherData)
        listView.adapter = favoritesAdapter

        dbRef = FirebaseDatabase.getInstance().getReference("weather")
        loadFavoritesAndFetchWeatherData()

        // Set click listener on ListView items
        listView.setOnItemClickListener { _, _, position, _ ->
            val cityName = cityWeatherData[position].substringBefore(" -") // Get the city name from the clicked item
            val details = cityWeatherDetails[cityWeatherData[position]] // Get the details for that city

            details?.let {
                // Create an Intent to start CityDetailActivity
                val intent = Intent(requireContext(), CityDetailActivity::class.java).apply {
                    putExtra("cityName", cityName) // Pass the city name to CityDetailActivity
                    putExtra("temperature", it["temperature"]) // Pass the temperature
                    putExtra("weatherCondition", it["weatherCondition"]) // Pass the weather condition
                    putExtra("updatedAt", it["updatedAt"]) // Pass the last updated time
                    putExtra("forecast", it["forecast"]) // Pass the forecast
                }
                startActivity(intent) // Start the new activity
            }
        }

        return view
    }

    private fun loadFavoritesAndFetchWeatherData() {
        cityWeatherData.clear()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cityWeatherData.clear()

                if (snapshot.exists()) {
                    val tasks = mutableListOf<Thread>()
                    for (favoriteSnapshot in snapshot.children) {
                        val cityId = favoriteSnapshot.child("id").getValue(String::class.java)
                        val key = favoriteSnapshot.key
                        if (!cityId.isNullOrEmpty()) {
                            val task = Thread { fetchAndStoreWeatherData(cityId, key) }
                            tasks.add(task)
                            task.start()
                        } else {
                            Log.e("FavoritesFragment", "City ID is null or empty for key: $key")
                        }
                    }
                    tasks.forEach { it.join() }
                    activity?.runOnUiThread {
                        favoritesAdapter.notifyDataSetChanged()
                        Log.d("FavoritesFragment", "ListView updated with fresh weather data")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun fetchAndStoreWeatherData(cityId: String, key: String?) {
        val apiKey = "fab4605e645439f544454328fa635b39" // Replace with your actual API key
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityId&units=metric&appid=$apiKey"

        try {
            val result = URL(url).readText(Charsets.UTF_8)
            val jsonObj = JSONObject(result)
            val main = jsonObj.optJSONObject("main") ?: return
            val weather = jsonObj.getJSONArray("weather").optJSONObject(0) ?: return
            val temperature = main.optString("temp", "N/A") + "°C"
            val weatherDescription = weather.optString("description", "N/A").capitalize(Locale.ROOT)
            val updatedAt = jsonObj.optLong("dt", 0) * 1000
            val formattedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(updatedAt))

            // Fetch future weather forecast
            val latitude = jsonObj.optJSONObject("coord")?.optString("lat")
            val longitude = jsonObj.optJSONObject("coord")?.optString("lon")

            var forecast = "No forecast data"
            if (latitude != null && longitude != null) {
                forecast = fetchFutureWeatherForecast(latitude, longitude, apiKey)
            }

            val displayText = "$cityId - $temperature, $weatherDescription (Updated at: $formattedDate)"

            synchronized(cityWeatherData) {
                cityWeatherData.add(displayText)
                cityWeatherDetails[displayText] = mapOf(
                    "temperature" to temperature,
                    "weatherCondition" to weatherDescription,
                    "updatedAt" to formattedDate,
                    "forecast" to forecast // Store the forecast
                )
            }

            if (key != null) {
                dbRef.child(key).updateChildren(
                    mapOf(
                        "temperature" to temperature,
                        "weatherCondition" to weatherDescription,
                        "timestamp" to updatedAt
                    )
                )
            }

        } catch (e: Exception) {
            Log.e("FavoritesFragment", "Error fetching weather data for $cityId: ${e.message}")
        }
    }

    private fun fetchFutureWeatherForecast(latitude: String, longitude: String, apiKey: String): String {
        return try {
            val forecastUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=$latitude&lon=$longitude&units=metric&exclude=current,minutely,hourly,alerts&appid=$apiKey"
            val forecastResult = URL(forecastUrl).readText(Charsets.UTF_8)
            val forecastJsonObj = JSONObject(forecastResult)
            val dailyForecasts = forecastJsonObj.getJSONArray("daily")

            // Get the forecast for the next day
            if (dailyForecasts.length() > 0) {
                val firstForecast = dailyForecasts.getJSONObject(1) // Change index for different days
                val forecastTemperature = firstForecast.getJSONObject("temp").optString("day", "N/A") + "°C"
                val forecastWeatherDescription = firstForecast.getJSONArray("weather").optJSONObject(0)?.optString("description", "N/A")?.capitalize(Locale.ROOT)
                "Tomorrow's forecast: $forecastTemperature, $forecastWeatherDescription"
            } else {
                "No forecast data available"
            }
        } catch (e: Exception) {
            Log.e("FavoritesFragment", "Error fetching forecast data: ${e.message}")
            "Forecast data not available"
        }
    }
}
