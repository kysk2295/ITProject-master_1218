package com.example.itproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.itproject.Model
import com.example.itproject.R
import java.util.*
import kotlin.collections.ArrayList

class MakeSetAdapter(private val list : MutableList<Model>, private val onItemCheck: OnItemCheckListener, private val isEmpty : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var array_word : ArrayList<String>? = ArrayList()
    private var array_meaning : ArrayList<String>? = ArrayList()
    private var title : String = ""
    private var subtitle : String = ""
    private var array_selected : ArrayList<Int> = ArrayList()
    private var array_holder : ArrayList<CardTypeViewHolder> = ArrayList()
    private var titleCreated : Boolean = false
    private var cardCreated : Boolean = false
    private lateinit var titleHolder : TitleTypeViewHolder

    interface OnItemCheckListener {
        fun onItemCheck(index : Int)
        fun onItemUncheck(index : Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view : View?

        return when(viewType) {

            Model.TITLE_TYPE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.title_make_set, parent, false)
                TitleTypeViewHolder(view)
            }

            Model.CARD_TYPE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.card_make_set, parent, false)
                CardTypeViewHolder(view)
            }

            else -> throw RuntimeException("viewType 에러")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = list[position]
        when(item.type) {

            Model.TITLE_TYPE -> {
                if(!titleCreated) {
                    (holder as TitleTypeViewHolder).et_title.setText(item.text)
                    title = item.text
                    holder.et_subtitle.setText(item.text2)
                    subtitle = item.text2
                    titleCreated = true
                    titleHolder = holder
                }
            }

            Model.CARD_TYPE -> {

                if(array_holder.size < position) {
                    (holder as CardTypeViewHolder).et_word.setText(item.text)
                    if(!isEmpty && !cardCreated) {
                        array_word!!.add(item.text)
                        array_meaning!!.add(item.text2)
                        if(array_word!!.size == list.size - 1)
                            cardCreated = true
                    }
                    holder.et_meaning.setText(item.text2)
                    holder.checkbox.isChecked = false
                    holder.checkbox.setOnClickListener {
                        if(holder.checkbox.isChecked)
                            onItemCheck.onItemCheck(holder.adapterPosition)
                        else
                            onItemCheck.onItemUncheck(holder.adapterPosition)

                    }
                    holder.et_word.requestFocus()
                    array_holder.add(holder)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }

    inner class TitleTypeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val et_title : EditText = itemView.findViewById(R.id.MakeSet_title)
        val et_subtitle : EditText = itemView.findViewById(R.id.MakeSet_subtitle)
    }

    inner class CardTypeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val et_word : EditText = itemView.findViewById(R.id.MakeSet_word)
        val et_meaning : EditText = itemView.findViewById(R.id.MakeSet_meaning)
        val checkbox : CheckBox = itemView.findViewById(R.id.MakeSet_checkbox)
    }

    private fun removeItem(position : Int) {
        val index = position - 1
        list.removeAt(position)
        array_holder.removeAt(index)
        array_word!!.removeAt(index)
        array_meaning!!.removeAt(index)
        notifyItemRemoved(position)
    }

    fun deleteItems(){
        if(array_selected.size > 1)
            Collections.sort(array_selected, AscendingInteger())
        array_selected.forEach {
            removeItem(it)
        }
        array_selected = ArrayList()
    }

    internal inner class AscendingInteger : Comparator<Int> { //정렬
        override fun compare(a: Int, b: Int): Int {
            return b.compareTo(a)
        }
    }

    fun addItem() : Int{
        array_word!!.add("")
        array_meaning!!.add("")
        list.add(
            Model(
                Model.CARD_TYPE,
                "",
                ""
            )
        )
        notifyItemInserted(list.size - 1)
        return getLastIndex()
    }

    private fun getLastIndex() : Int {
        return list.size - 1
    }

    private fun setArray() {
        for(i in 0 until array_holder.size) {
            array_word!![i] = array_holder[i].et_word.text.toString()
            array_meaning!![i] = array_holder[i].et_meaning.text.toString()
        }
    }

    private fun setTitleText() {
        title = titleHolder.et_title.text.toString()
        subtitle = titleHolder.et_subtitle.text.toString()
    }

    fun getWords() : ArrayList<String> {
        setArray()
        return array_word!!
    }

    fun getMeanings() : ArrayList<String> {
        return array_meaning!!
    }

    fun getTitleText() : String {
        setTitleText()
        return title
    }

    fun getSubtitleText() : String {
        return subtitle
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if(holder.adapterPosition == 0) {
            (holder as TitleTypeViewHolder).et_title.setText(title)
            holder.et_subtitle.setText(subtitle)
        }
        else {
            val index = holder.adapterPosition - 1
            val word = array_word!![index]
            (holder as CardTypeViewHolder).et_word.setText(word)
            val meaning = array_meaning!![index]
            holder.et_meaning.setText(meaning)
            if(array_selected.contains(holder.adapterPosition)) {
                holder.checkbox.isChecked = true
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)

        if(holder.adapterPosition == 0) {
            title = (holder as TitleTypeViewHolder).et_title.text.toString()
            subtitle = holder.et_subtitle.text.toString()
        }

        else if(holder.adapterPosition != -1){
            val index = holder.adapterPosition - 1
            array_word!![index] = (holder as CardTypeViewHolder).et_word.text.toString()
            array_meaning!![index] = holder.et_meaning.text.toString()
        }
    }

    fun setSelectedArray(a : Int, isAdd : Boolean) {
        if(isAdd)
            array_selected.add(a)
        else
            array_selected.remove(a)
    }

}
