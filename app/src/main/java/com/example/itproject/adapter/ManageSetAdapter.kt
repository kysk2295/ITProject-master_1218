package com.example.itproject.adapter

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import com.example.itproject.MSItem
import com.example.itproject.fragment.ManageSetFragment
import com.example.itproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

class ManageSetAdapter(
    private val list : ArrayList<MSItem>,
    private val onItemCheck : OnItemCheckListener,
    private val fragment : ManageSetFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var cardCreated : Boolean = false
    private var array_selected : ArrayList<Int> = ArrayList()
    private val array_cardHolder : ArrayList<CardViewHolder> = ArrayList()
    private val itemList : ArrayList<MSItem> = ArrayList()
    private val tmp_selected : ArrayList<Int> = ArrayList()
    private val array_index : ArrayList<Int> = ArrayList()

    init{
        itemList.addAll(list)
        for(i in 0 until list.size) {
            array_index.add(i)
        }
    }

    interface OnItemCheckListener {
        fun onItemCheck(index : Int)
        fun onItemUncheck(index : Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view : View = LayoutInflater.from(parent.context!!).inflate(R.layout.card_manage_set, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        (holder as CardViewHolder).title.text = item.text
        if(item.text2.isNotBlank())
            holder.subtitle.text = item.text2
        else {
            holder.subtitle.visibility = View.GONE
            holder.subtitle.text = ""
        }
        val progressBar = holder.progressBar
        progressBar.max = 100f
        progressBar.progress = item.progress
        progressBar.progressColor = Color.parseColor("#2196f3")
        progressBar.progressBackgroundColor = Color.LTGRAY

        holder.checkbox.setOnClickListener {
            if(holder.checkbox.isChecked)
                onItemCheck.onItemCheck(holder.adapterPosition)
            else
                onItemCheck.onItemUncheck(holder.adapterPosition)

        }
        if(!cardCreated) {
            array_cardHolder.add(holder)
            if(array_cardHolder.size == list.size)
                cardCreated = true
        }
    }

    inner class CardViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title : TextView = itemView.findViewById(R.id.ManageSet_title)
        val subtitle : TextView = itemView.findViewById(R.id.ManageSet_subtitle)
        val progressBar : RoundCornerProgressBar = itemView.findViewById(R.id.ManageSet_progress)
        val checkbox : CheckBox = itemView.findViewById(R.id.ManageSet_checkbox)

        init {
            itemView.setOnClickListener {
                fragment.moveToSet(title.text.toString(), subtitle.text.toString())
            }
        }
    }

    fun setSelectedArray(a : Int, isAdd : Boolean) {
        if(isAdd) {
            array_selected.add(a)
            tmp_selected.add(a)
        }
        else {
            array_selected.remove(a)
            tmp_selected.remove(a)
        }
    }


    fun deleteItems(){
        if(array_selected.size > 1)
            Collections.sort(array_selected, AscendingInteger())
        array_selected.forEach {
            removeItem(it)
        }
        val sf : SharedPreferences = fragment.context!!.getSharedPreferences("count_sets", Context.MODE_PRIVATE)
        val et: SharedPreferences.Editor = sf.edit()
        val count = sf.getInt("sets", 0)
        et.putInt("sets", count - array_selected.size).apply()
        array_selected.clear()
        tmp_selected.clear()
    }

    internal inner class AscendingInteger : Comparator<Int> { //정렬
        override fun compare(a: Int, b: Int): Int {
            return b.compareTo(a)
        }
    }

    private fun removeItem(position : Int) {
        val db = FirebaseFirestore.getInstance()
        val email = FirebaseAuth.getInstance().currentUser!!.email!!
        db.collection("users").document(email).collection("sets").document(itemList[position].text).delete()
        itemList.removeAt(position)
        list.clear()
        list.addAll(itemList)
        notifyItemRemoved(position)
    }

    fun filter(s : String) {
        val text = s.toLowerCase(Locale.getDefault())
        list.clear()
        array_selected.clear()
        array_index.clear()

        for (i in 0 until array_cardHolder.size) {
            array_cardHolder[i].checkbox.isChecked = false
        }
        if (text.isEmpty()) {
            list.addAll(itemList)
            array_selected.addAll(tmp_selected)
            tmp_selected.forEach {
                array_cardHolder[it].checkbox.isChecked = true
            }
        } else {
            for (item in itemList) {
                val title = item.text
                if (title.toLowerCase(Locale.getDefault()).contains(text)) {
                    list.add(item)
                    val index = itemList.indexOf(item)
                    array_index.add(index)
                    if (tmp_selected.contains(index)) {
                        array_cardHolder[array_index.size - 1].checkbox.isChecked = true
                        array_selected.add(index)
                    }
                }
            }
        }
    }
}