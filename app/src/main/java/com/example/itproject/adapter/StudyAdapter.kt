package com.example.itproject.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itproject.R
import com.example.itproject.activity.ResultActivity
import com.example.itproject.activity.StudyActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class StudyAdapter(private val array_meaning : ArrayList<String>, private val activity : StudyActivity, private val isRestarted : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val array_holder : ArrayList<MyViewHolder> = ArrayList()
    private var size = 0
    private var index = 0
    private var array_incorrect : ArrayList<Int> //틀린 것들 인덱스 저장
    private var progress : Float = 0f
    private var title = ""
    private var arraySize = 0

    init{
        size = if(array_meaning.size >= 4) 4
        else array_meaning.size
        progress = activity.getProgress()
        array_incorrect = activity.getIncorrectArray()
        if(isRestarted) {
            arraySize = array_incorrect.size
            index = if(array_incorrect.contains(activity.getLastIndex()))
                array_incorrect.indexOf(activity.getLastIndex())
            else
                0
        }
        else {
            //array_incorrect =
            arraySize = array_meaning.size
            index = activity.getLastIndex()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.card_study, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(array_holder.size < size)
            array_holder.add(holder as MyViewHolder)
        if(array_holder.size == size) {
            if(isRestarted)
                setSelections(array_incorrect[index])
            else
                setSelections(index)
        }

    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val meaning : TextView = itemView.findViewById(R.id.Study_meaning)
        init {
            var tmp = false

            itemView.setOnClickListener {
                val sf : SharedPreferences = activity.getSharedPreferences("shouldRefresh", Context.MODE_PRIVATE)
                val edit = sf.edit()
                edit.putBoolean("shouldRefresh", true).apply()

                val input = meaning.text.toString()
                val answer = if(!isRestarted) array_meaning[index]
                else array_meaning[array_incorrect[index]]
                val builder : AlertDialog.Builder = AlertDialog.Builder(activity)
                val inflater : LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val dialog : AlertDialog
                val view : View

                if(input == answer) {
                    //정답
                    view = inflater.inflate(R.layout.dialog_correct, null)
                    view.findViewById<TextView>(R.id.CorrectText).text = answer
                    if(isRestarted)
                        array_incorrect.removeAt(index)
                }
                else {
                    //오답
                    view = inflater.inflate(R.layout.dialog_incorrect, null)
                    view.findViewById<TextView>(R.id.CorrectText).text = answer
                    view.findViewById<TextView>(R.id.IncorrectText).text = input
                    if(!isRestarted)
                        array_incorrect.add(index)
                    else {
                        if(array_incorrect.size > index + 1)
                            index++
                        else
                            tmp = true
                    }
                }

                builder.setView(view)
                dialog = builder.create()
                dialog.show()
                Handler().postDelayed({
                    dialog.dismiss()

                    progress = if(array_incorrect.size > 0)
                        (array_meaning.size - array_incorrect.size).toFloat() / array_meaning.size.toFloat() * 100
                    else
                        100f
                    Log.i(array_meaning.size.toString(), array_incorrect.size.toString())

                    if(isRestarted) {
                        if(array_incorrect.size > index && !tmp)
                            setSelections(array_incorrect[index])
                        else {
                            //index++
                            /*index = if(array_incorrect.size == 0)
                                0
                            else
                                array_incorrect[0]*/
                            /*progress = if(array_incorrect.size > 0)
                                (index - array_incorrect.size).toFloat() / array_meaning.size.toFloat() * 100
                            else
                                100f*/
                            updateDB()
                            moveToResult()
                        }
                    }
                    else {
                        if(index < arraySize - 1)
                            setSelections(++index)
                        else {
                            //index++
                            index = if(array_incorrect.size == 0)
                                0
                            else
                                array_incorrect[0]

                            updateDB()
                            moveToResult()
                        }
                    }

                }, 2000)
            }
        }
    }

    fun setSelections(index : Int) {
        val randArray : ArrayList<Int> = makeRandomArray(size, size)
        val i = Random().nextInt(size) // 정답이 들어갈 인덱스
        val tmpArray : ArrayList<Int> = makeRandomArray(size - 1, array_meaning.size, index)

        var count = 0

        randArray.forEach {
            if(it == i) {
                array_holder[it].meaning.text = array_meaning[index]
            }
            else {
                array_holder[it].meaning.text = array_meaning[tmpArray[count]]
                count++
            }
        }
        activity.setWord(index)
        if(!isRestarted)
            progress = (index - array_incorrect.size).toFloat() / array_meaning.size.toFloat() * 100
        else
            progress = (array_meaning.size - array_incorrect.size).toFloat() / array_meaning.size.toFloat() * 100
            //progress += ((index - array_incorrect.size).toFloat() / array_meaning.size.toFloat()) * 100
        activity.setCurrentProgress(progress)
        updateDB()
    }

    private fun makeRandomArray(size_ : Int, max : Int, exclude : Int = -1) : ArrayList<Int> {
        val randArray : ArrayList<Int> = ArrayList()
        for(it in 0 until size_) {
            var tmp = 0
            if(it == 0) {
                tmp = Random().nextInt(max)

                while(tmp == exclude)
                    tmp = Random().nextInt(max)

                randArray.add(tmp)
            }
            else {
                while(randArray.contains(tmp) || (tmp == exclude))
                    tmp = Random().nextInt(max)

                randArray.add(tmp)
            }
        }
        return randArray
    }

    private fun moveToResult() {
        val intent = Intent(activity, ResultActivity::class.java)
        val array_word = activity.getWords()
        intent.putStringArrayListExtra("array_word", array_word)
        intent.putStringArrayListExtra("array_meaning", array_meaning)
        intent.putExtra("progress", progress)
        intent.putIntegerArrayListExtra("array_incorrect", array_incorrect)
        intent.putExtra("title", title)
        intent.putExtra("lastIndex", index)

        activity.startActivity(intent)
        activity.finish()
    }

    private fun updateDB() {
        val email = FirebaseAuth.getInstance().currentUser!!.email!!
        title = activity.getTitleText()
        val db = FirebaseFirestore.getInstance()
        val map : HashMap<String, Any> = hashMapOf(
            "progress" to progress,
            "array_incorrect" to array_incorrect,
            "lastIndex" to index
        )
        /*if(isRestarted)
            map = hashMapOf(
                "progress" to progress,
                "array_incorrect" to array_incorrect,
                "lastIndex" to index
            )
        else*/

        db.collection("users").document(email).collection("sets").document(title).update(map)
    }
}