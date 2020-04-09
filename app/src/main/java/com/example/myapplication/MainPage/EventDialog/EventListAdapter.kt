package com.example.myapplication.MainPage.EventDialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList
import com.example.myapplication.BuyPage.ItemListAdapter
import com.example.myapplication.Data.EventData
import com.example.myapplication.R
import com.koushikdutta.ion.Ion

class EventListAdapter(val context: Context,var items:ArrayList<EventItem> ) :RecyclerView.Adapter<EventListAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListAdapter.ViewHolder {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val v = LayoutInflater.from(parent.context).inflate(R.layout.event_list,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return items.size
    }

    override fun onBindViewHolder(holder: EventListAdapter.ViewHolder, position: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        holder.name.text = items[position].name
        holder.img.setImageBitmap(items[position].img)
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var img: ImageView
        var name: TextView
        init{
            img = itemView.findViewById(R.id.event_image)
            name = itemView.findViewById(R.id.event_name)
        }
    }
}