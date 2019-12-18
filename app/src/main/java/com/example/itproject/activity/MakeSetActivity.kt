package com.example.itproject.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itproject.*
import com.example.itproject.adapter.MakeSetAdapter
import com.example.itproject.adapter.MakeSetAdapter.OnItemCheckListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_make_set.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class MakeSetActivity : AppCompatActivity() {

    private var array_word : ArrayList<String>? = null
    private var list : ArrayList<Model> = ArrayList()
    private var array_meaning : ArrayList<String>? = null
    private var adapter : MakeSetAdapter? = null
    private var dialog : AlertDialog? = null
    private var array_null : ArrayList<Int> = ArrayList() // 뜻이 리턴되지 않은 단어들의 인덱스 저장
    private lateinit var onItemClick : OnItemCheckListener
    private var count = 0
    private var title = ""
    private var subtitle = ""
    private val arrayFinished : ArrayList<Boolean> = ArrayList()
    private val arrayFinished_ : ArrayList<Boolean> = ArrayList()
    private var fix : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_set)

        array_word = intent.getStringArrayListExtra("array_word")
        array_meaning = intent.getStringArrayListExtra("array_meaning")

        val alertBuilder : AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        alertBuilder.setView(inflater.inflate(R.layout.dialog_loading, null))
        alertBuilder.setCancelable(false)
        dialog = alertBuilder.create()

        MakeSet_back.setOnClickListener {
            finish()
        }

        onItemClick = object : OnItemCheckListener {

            override fun onItemCheck(index : Int) {
                adapter!!.setSelectedArray(index, true)
            }

            override fun onItemUncheck(index: Int) {
                adapter!!.setSelectedArray(index, false)
            }
        }

        if(array_word != null) {
            dialog!!.show()

            if(array_meaning == null) {
                array_meaning = ArrayList()


                for(i in 0 until array_word!!.size) {
                    array_meaning!!.add("")
                    arrayFinished_.add(false)
                }

                val retrofitService=
                    RetrofitService.create()
                array_word!!.forEachIndexed { index, s ->
                    retrofitService.getWordRetrofit(s).enqueue(object : Callback<WordRepo> {
                        override fun onFailure(call: Call<WordRepo>, t: Throwable) {}
                        override fun onResponse(call: Call<WordRepo>, response: Response<WordRepo>) {
                            val wordrepo: WordRepo? = response.body()
                            arrayFinished_[index] = true
                            if (wordrepo?.meaning?.korean == null) {
                                array_null.add(index)
                            }
                            else
                                array_meaning!![index] = wordrepo.meaning.korean

                            if(!arrayFinished_.contains(false)) {
                                setRecycler()
                            }
                        }
                    })
                }

            }

            else {
                setRecycler()
            }
        }
        else {
            setEmptyRecycler()
        }

        MakeSet_trash.setOnClickListener {
            adapter!!.deleteItems()
        }

        MakeSet_checkbtn.setOnClickListener {
            if(adapter != null) {

                array_word = adapter!!.getWords()
                array_meaning = adapter!!.getMeanings()
                val beforeTitle : String = title
                title = adapter!!.getTitleText()
                subtitle = adapter!!.getSubtitleText()

                when {
                    title.isBlank() -> {
                        Toast.makeText(applicationContext, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                        moveFocus(0, "title")
                    }
                    array_word!!.size == 0 -> {
                        Toast.makeText(applicationContext, "카드를 만들어 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    array_word!!.contains("") -> {
                        val index = array_word!!.indexOf("") + 1
                        Toast.makeText(applicationContext, "단어를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                        moveFocus(index, "word")
                    }
                    array_meaning!!.contains("") -> {
                        val index = array_meaning!!.indexOf("") + 1
                        Toast.makeText(applicationContext, "뜻을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                        moveFocus(index, "meaning")
                    }
                    else -> {
                        dialog!!.show()
                        for(i in 0 until array_word!!.size) {
                            arrayFinished.add(false)
                            addToDB(i)
                        }
                        fix = intent.getBooleanExtra("fix", false)
                        if(fix) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.email!!).collection("sets").document(beforeTitle)
                                .delete()
                            val s : SetActivity = SetActivity.ac
                            s.finish()
                            val edit = getSharedPreferences("shouldRefresh", Context.MODE_PRIVATE).edit()
                            edit.putBoolean("shouldRefresh", true).apply()
                        }
                    }
                }
            }
        }

        MakeSet_addbtn.setOnClickListener {
            val lastIndex = adapter!!.addItem()
            MakeSet_recycler.scrollToPosition(lastIndex)
        }
    }

    private fun setEmptyRecycler() {
        list.add(Model(Model.TITLE_TYPE, "", ""))
        for(i in 0..1) {
            list.add(Model(Model.CARD_TYPE, "", ""))
        }
        adapter = MakeSetAdapter(list, onItemClick, false)
        MakeSet_recycler.adapter = adapter
        MakeSet_recycler.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun setRecycler() {
        if(intent.getStringExtra("title") == null) {
            title = ""
            subtitle = ""
        }
        else {
            title = intent.getStringExtra("title")!!
            subtitle = intent.getStringExtra("subtitle")!!
        }

        list.add(Model(Model.TITLE_TYPE, title, subtitle))
        for(i in 0 until array_word!!.size) {
            list.add(Model(Model.CARD_TYPE, array_word!![i], array_meaning!![i]))
        }
        adapter = MakeSetAdapter(list, onItemClick, false)
        MakeSet_recycler.adapter = adapter
        MakeSet_recycler.layoutManager = LinearLayoutManager(applicationContext)
        dialog!!.dismiss()
    }

    private fun moveFocus(position : Int, what : String) {
        val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        MakeSet_recycler.scrollToPosition(position)
        Handler().postDelayed({
            when (what) {
                "word" -> {
                    MakeSet_recycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<EditText>(
                        R.id.MakeSet_word
                    ).requestFocus()
                    imm.showSoftInput(MakeSet_recycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<EditText>(
                        R.id.MakeSet_word
                    ), 0)
                }
                "meaning" -> {
                    MakeSet_recycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<EditText>(
                        R.id.MakeSet_meaning
                    ).requestFocus()
                    imm.showSoftInput(MakeSet_recycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<EditText>(
                        R.id.MakeSet_meaning
                    ), 0)
                }
                "title" -> {
                    MakeSet_recycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<EditText>(
                        R.id.MakeSet_title
                    ).requestFocus()
                    imm.showSoftInput(MakeSet_recycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<EditText>(
                        R.id.MakeSet_title
                    ), 0)
                }
            }
        }, 100)
    }

    private fun addToDB(i : Int) {
        val db : FirebaseFirestore = FirebaseFirestore.getInstance()
        val email: String = FirebaseAuth.getInstance().currentUser!!.email.toString()

        val tmp = db.collection("users").document(email).collection("sets").document(title)
        val array_star : BooleanArray? = intent.getBooleanArrayExtra("array_star")
        val star : Boolean =
            if(array_star == null) {
                false
            }
            else array_star[i]
        val map = hashMapOf(
            "word" to array_word!![i],
            "meaning" to array_meaning!![i],
            "star" to star
        )

        tmp.collection("_").document(i.toString()).set(map)
            .addOnSuccessListener {
                count++
                arrayFinished[i] = true
                if(!arrayFinished.contains(false)) {
                    dialog!!.dismiss()
                    val intent = Intent(this, SetActivity::class.java)
                    val subMap = hashMapOf(
                        "subtitle" to subtitle,
                        "size" to count,
                        "progress" to 0f,
                        "lastIndex" to 0,
                        "array_incorrect" to ArrayList<Int>()
                    )

                    tmp.set(subMap)

                    if(!fix) {
                        val sf : SharedPreferences = getSharedPreferences("count_sets", Context.MODE_PRIVATE)
                        val et : SharedPreferences.Editor = sf.edit()
                        val count_before = sf.getInt("sets", 0)
                        et.putInt("sets", count_before + 1).apply()
                    }

                    intent.putExtra("title", title)
                    intent.putExtra("subtitle", subtitle)
                    startActivity(intent)
                    finish()
                }
            }
    }
}
