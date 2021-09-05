package com.example.opgmedar

import android.R.id.button1
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opgmedar.adapter.MyCartAdapter
import com.example.opgmedar.databinding.ActivityCartBinding
import com.example.opgmedar.eventbus.UpdateCartEvent
import com.example.opgmedar.listener.ICartLoadListener
import com.example.opgmedar.model.CartModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.layout_cart_item.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode



class CartActivity : AppCompatActivity(), ICartLoadListener {

    var cartLoadListener:ICartLoadListener?=null
    var tv_empty:TextView?=null
    private lateinit var binding: ActivityCartBinding
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun init(){
        cartLoadListener = this
        tv_empty = findViewById<TextView>(R.id.tv_empty)

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerCart!!.layoutManager = layoutManager
        binding.recyclerCart!!.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        binding.ivBackCart!!.setOnClickListener{  val backIntent = Intent(this, ShopActivity::class.java)
            startActivity(backIntent)}
        binding.btnOrder!!.setOnClickListener {
            if (binding.tvTotalPrice.text != "0.00 kn"){
                val orderIntent = Intent(this, OrderActivity::class.java)
                orderIntent.putExtra("totalPrice", binding.tvTotalPrice.text)
                startActivity(orderIntent)}else {
                TransitionManager.beginDelayedTransition(binding.root)
                tv_empty!!.isVisible = true
                }
        }

    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateCartEvent(event: UpdateCartEvent){
        loadCartFromFirebase()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        loadCartFromFirebase()
    }

    private fun loadCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Cart")
                .child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartSnapshot in snapshot.children) {
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)

                    }

                    cartLoadListener!!.onLoadCartSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener!!.onLoadCartFailed(error.message)
                }

            })
    }

    override fun onLoadCartSuccess(cartModelList: List<CartModel>) {

        var sum = 0.00
        for (cartModel in cartModelList){
            sum+= cartModel.totalPrice
        }
        binding.tvTotalPrice.text = StringBuilder().append(sum).append("0 kn")
        val adapter = MyCartAdapter(this, cartModelList)
        binding.recyclerCart!!.adapter = adapter
    }

    override fun onLoadCartFailed(message: String?) {
        Snackbar.make(mainLayout, message!!, Snackbar.LENGTH_LONG).show()
    }
}