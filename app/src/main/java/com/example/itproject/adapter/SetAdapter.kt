package com.example.itproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itproject.Model
import com.example.itproject.R
import com.example.itproject.activity.SetActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class SetAdapter(private val list : MutableList<Model>, private val name : String, private val activity : SetActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //private val array_star : ArrayList<Boolean> = ArrayList()
    private lateinit var tmp : CollectionReference
    private var title = ""
    private var titleCreated : Boolean = false
    private var cardCreated : Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view : View?
        val size = list.size - 1
        val email = FirebaseAuth.getInstance().currentUser!!.email!!
        tmp = FirebaseFirestore.getInstance().collection("users").document(email).collection("sets")
        /*if(array_star.size != size) {
            for(i in 0 until size) {
                array_star.add(false)
            }
        }*/
        return when(viewType) {

            Model.TITLE_TYPE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.title_set, parent, false)
                TitleTypeViewHolder(view)
            }

            Model.CARD_TYPE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.card_set, parent, false)
                CardTypeViewHolder(view)
            }

            else -> throw RuntimeException("viewType 에러")
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        when(item.type) {
            Model.TITLE_TYPE -> {
                if(!titleCreated) {
                    title = item.text
                    (holder as TitleTypeViewHolder).title.text = title
                    if(item.text2.isNotBlank())
                        holder.subtitle.text = item.text2
                    else
                        holder.subtitle.visibility = View.GONE
                    holder.countText.text = "단어 ${list.size - 1}개"
                    holder.nameText.text = name
                    if(activity.getIsFromOther()) {
                        holder.studyBtn.visibility = View.GONE
                        holder.cardBtn.visibility = View.GONE
                    }
                    else {
                        holder.studyBtn.setOnClickListener {
                            activity.moveToStudy()
                        }
                        holder.cardBtn.setOnClickListener {
                            activity.moveToCard()
                        }
                    }
                    titleCreated = true
                }
            }

            Model.CARD_TYPE -> {
                if(!cardCreated) {
                    (holder as CardTypeViewHolder).word.text = item.text
                    holder.meaning.text = item.text2
                    val index = position - 1
                    val star = activity.getStar(index)
                    if(star)
                        holder.star.setImageResource(R.drawable.star_yellow)
                    holder.star.setOnClickListener {
                        if(!star) {
                            holder.star.setImageResource(R.drawable.star_yellow)
                            activity.setStar(index, true)
                            tmp.document(title).collection("_").document(index.toString()).update("star", true)
                        }
                        else {
                            holder.star.setImageResource(R.drawable.star_white)
                            //array_star[index] = false
                            activity.setStar(index, false)
                            tmp.document(title).collection("_").document(index.toString()).update("star", false)
                        }
                    }
                    if(position == list.size - 1)
                        cardCreated = true
                }

            }
        }
    }

    inner class TitleTypeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title : TextView = itemView.findViewById(R.id.SetActivity_title)
        val subtitle : TextView = itemView.findViewById(R.id.SetActivity_subtitle)
        val countText : TextView = itemView.findViewById(R.id.SetActivity_count)
        val nameText : TextView = itemView.findViewById(R.id.SetActivity_name)
        val studyBtn : Button = itemView.findViewById(R.id.SetActivity_study)
        val cardBtn : Button = itemView.findViewById(R.id.SetActivity_card)
    }

    inner class CardTypeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val word : TextView = itemView.findViewById(R.id.Set_word)
        val meaning : TextView = itemView.findViewById(R.id.Set_meaning)
        val star : ImageView = itemView.findViewById(R.id.Set_star)
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }
}