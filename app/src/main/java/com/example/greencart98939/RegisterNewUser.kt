package com.example.greencart98939

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class RegisterNewUser : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null
    private var token:String?=null
    private var imageurl:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_new_user)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        findViewById<Button>(R.id.signup).setOnClickListener {
            val email = findViewById<EditText>(R.id.newemail).text.toString()
            val password = findViewById<EditText>(R.id.newpass).text.toString()
            val fullName = findViewById<EditText>(R.id.name).text.toString()
            val address = findViewById<EditText>(R.id.adress).text.toString()
            val mobileNo = findViewById<EditText>(R.id.mobileno).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.confirmpass).text.toString()

            // Perform validation checks
            if (email.isBlank() || password.isBlank() || fullName.isBlank() || address.isBlank() || mobileNo.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(baseContext, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(baseContext, "Invalid email format.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.PHONE.matcher(mobileNo).matches()) {
                Toast.makeText(baseContext, "Invalid mobile number format.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(baseContext, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign up the user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid ?: ""
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                 token = task.result.toString()
                                // Pass the token to your listener
                                onTokenReceived(token!!)
                            } else {
                                Log.e("FCM Token", "Failed to get token")
                            }
                        }
                        // Save user details to Firebase Realtime Database
                        val userDetailsRef = database.getReference("users").child(userId).child("userdetails")
                        val userDetails = User(user.toString(),fullName,mobileNo,address,imageurl.toString(),token)
                        userDetailsRef.setValue(userDetails).addOnCompleteListener {
                            Toast.makeText(baseContext, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }

                        // Redirect to Home Activity
                       // Finish current activity
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(baseContext, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        findViewById<ImageView>(R.id.imageView8).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            // Display the selected image using Glide
            val image=findViewById<ImageView>(R.id.imageView8)
            Glide.with(this).load(imageUri).into(image)
            // Upload the selected image to Firebase Storage
            uploadImageToFirebaseStorage(imageUri!!)
        }
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading")
        progressDialog.show()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                token = task.result.toString()
                // Pass the token to your listener
                onTokenReceived(token!!)
            } else {
                Log.e("FCM Token", "Failed to get token")
            }
        }
        val imagesRef = storageRef.child("images"+"${System.currentTimeMillis()}"+ token.toString() +".jpg")

        imagesRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                progressDialog.dismiss()
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                // Get the download URL of the uploaded image
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    // Do something with the download URL (save to database, display in ImageView, etc.)
                    val downloadUrl = uri.toString()
                    // You can save the download URL to Firebase Realtime Database or display it in ImageView
                    // For now, let's just display it in ImageView using Glide
                    val image=findViewById<ImageView>(R.id.imageView8)
                    Glide.with(this).load(downloadUrl).into(image)
                    imageurl=downloadUrl.toString()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                // Update progress dialog while uploading
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressDialog.setMessage("Uploaded $progress%")
            }
    }
    fun onTokenReceived(token: String) {
        // Do something with the token
        Log.d("FCM Token", token)
    }

    companion object {
        const val IMAGE_PICK_REQUEST_CODE = 100
    }
}