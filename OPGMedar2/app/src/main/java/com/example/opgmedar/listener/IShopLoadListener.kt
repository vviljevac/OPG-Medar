package com.example.opgmedar.listener

import com.example.opgmedar.model.ShopModel

interface IShopLoadListener {
    fun onProductLoadSuccess(shopModelList:List<ShopModel>)
    fun onProductLoadFailed(message:String?)
}