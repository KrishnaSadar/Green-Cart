package com.example.greencart98939

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.razorpay.Checkout
import org.json.JSONException
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class BuyersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyers)
        val prize="₹"+intent.getStringExtra("prize")
        val description=intent.getStringExtra("description")
        val des=findViewById<TextView>(R.id.textView9)
        val amount=findViewById<TextView>(R.id.textView8)
        amount.text=prize.toString()
        des.text=description.toString()

        val buy=findViewById<Button>(R.id.buy)
        buy.setOnClickListener {
            val editadress=findViewById<EditText>(R.id.editTextText2)
            val adress=editadress.text.toString()

            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@BuyersActivity)
            builder.setMessage("Make the payment and we connect with you")
            builder.setTitle("Are you sure to buy?")
            builder.setCancelable(false)
            builder.setPositiveButton("Make Payment",DialogInterface.OnClickListener { dialog, which ->
                // on below line getting amount from edit text
                val amt =intent.getStringExtra("prize").toString()

                // rounding off the amount.
                val amount = Math.round(amt.toFloat() * 100).toInt()

                // on below line we are
                // initializing razorpay account
                val checkout = Checkout()

                // on the below line we have to see our id.
                checkout.setKeyID("rzp_test_kaaL0urTmlOHSN")

                // set image
                checkout.setImage(R.drawable.applogo)

                // initialize json object
                val obj = JSONObject()
                try {
                    // to put name
                    obj.put("name", "RoutMate")

                    // put description
                    obj.put("description", "Test payment")

                    // to set theme color
                    obj.put("theme.color", "")

                    // put the currency
                    obj.put("currency", "INR")

                    // put amount
                    obj.put("amount", amount)

                    // put mobile number
                    obj.put("prefill.contact", "9527588063")

                    // put email
                    obj.put("prefill.email", "krishna.sadar23@vit.edu")

                    // open razorpay to checkout activity
                    checkout.open(this@BuyersActivity, obj)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            })
            builder.setNegativeButton("Cancle",DialogInterface.OnClickListener { dialog, which ->

                dialog.cancel()
            })
            val alertDialog: android.app.AlertDialog? = builder.create()
            if (alertDialog != null) { alertDialog.show() }

//closing of payment gatwaye



        }
    }
    fun onPaymentSuccess(s: String?) {
        // this method is called on payment success.
        MotionToast.createToast(this@BuyersActivity,
            "Oops!",
            "Payment Succesful",
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this@BuyersActivity, www.sanju.motiontoast.R.font.montserrat_bold))

    }

    fun onPaymentError(p0: Int, s: String?) {
        // on payment failed.
        MotionToast.createToast(this@BuyersActivity,
            "Oops!",
            "Payment Failed due to error",
            MotionToastStyle.ERROR,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this@BuyersActivity, www.sanju.motiontoast.R.font.montserrat_bold))
//        // Toast.makeText(this, "Payment Failed due to error : " + s, Toast.LENGTH_SHORT).show();

    }

}