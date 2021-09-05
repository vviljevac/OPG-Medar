package com.example.opgmedar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.opgmedar.adapter.MyShopAdapter
import com.example.opgmedar.databinding.ActivityShopBinding
import com.example.opgmedar.eventbus.UpdateCartEvent
import com.example.opgmedar.listener.ICartLoadListener
import com.example.opgmedar.listener.IShopLoadListener
import com.example.opgmedar.model.CartModel
import com.example.opgmedar.model.ShopModel
import com.example.opgmedar.util.SpaceItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_shop.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.jar.Manifest

class ShopActivity : AppCompatActivity(), IShopLoadListener, ICartLoadListener {

    lateinit var shopLoadListener: IShopLoadListener
    lateinit var cartLoadListener: ICartLoadListener
    private lateinit var binding: ActivityShopBinding

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
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
     fun onUpdateCartEvent(event: UpdateCartEvent){
        countCartFromFirebase()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        loadShopFromFirebase()
        countCartFromFirebase()

    }

    private fun countCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Cart")
                .child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartSnapshot in snapshot.children) {
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)
                    }
                    cartLoadListener.onLoadCartSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener.onLoadCartFailed(error.message)
                }

            })
    }

    private fun loadShopFromFirebase() {
        val shopModels : MutableList<ShopModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Products")
            .addListenerForSingleValueEvent(object:ValueEventListener{
               override fun onDataChange(snapshot: DataSnapshot){
                    if(snapshot.exists()){
                        for (shopSnapshot in snapshot.children){
                            val shopModel = shopSnapshot.getValue(ShopModel::class.java)
                            shopModel!!.key = shopSnapshot.key
                            shopModels.add(shopModel)
                        }
                        shopLoadListener.onProductLoadSuccess(shopModels)
                    } else shopLoadListener.onProductLoadFailed("Product does not exist")
                }

                override fun onCancelled(error: DatabaseError) {
                    shopLoadListener.onProductLoadFailed(error.message)
                }
            })
    }

    private fun init(){
        shopLoadListener = this
        cartLoadListener = this

        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.recyclerShop.layoutManager = gridLayoutManager
        binding.recyclerShop.addItemDecoration(SpaceItemDecoration())

        binding.ivBack.setOnClickListener{ val backIntent = Intent(this, MainActivity::class.java)
            startActivity(backIntent)
        }
        binding.cartButton.setOnClickListener{ val cartIntent = Intent(this, CartActivity::class.java)
            startActivity(cartIntent)}
    }

    override fun onProductLoadSuccess(shopModelList: List<ShopModel>) {
        val adapter = MyShopAdapter(this,shopModelList!!, cartLoadListener)
        binding.recyclerShop.adapter = adapter
    }

    override fun onProductLoadFailed(message: String?) {
        Snackbar.make(mainLayout, message!!, Snackbar.LENGTH_LONG).show()
    }

    override fun onLoadCartSuccess(cartModelList: List<CartModel>) {
        var cartSum = 0
        for (cartModel in cartModelList!!) cartSum+= cartModel!!.quantity
        badge!!.setNumber(cartSum)
    }

    override fun onLoadCartFailed(message: String?) {
        Snackbar.make(mainLayout, message!!, Snackbar.LENGTH_LONG).show()
    }
}