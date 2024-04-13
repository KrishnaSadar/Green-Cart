package com.example.greencart98939

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ProductAdapter(private val empList: MutableList<Product>, context: Context) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var mListener: onItemClickListener? = null

    interface onItemClickListener{
        fun onItemClick(position: Int){

        }
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.emp_list_item, parent, false)
        return ViewHolder(itemView, mListener!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentEmp = empList[position]
        holder.compony.text=currentEmp.compony.toString()
        holder.productname.text=currentEmp.productname.toString()
        holder.prize.text="₹"+currentEmp.prize.toString()
        holder.rating.text="rating:Not Avail"
        Glide.with(holder.itemView.context).load(currentEmp.productpic).into(holder.productPic)
        holder.itemView.setOnClickListener {
            val product = empList[position]
            val context = holder.itemView.context
            val intent = Intent(context, BuyersActivity::class.java)
            // Pass any data you need to the BuyerActivity using extras
            intent.putExtra("compony", product.compony)
            intent.putExtra("name", product.productname)
            intent.putExtra("prize", product.prize)
            intent.putExtra("description", product.description)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return empList.size
    }

    inner class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        var compony : TextView = itemView.findViewById(R.id.textView)
        var productname : TextView = itemView.findViewById(R.id.textView2)
        var prize : TextView = itemView.findViewById(R.id.textView5)
        var rating : TextView = itemView.findViewById(R.id.textView4)
        var productPic : ImageView = itemView.findViewById(R.id.imageView5)



        init {
            // Add null checks for views
            compony = itemView.findViewById(R.id.textView) ?: TextView(itemView.context)
            productname = itemView.findViewById(R.id.textView2) ?: TextView(itemView.context)
            prize = itemView.findViewById(R.id.textView5) ?: TextView(itemView.context)
            rating = itemView.findViewById(R.id.textView4) ?: TextView(itemView.context)
            productPic = itemView.findViewById(R.id.imageView5) ?: ImageView(itemView.context)
        }
    }



}