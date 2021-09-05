package com.example.opgmedar

import android.app.Activity
import android.app.AlertDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.opgmedar.databinding.ActivityOrderBinding
import com.example.opgmedar.eventbus.UpdateCartEvent
import com.example.opgmedar.model.CartModel
import com.example.opgmedar.model.CartToOrderModel
import com.example.opgmedar.model.OrderModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.activity_shop.*
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder
import java.security.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var database: DatabaseReference
    private lateinit var cartModels: ArrayList<CartToOrderModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

   private fun init() {
       val dial = AlertDialog.Builder(this)
               .setTitle("Plaćanje se izvršava pri pouzeću")
               .setMessage("")
               .setPositiveButton("Uredu") { dial, _ ->
                   dial.dismiss()
               }
               .create()
       dial.show()
       binding.tvTotalPriceOrder.text = intent.getStringExtra("totalPrice")

       binding.btnFinish.setOnClickListener {
           val firstName = binding.etFirstname.text.toString()
           val lastName = binding.etLastname.text.toString()
           val address = binding.etAddress.text.toString()
           val postal = binding.etPostal.text.toString()
           val email = binding.etEmail.text.toString()
           val phone = binding.etPhone.text.toString()

           if (firstName.length < 2 || firstName.length > 20) {
               Snackbar.make(mainLayout_order, "Krivo uneseno ime", Snackbar.LENGTH_LONG).show()
               binding.etFirstname.text.clear()

           } else if (lastName.length < 2 || lastName.length > 20) {
               Snackbar.make(mainLayout_order, "Krivo uneseno prezime", Snackbar.LENGTH_LONG).show()
               binding.etLastname.text.clear()

           } else if (address.length < 4 || address.length > 60) {
               Snackbar.make(mainLayout_order, "Krivo unesena adresa", Snackbar.LENGTH_LONG).show()
               binding.etAddress.text.clear()
           } else if (email.length < 8 || email.length > 345) {
               Snackbar.make(mainLayout_order, "Krivo unesena e-mail adresa", Snackbar.LENGTH_LONG).show()
               binding.etEmail.text.clear()

           } else if (postal.length != 5) {
               Snackbar.make(mainLayout_order, "Krivo unesen poštanski broj", Snackbar.LENGTH_LONG).show()
               binding.etPostal.text.clear()

           }else if(phone.length >14 || phone.length < 9) {
               Snackbar.make(mainLayout_order, "Krivo unesen broj mobitela", Snackbar.LENGTH_LONG).show()
               binding.etPostal.text.clear()
           }else{

               database = FirebaseDatabase.getInstance().getReference("Orders")
               cartModels = arrayListOf<CartToOrderModel>()
               FirebaseDatabase.getInstance()
                       .getReference("Cart")
                       .child("UNIQUE_USER_ID")
                       .addListenerForSingleValueEvent(object : ValueEventListener {
                           override fun onDataChange(snapshot: DataSnapshot) {
                               if (snapshot.exists()) {
                                   for (cartSnapshot in snapshot.children) {
                                       val cartModel = cartSnapshot.getValue(CartToOrderModel::class.java)
                                       cartModel!!.key = cartSnapshot.key
                                       cartModels.add(cartModel)
                                   }

                               }
                           }

                           override fun onCancelled(error: DatabaseError) {
                           }
                       })
               val rand = Random(System.nanoTime()).nextInt().toString()
               val orderModel = OrderModel(firstName, lastName, address, email, postal, phone, binding.tvTotalPriceOrder.text.toString())
               database.child(rand)
                       .setValue(orderModel).addOnSuccessListener {
                           database.child(rand).child("Order").setValue(cartModels)
                           FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID").removeValue()
                           binding.cbAddress.isChecked = false

                           val dialog = AlertDialog.Builder(this)
                                   .setTitle("NARUDŽBA USPJEŠNA")
                                   .setMessage("Želite li natrag u kupovinu?")
                                   .setNegativeButton("Ne") { dialog, _ ->
                                       dialog.dismiss()
                                       val mainIntent = Intent(this, MainActivity::class.java)
                                       startActivity(mainIntent)
                                   }
                                   .setPositiveButton("Da") { dialog, _ ->
                                       dialog.dismiss()
                                       val shopIntent = Intent(this, ShopActivity::class.java)
                                       startActivity(shopIntent)
                                   }
                                   .create()
                           dialog.show()
                       }.addOnFailureListener {
                           Snackbar.make(mainLayout_order, "Narudžba neuspješna!", Snackbar.LENGTH_LONG).show()
                       }
           }
       }
           binding.ivBackOrder.setOnClickListener {
               val backIntent = Intent(this, CartActivity::class.java)
               startActivity(backIntent)
           }
           binding.cbAddress.setOnCheckedChangeListener { buttonView, isChecked ->
               if (isChecked) {
                   binding.etAddress.setText("Ulica Medina 1, Medulin")
                   binding.etPostal.setText("52203")
                   binding.etAddress.isEnabled = false
                   binding.etPostal.isEnabled = false
               } else {
                   binding.etAddress.setText("")
                   binding.etPostal.setText("")
                   binding.etAddress.isEnabled = true
                   binding.etPostal.isEnabled = true
               }
           }

       binding.mainLayoutOrder.setOnClickListener {

           hideSoftKeyboard()
       }
   }
}
fun Activity.hideSoftKeyboard(){
    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(currentFocus?.windowToken, 0)

       }

}