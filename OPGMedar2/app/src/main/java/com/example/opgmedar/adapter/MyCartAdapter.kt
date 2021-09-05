package com.example.opgmedar.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opgmedar.R
import com.example.opgmedar.eventbus.UpdateCartEvent
import com.example.opgmedar.model.CartModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.layout_cart_item.view.*
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MyCartAdapter(
    private val context: Context,
    private val cartModelList: List<CartModel>
): RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder>() {
    class MyCartViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var iv_remove:ImageView?=null
        var iv_add:ImageView?=null
        var iv_imageView:ImageView?=null
        var tv_name:TextView?=null
        var tv_price:TextView?=null
        var tv_quantity:TextView?=null
        var iv_delete:ImageView?=null

        init {
            iv_remove = itemView.findViewById(R.id.iv_remove_item) as ImageView
            iv_add = itemView.findViewById(R.id.iv_add_item) as ImageView
            iv_imageView = itemView.findViewById(R.id.iv_cart_product) as ImageView
            tv_name = itemView.findViewById(R.id.tv_cart_name) as TextView
            tv_price = itemView.findViewById(R.id.tv_cart_price) as TextView
            tv_quantity = itemView.findViewById(R.id.tv_quantity) as TextView
            iv_delete = itemView.findViewById(R.id.iv_delete) as ImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCartViewHolder {
        return MyCartViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_cart_item,parent,false))

    }

    override fun onBindViewHolder(holder: MyCartViewHolder, position: Int) {
        Glide.with(context)
            .load(cartModelList[position].image)
            .into(holder.iv_imageView!!)
        holder.tv_name!!.text = StringBuilder().append(cartModelList[position].name)
        holder.tv_price!!.text = StringBuilder().append(cartModelList[position].totalPrice).append("0 kn")
        holder.tv_quantity!!.text = StringBuilder().append(cartModelList[position].quantity)

        holder.iv_remove!!.setOnClickListener{_ -> removeCartItem(holder,cartModelList[position])}
        holder.iv_add!!.setOnClickListener{_ -> addCartItem(holder,cartModelList[position])}
        holder.iv_delete!!.setOnClickListener{_ ->
            val dialog = AlertDialog.Builder(context)
                .setTitle("Obriši proizvod")
                .setMessage("Želite li obrisati proizvod?")
                .setNegativeButton("Odustani") {dialog,_ ->dialog.dismiss()}
                .setPositiveButton("Obriši") {dialog,_ ->dialog.dismiss()
                    notifyItemRemoved(position)
                    FirebaseDatabase.getInstance()
                        .getReference("Cart")
                        .child("UNIQUE_USER_ID")
                        .child(cartModelList[position].key!!)
                        .removeValue()
                        .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateCartEvent()) }
                }
                .create()
            dialog.show()
        }
    }

    private fun addCartItem(holder: MyCartViewHolder, cartModel: CartModel) {
        cartModel.quantity +=1
        cartModel.totalPrice = cartModel.quantity*cartModel.price!!.toFloat()
        holder.tv_price!!.text = StringBuilder().append(cartModel.totalPrice).append("0 kn")
        holder.tv_quantity!!.text = StringBuilder().append(cartModel.quantity)
        updateFirebase(cartModel)
    }

    private fun removeCartItem(holder: MyCartViewHolder, cartModel: CartModel) {
        if (cartModel.quantity > 1){
            cartModel.quantity -=1
            cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
            holder.tv_price!!.text = StringBuilder().append(cartModel.totalPrice).append("0 kn")
            holder.tv_quantity!!.text = StringBuilder().append(cartModel.quantity)
            updateFirebase(cartModel)
        }
    }

    private fun updateFirebase(cartModel: CartModel) {
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .child(cartModel.key!!)
            .setValue(cartModel)
            .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateCartEvent()) }
    }

    override fun getItemCount(): Int {
        return cartModelList.size
    }
}