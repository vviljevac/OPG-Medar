package com.example.opgmedar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opgmedar.R
import com.example.opgmedar.eventbus.UpdateCartEvent
import com.example.opgmedar.listener.ICartLoadListener
import com.example.opgmedar.listener.IRecyclerClickListener
import com.example.opgmedar.model.CartModel
import com.example.opgmedar.model.ShopModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.layout_shop_item.view.*
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MyShopAdapter(
    private val context: Context,
    private val list:List<ShopModel>,
    private val cartListener: ICartLoadListener
): RecyclerView.Adapter<MyShopAdapter.MyShopViewHolder>(){

    class MyShopViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var imageView: ImageView?=null
        var textName:TextView?=null
        var textPrice: TextView?=null

        private var clickListener:IRecyclerClickListener? =null
        fun setClickListener(clickListener: IRecyclerClickListener){
            this.clickListener = clickListener
        }
        init {
            imageView = itemView.findViewById(R.id.iv_product) as ImageView
            textName = itemView.findViewById(R.id.tv_name) as TextView
            textPrice = itemView.findViewById(R.id.tv_price) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
        clickListener!!.onItemClickListener(v,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyShopViewHolder {
       return MyShopViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_shop_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyShopViewHolder, position: Int) {
        Glide.with(context)
            .load(list[position].image)
            .into(holder.imageView!!)
        holder.textName!!.text = StringBuilder().append(list[position].name)
        holder.textPrice!!.text = StringBuilder().append(list[position].price).append(" kn")

        holder.setClickListener(object:IRecyclerClickListener{
            override fun onItemClickListener(view: View?, position: Int) {
                addToCart(list[position])
            }
        })
    }

    private fun addToCart(shopModel: ShopModel) {
        val userCart = FirebaseDatabase.getInstance()
            .getReference("Cart")
                .child("UNIQUE_USER_ID")
        userCart.child(shopModel.key!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val cartModel = snapshot.getValue(CartModel::class.java)
                    val updateData: MutableMap<String, Any> = HashMap()
                    cartModel!!.quantity = cartModel!!.quantity+1
                    updateData["quantity"] = cartModel!!.quantity
                    updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()
                    userCart.child(shopModel.key!!)
                        .updateChildren(updateData)
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCartEvent())
                            cartListener.onLoadCartFailed("Success")
                        }
                        .addOnFailureListener{ e -> cartListener.onLoadCartFailed(e.message)}
                }else{
                    val cartModel = CartModel()
                    cartModel.key = shopModel.key
                    cartModel.name = shopModel.name
                    cartModel.image = shopModel.image
                    cartModel.price = shopModel.price
                    cartModel.quantity = 1
                    cartModel.totalPrice = shopModel.price!!.toFloat()

                    userCart.child(shopModel.key!!)
                        .setValue(cartModel)
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCartEvent())
                            cartListener.onLoadCartFailed("Success")
                        }
                        .addOnFailureListener{ e -> cartListener.onLoadCartFailed(e.message)}
                }



                }

                override fun onCancelled(error: DatabaseError) {
                cartListener.onLoadCartFailed(error.message)
                }

            })
    }

    override fun getItemCount(): Int {
        return list.size
    }

}