package com.example.greencart98939

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
         findViewById<TextView>(R.id.newuser).setOnClickListener {
             startActivity(Intent(this,RegisterNewUser::class.java))
             finish()
         }
        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in, navigate to Home Activity
            startActivity(Intent(this, HomeActivity::class.java))
            finish() // Finish current activity to prevent going back to it
        }

        // Set onClickListener for login button
        findViewById<Button>(R.id.login).setOnClickListener {

           authenticateUser()
        }
    }
    private fun authenticateUser() {
        val email = findViewById<EditText>(R.id.email).text.toString()
        val password = findViewById<EditText>(R.id.pass).text.toString()
        if (email.isBlank() || password.isBlank()) {
            // Handle case where email or password is blank
            Toast.makeText(baseContext, "Email or password cannot be blank.",
                Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to Home Activity
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish() // Finish current activity to prevent going back to it
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed:$task",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}