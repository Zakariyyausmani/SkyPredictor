// Weather.kt
package com.example.wavesoffood

data class Weather(
    var id: String? = null, // Optionally keep an ID if you want to reference it
    var cityName: String? = null,
    var countryName: String? = null,
    var temperature: String? = null,
    var status: String? = null
)
