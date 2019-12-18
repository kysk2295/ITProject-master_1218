package com.example.itproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itproject.R
import com.example.itproject.SearchItem
import com.example.itproject.fragment.SearchFragment

class SearchAdapter(private val list : ArrayList<SearchItem>, private val fragment : SearchFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.card_search, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).title.text = list[position].title
        if(list[position].subtitle.isBlank())
            holder.subtitle.visibility = View.GONE
        else
            holder.subtitle.text = list[position].subtitle
        holder.name.text = list[position].name
        holder.email.text = list[position].email
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title : TextView = itemView.findViewById(R.id.Search_title)
        val subtitle : TextView = itemView.findViewById(R.id.Search_subtitle)
        val name : TextView = itemView.findViewById(R.id.Search_name)
        val email : TextView = itemView.findViewById(R.id.Search_email)
        init {
            itemView.setOnClickListener {
                val titleText = title.text.toString()
                val subtitleText = subtitle.text.toString()
                val emailText = email.text.toString()
                fragment.moveToSet(titleText, subtitleText, emailText)
            }
        }
    }
}