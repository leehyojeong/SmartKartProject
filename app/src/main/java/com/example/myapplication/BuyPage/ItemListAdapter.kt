package com.example.myapplication.BuyPage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Data.Product
import com.example.myapplication.Data.ProductData
import com.example.myapplication.R

class ItemListAdapter(var items:ArrayList<Product>, val context:Context, var hasCount:Boolean):RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    var itemClickListener:OnItemClickListener ?= null

    interface OnItemClickListener{
        fun OnItemClick(holder:ItemListAdapter.ViewHolder, view:View, data: Product, position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //이미지 설정
        holder.name.text = items[position].name
        if(hasCount){
            holder.count.text = items[position].num.toString()
        }
        else{
            holder.count.text = ""
        }
        holder.price.text = items[position].price.toString()+"원"
        var total = 0
        for (i in 0..position){
            total += (items[i].price * items[i].num)
        }
        holder.totalPrice.text = total.toString()
        holder.img.setImageBitmap(items[position].img)
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var img:ImageView
        var name:TextView
        var price:TextView
        var count:TextView
        var totalPrice:TextView
        init{
            img = itemView.findViewById(R.id.item_img)
            name = itemView.findViewById(R.id.item_name)
            price = itemView.findViewById(R.id.item_price)
            count = itemView.findViewById(R.id.item_count)
            totalPrice = itemView.findViewById(R.id.total_price_list)
            itemView.setOnClickListener {
                val position = adapterPosition
                itemClickListener?.OnItemClick(this,it,items[position],position)
            }
        }
    }

}