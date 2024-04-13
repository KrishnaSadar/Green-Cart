package com.example.greencart98939

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private var productList: MutableList<Product> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference.child("products")

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rvEmp)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(productList, this)
        productAdapter.setOnItemClickListener(object : ProductAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click here
            }
        })
        recyclerView.adapter = productAdapter

        // Load data from Firebase
        loadDataFromFirebase()
        val user = auth.currentUser
        val userId = user?.uid ?: ""
        val userDetailsRef = database.getReference("users").child(userId).child("userdetails")
        // Assuming userDetailsRef is a DatabaseReference
        userDetailsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method will be called once with the initial value and again whenever data at this location is updated.
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    // Data retrieval successful, you can access user details here
                    // For example:
                    val fullName = user.name
                    val mobileNo = user.mobileNumber
                    val address = user.adress
                    val imageUrl = user.profilepic
                    val token = user.token
                    val image=findViewById<ImageView>(R.id.profileimg)
                    Glide.with(this@HomeActivity).load(imageUrl).into(image)
                } else {
                    // Data does not exist
                    Toast.makeText(baseContext, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", databaseError.toException())
                Toast.makeText(baseContext, "Failed to read user data", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<ImageView>(R.id.profileimg).setOnClickListener {
            val intent=Intent(this,UserProfile::class.java)
            intent.putExtra("userid",userId.toString())
            startActivity(intent)
        }
        findViewById<Button>(R.id.search).setOnClickListener {
            startActivity(Intent(this,Search::class.java))
        }
    }
    private fun loadDataFromFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        productList.add(it)
                    }
                }
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}