package com.example.itproject.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.example.itproject.R

class CardAdapter(private val array_word: ArrayList<String>, private val array_meaning: ArrayList<String>, private val array_star: BooleanArray) : BaseAdapter(),FragmentManager.OnBackStackChangedListener {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.card, parent, false)
        val textView = view.findViewById<TextView>(R.id.textViewCard)
        textView.text = array_word[position]
        Log.d("asdf","test3")
        var cnt:Int=0


        textView.setOnClickListener(View.OnClickListener {

            ++cnt
            Log.d("asdf","test2")
            val oa1:ObjectAnimator=ObjectAnimator.ofFloat(view,"scaleX",1f,0f)
            val oa2:ObjectAnimator= ObjectAnimator.ofFloat(view,"scaleX",1f,0f)
            oa1.setInterpolator(DecelerateInterpolator())
            oa2.setInterpolator(AccelerateDecelerateInterpolator())
            oa1.setDuration(400)
            oa2.setDuration(400)

            oa1.addListener(object :AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    Log.d("asdf","test1")
                    textView.setText(array_meaning[position])
                    view.requestLayout()
                }
            })
            oa2.addListener(object :AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    textView.setText(array_word[position])
                    view.requestLayout()
                }
            })
            if (cnt%2==0){
                oa2.start()
            }
            else {
                oa1.start()
            }

        })
        return view
    }



    override fun getItem(position: Int): Any {
        return array_word[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return array_word.size
    }

    override fun onBackStackChanged() {

    }


}