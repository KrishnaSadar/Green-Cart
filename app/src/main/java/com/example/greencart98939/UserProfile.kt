package com.example.greencart98939

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greencart98939.RegisterNewUser.Companion.IMAGE_PICK_REQUEST_CODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class UserProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storageRef: StorageReference
    private lateinit var dbRef: DatabaseReference
    private var imageUri: Uri? = null
    private var imageurl:String?=null
    private lateinit var fullname:String
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private var productList: MutableList<Product> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
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
                     fullname = user.name.toString()
                    val mobileNo = user.mobileNumber
                    val address = user.adress
                    val imageUrl = user.profilepic
                    val token = user.token
                    val image=findViewById<ImageView>(R.id.imageView6)
                    findViewById<TextView>(R.id.textView6).text="$fullname"
                    Glide.with(this@UserProfile).load(imageUrl).into(image)
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
             findViewById<Button>(R.id.additem).setOnClickListener {
                 openUpdateDialog(
                    userId,
                     fullname
                 )
             }
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rv1Emp)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(productList, this)
        productAdapter.setOnItemClickListener(object : ProductAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click here
            }
        })
        recyclerView.adapter = productAdapter

        // Load data from Firebase
        loadDataFromFirebase(userId.toString())
    }
    private fun openUpdateDialog(empId: String,empNmae:String) {
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog, null)

        mDialog.setView(mDialogView)

        val productNmae = mDialogView.findViewById<EditText>(R.id.etEmpName)
        val prize = mDialogView.findViewById<EditText>(R.id.etEmpAge)
        val description = mDialogView.findViewById<EditText>(R.id.etEmpSalary)
          val productpic=mDialogView.findViewById<ImageView>(R.id.imageView7)
        val btnUpdateData = mDialogView.findViewById<Button>(R.id.btnUpdateData)

        mDialog.setTitle("Adding Item ....")

        val alertDialog = mDialog.create()
        alertDialog.show()
// Set click listener to imageView7 to select image
        productpic.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }
        btnUpdateData.setOnClickListener {
            updateEmpData(
                empId,
                fullname.toString(),
                productNmae.text.toString(),
                prize.text.toString(),
                imageurl.toString(),
                description.text.toString()
            )

            MotionToast.createToast(this,
                "Upadated",
                "Update is Succesfully",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.montserrat_bold))


            alertDialog.dismiss()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            // Upload the selected image to Firebase Storage
            uploadImageToFirebaseStorage(imageUri!!)
        }
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading")
        progressDialog.show()

        val imagesRef = storageRef.child("images" + "${System.currentTimeMillis()}" + ".jpg")

        imagesRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                progressDialog.dismiss()
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                // Get the download URL of the uploaded image
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    // Save the URL to imageurl
                    imageurl = uri.toString()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                // Update progress dialog while uploading
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressDialog.setMessage("Uploaded $progress%")
            }
    }


    private fun updateEmpData(id: String, compony: String, productname: String, prize: String, productpic: String, decription: String) {

        val dbRef = FirebaseDatabase.getInstance().getReference("products").child(productname.toString())
        val MydbRef = FirebaseDatabase.getInstance().getReference("Myproducts").child(id.toString()).child(productname.toString())
        val empInfo = Product(id, compony, productname, prize,decription,productpic)
        dbRef.setValue(empInfo).addOnCompleteListener {
            Toast.makeText(this, "upload succesfull", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "upload failed:${it.localizedMessage}", Toast.LENGTH_SHORT).show()

        }
        MydbRef.setValue(empInfo)
    }
    private fun loadDataFromFirebase(id:String) {
        val MydbRef = FirebaseDatabase.getInstance().getReference("Myproducts").child(id.toString())
        MydbRef.addValueEventListener(object : ValueEventListener {
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