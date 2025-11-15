// FileName: VolunteerDashboardScreen.kt
package com.example.seniorbuddy

// --- IMPORTS (I've fixed the icon imports) ---
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
// -------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDashboardScreen(
    onLogoutClick: () -> Unit // <-- NEW: It now accepts this
) {

    var pendingRequests by remember { mutableStateOf(listOf<RideRequest>()) }
    var myAcceptedRide by remember { mutableStateOf<RideRequest?>(null) }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val currentVolunteerId = auth.currentUser?.uid

    LaunchedEffect(currentVolunteerId) {
        if (currentVolunteerId == null) {
            Log.e("VolunteerDashboard", "User not logged in.")
            return@LaunchedEffect
        }

        val pendingQuery = firestore.collection("rideRequests")
            .whereEqualTo("status", "pending")
            .orderBy("time")
        val pendingRegistration = pendingQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("VolunteerDashboard", "Listen for pending failed.", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                pendingRequests = snapshot.documents.mapNotNull {
                    it.toObject(RideRequest::class.java)?.copy(id = it.id)
                }
            }
        }

        val acceptedQuery = firestore.collection("rideRequests")
            .whereEqualTo("volunteerId", currentVolunteerId)
            .whereEqualTo("status", "accepted")
        val acceptedRegistration = acceptedQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("VolunteerDashboard", "Listen for accepted failed.", error)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                myAcceptedRide = snapshot.documents.first().toObject(RideRequest::class.java)?.copy(id = snapshot.documents.first().id)
            } else {
                myAcceptedRide = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    // --- NEW: LOGOUT BUTTON ---
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // --- SECTION 1 - MY ONGOING RIDE ---
            item {
                Text(
                    text = "My Ongoing Ride",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (myAcceptedRide == null) {
                item {
                    Text(
                        text = "You have no accepted rides.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                    )
                }
            } else {
                val ride = myAcceptedRide!!
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pickup for: ${ride.seniorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // --- UPDATED: Show new details ---
                            Text("From: ${ride.pickup}")
                            Text("To: ${ride.drop}")
                            Text("Time: ${ride.time}")
                            Text("Fare: ${ride.suggestedFare}", fontWeight = FontWeight.Bold)
                            Text("Vehicle: ${ride.vehicleType}")
                            if (ride.assistanceDetails.isNotBlank()) {
                                Text("Assistance: ${ride.assistanceDetails}", color = Color.Red, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    firestore.collection("rideRequests").document(ride.id)
                                        .update("status", "completed")
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Ride Completed!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006400))
                            ) {
                                Text("Ride Completed")
                            }
                        }
                    }
                }
            }

            // --- SECTION 2 - AVAILABLE RIDE REQUESTS ---
            item {
                Text(
                    text = "Available Ride Requests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            if (pendingRequests.isEmpty()) {
                item {
                    Text(
                        text = "No pending requests right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                items(pendingRequests) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pickup for: ${request.seniorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // --- UPDATED: Show new details ---
                            Text("From: ${request.pickup}")
                            Text("To: ${request.drop}")
                            Text("Time: ${request.time}")
                            Text("Fare: ${request.suggestedFare}", fontWeight = FontWeight.Bold)
                            Text("Vehicle: ${request.vehicleType}")
                            if (request.assistanceDetails.isNotBlank()) {
                                Text("Assistance: ${request.assistanceDetails}", color = Color.Red, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (currentVolunteerId == null) {
                                        Toast.makeText(context, "Error: Not logged in", Toast.LENGTH_SHORT).show()
                                    } else {
                                        firestore.collection("users").document(currentVolunteerId).get()
                                            .addOnSuccessListener { userDocument ->
                                                val volunteerName = userDocument.getString("name") ?: "Unknown Volunteer"
                                                val rideDocRef = firestore.collection("rideRequests").document(request.id)

                                                rideDocRef.update(
                                                    mapOf(
                                                        "status" to "accepted",
                                                        "volunteerId" to currentVolunteerId,
                                                        "volunteerName" to volunteerName
                                                    )
                                                )
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Request Accepted!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Accept Request")
                            }
                        }
                    }
                }
            }
        }
    }
}