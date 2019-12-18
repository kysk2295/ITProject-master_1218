package com.example.itproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.itproject.R

class SwipeStackAdapter : BaseAdapter {

    var mdata: List<String>?=null
    internal var context: Context?=null

    internal constructor() {}
    internal constructor(data: List<String>, context: Context) {
        mdata = data
        this.context = context
    }

    override fun getCount(): Int {
        return mdata?.size!!
    }

    override fun getItem(i: Int): String {
        return mdata?.get(i).toString()
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View, viewGroup: ViewGroup): View {
        val li = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        li.inflate(R.layout.card, viewGroup, false)
        val textView = view.findViewById<TextView>(R.id.textViewCard)
        textView.text = mdata?.get(i)
        return view
    }
}