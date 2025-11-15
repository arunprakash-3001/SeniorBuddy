// FileName: MainActivity.kt
package com.example.seniorbuddy

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.seniorbuddy.ui.theme.SeniorBuddyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // --- This is the logout function ---
    val onLogout = {
        auth.signOut() // Sign the user out
        navController.navigate("login") {
            popUpTo(0) // Go to login and clear all history
            launchSingleTop = true
        }
        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // --- Login Screen (no change) ---
        composable(route = "login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        return@LoginScreen
                    }
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId == null) {
                                    Toast.makeText(context, "Login failed, user not found", Toast.LENGTH_SHORT).show()
                                    return@addOnCompleteListener
                                }
                                firestore.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val role = document.getString("role")
                                            val destination = if (role == "Senior") {
                                                "senior_dashboard"
                                            } else {
                                                "volunteer_dashboard"
                                            }
                                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                            navController.navigate(destination) {
                                                popUpTo("login") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        // --- Registration Screen (no change) ---
        composable(route = "register") {
            RegistrationScreen(
                onRegisterClick = { name, email, password, role ->
                    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@RegistrationScreen
                    }
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: ""
                                val userMap = hashMapOf("name" to name, "email" to email, "role" to role)
                                firestore.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login")
                                    }
                            } else {
                                Toast.makeText(context, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // --- Pass the onLogout function ---
        composable(route = "senior_dashboard") {
            SeniorDashboardScreen(onLogoutClick = onLogout)
        }

        // --- Pass the onLogout function ---
        composable(route = "volunteer_dashboard") {
            VolunteerDashboardScreen(onLogoutClick = onLogout)
        }
    }
}