// FileName: RideRequest.kt
package com.example.seniorbuddy // Make sure this package name matches yours!

// This is the single, shared data class for your app
data class RideRequest(
    val id: String = "",
    val seniorId: String = "",
    val seniorName: String = "",
    val pickup: String = "",
    val drop: String = "",
    val time: String = "",
    val status: String = "",
    val volunteerId: String? = null,
    val volunteerName: String? = null,

    // --- THESE ARE THE NEW FIELDS YOU ARE MISSING ---
    val assistanceDetails: String = "", // For disabilities
    val vehicleType: String = "",       // Car, Bike, Auto
    val suggestedFare: String = ""      // The price
)