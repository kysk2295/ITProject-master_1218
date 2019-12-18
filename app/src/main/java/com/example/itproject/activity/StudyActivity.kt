package com.example.itproject.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itproject.R
import com.example.itproject.adapter.StudyAdapter
import kotlinx.android.synthetic.main.activity_study.*

class StudyActivity : AppCompatActivity() {

    private lateinit var array_word : ArrayList<String>
    private lateinit var adapter : StudyAdapter
    private var title : String = ""
    private lateinit var array_incorrect : ArrayList<Int>
    private var progress : Float = 0f
    private var lastIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        val intent = intent

        if(intent != null) {
            array_word = intent.getStringArrayListExtra("array_word")!!
            val array_meaning : ArrayList<String> = intent.getStringArrayListExtra("array_meaning")!!
            array_incorrect = intent.getIntegerArrayListExtra("array_incorrect")!!
            lastIndex = intent.getIntExtra("lastIndex", 0)
            val isRestarted : Boolean = array_incorrect.contains(lastIndex) || (lastIndex == 0 && array_incorrect.size != 0) //틀린 걸로 재시작한 건지, 아닌지
            title = intent.getStringExtra("title")!!
            Study_progress.progressBackgroundColor = Color.LTGRAY
            Study_progress.progressColor = Color.parseColor("#2196f3")
            Study_progress.max = 100f
            progress = intent.getFloatExtra("progress", 0f) //나갔다 왔을 때 재시작에 필요한 요소가 무엇인지 생각하고 db 재설계해야 함. lastIndex와 틀린 리스트 넣으면 될 듯?
            setCurrentProgress(progress)
            Study_recycler.layoutManager = LinearLayoutManager(applicationContext)
            adapter = StudyAdapter(array_meaning, this, isRestarted)
            Study_recycler.adapter = adapter
            Study_back.setOnClickListener {
                finish()
            }
            setWord(0)
        }
    }

    fun setWord(index : Int) {
        Study_word.text = array_word[index]
    }

    fun setCurrentProgress(f : Float) {
        Study_progress.progress = f
    }

    fun getWords() : ArrayList<String> {
        return array_word
    }

    fun getTitleText() : String {
        return title
    }

    fun getIncorrectArray() : ArrayList<Int> {
        return array_incorrect
    }

    fun getProgress() : Float {
        return progress
    }

    fun getLastIndex() : Int {
        return lastIndex
    }
}
