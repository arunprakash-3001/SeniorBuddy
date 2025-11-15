// FileName: SeniorDashboardScreen.kt
package com.example.seniorbuddy

// --- IMPORTS (I've added the new icon imports) ---
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout // <-- Correct icon
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
// -------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorDashboardScreen(
    onLogoutClick: () -> Unit // <-- NEW: It now accepts this
) {
    // --- NEW: Variables for new form fields ---
    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var assistance by remember { mutableStateOf("") }
    val vehicleTypes = listOf("Any", "Car", "Auto", "Bike")
    var selectedVehicle by remember { mutableStateOf(vehicleTypes[0]) }
    var fare by remember { mutableStateOf("") }
    var myRequests by remember { mutableStateOf(listOf<RideRequest>()) }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val currentUserId = auth.currentUser?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId == null) {
            return@LaunchedEffect
        }
        val query = firestore.collection("rideRequests")
            .whereEqualTo("seniorId", currentUserId)
            .orderBy("time", Query.Direction.DESCENDING)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SeniorDashboard", "Listen failed.", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                myRequests = snapshot.documents.mapNotNull { document ->
                    document.toObject(RideRequest::class.java)?.copy(id = document.id)
                }
            }
        }
    }

    val activeRequest = myRequests.find { it.status == "pending" || it.status == "accepted" }
    val rideHistory = myRequests.filter { it.status == "completed" || it.status == "cancelled" }
    val requestStatusText = when (activeRequest?.status) {
        "pending" -> "Waiting for a volunteer..."
        "accepted" -> "Accepted by: ${activeRequest.volunteerName ?: "Volunteer"}"
        else -> "No active request"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Senior Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Handle SOS Click */ }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "SOS",
                            tint = Color.White
                        )
                    }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- RIDE REQUEST SECTION ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Request a Ride",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(value = pickup, onValueChange = { pickup = it }, label = { Text("Pickup Place") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = drop, onValueChange = { drop = it }, label = { Text("Drop Place") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Pickup Time") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = assistance,
                            onValueChange = { assistance = it },
                            label = { Text("Special Assistance (e.g., wheelchair)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Vehicle Type:", style = MaterialTheme.typography.bodyLarge)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            vehicleTypes.forEach { type ->
                                Row(
                                    Modifier
                                        .selectable(
                                            selected = (type == selectedVehicle),
                                            onClick = { selectedVehicle = type }
                                        )
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (type == selectedVehicle),
                                        onClick = { selectedVehicle = type }
                                    )
                                    Text(text = type, modifier = Modifier.padding(start = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = fare,
                            onValueChange = { fare = it },
                            label = { Text("Suggested Fare (₹)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (pickup.isEmpty() || drop.isEmpty() || time.isEmpty() || fare.isEmpty()) {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                } else if (currentUserId == null) {
                                    Toast.makeText(context, "Error: Not logged in", Toast.LENGTH_SHORT).show()
                                } else if (activeRequest != null) {
                                    Toast.makeText(context, "You already have an active request", Toast.LENGTH_SHORT).show()
                                } else {
                                    firestore.collection("users").document(currentUserId).get()
                                        .addOnSuccessListener { userDocument ->
                                            val seniorName = userDocument.getString("name") ?: "Unknown Senior"

                                            val rideRequestMap = hashMapOf(
                                                "seniorId" to currentUserId,
                                                "seniorName" to seniorName,
                                                "pickup" to pickup,
                                                "drop" to drop,
                                                "time" to time,
                                                "status" to "pending",
                                                "volunteerId" to null,
                                                "volunteerName" to null,
                                                "assistanceDetails" to assistance,
                                                "vehicleType" to selectedVehicle,
                                                "suggestedFare" to "₹$fare"
                                            )

                                            firestore.collection("rideRequests")
                                                .add(rideRequestMap)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Request Sent Successfully!", Toast.LENGTH_SHORT).show()
                                                    pickup = ""
                                                    drop = ""
                                                    time = ""
                                                    assistance = ""
                                                    fare = ""
                                                    selectedVehicle = vehicleTypes[0]
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Error sending request: ${it.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                }
                            },
                            enabled = activeRequest == null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (activeRequest == null) "Request Volunteer" else "Request in Progress")
                        }
                    }
                }
            }

            // --- REQUEST STATUS SECTION ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your Ride Status",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = requestStatusText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (activeRequest?.status == "accepted") Color(0xFF008000) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // --- RIDE HISTORY SECTION ---
            item {
                Text(
                    text = "Your Ride History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (rideHistory.isEmpty()) {
                item {
                    Text(
                        text = "No completed rides yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(rideHistory) { ride ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("From: ${ride.pickup}", fontWeight = FontWeight.SemiBold)
                            Text("To: ${ride.drop}")
                            Text("Status: ${ride.status}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}