package com.example.itproject.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.itproject.R
import com.example.itproject.activity.MakeSetActivity
import de.hdodenhof.circleimageview.CircleImageView

class MainFragment : Fragment() {

    private lateinit var mainButton : CircleImageView
    private lateinit var cameraButton : ImageView
    private lateinit var pencilButton : ImageView
    private lateinit var background_view : View
    private lateinit var disappearance_left : Animation
    private lateinit var disappearance_right : Animation
    private lateinit var anim_reduction : Animation
    private var count_mainFragment : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view : View = inflater.inflate(R.layout.frame_main, container, false)

        mainButton = view.findViewById(R.id.main_button_sub)

        val anim_expansion : Animation = AnimationUtils.loadAnimation(activity,
            R.anim.expansion
        )
        val appearance_left : Animation = AnimationUtils.loadAnimation(activity,
            R.anim.appearance_left
        )
        val appearance_right : Animation = AnimationUtils.loadAnimation(activity,
            R.anim.appearance_right
        )

        anim_reduction  = AnimationUtils.loadAnimation(activity,
            R.anim.reduction
        )
        disappearance_left  = AnimationUtils.loadAnimation(activity,
            R.anim.disappearance_left
        )
        disappearance_right  = AnimationUtils.loadAnimation(activity,
            R.anim.disappearance_right
        )

        cameraButton = view.findViewById(R.id.imageview_camera)
        pencilButton = view.findViewById(R.id.imageview_pencil)

        background_view = view.findViewById(R.id.view_frame)

        background_view.alpha = 1f

        val sf1 : SharedPreferences = activity!!.getSharedPreferences("count_mainFragment", Context.MODE_PRIVATE)

        count_mainFragment = sf1.getInt("count",0)

        //메인 액티비티에서 이 프래그먼트로 왔을 때

        if(count_mainFragment == 0) {
            cameraButton.animation = appearance_right
            pencilButton.animation = appearance_left
            mainButton.animation = anim_expansion
        }

        //PictureFragment에서 이 프래그먼트로 왔을 때

        else if(count_mainFragment == 1) {

            cameraButton.animation = appearance_right
            pencilButton.animation = appearance_left

            reSize(96)

        }

        mainButton.setOnClickListener {

            val fragmentManager : FragmentManager= activity!!.supportFragmentManager

            mainButton.clearAnimation()
            cameraButton.clearAnimation()
            pencilButton.clearAnimation()

            background_view.alpha = 0f

            mainButton.animation = anim_reduction
            cameraButton.animation = disappearance_left
            pencilButton.animation = disappearance_right

            val sf : SharedPreferences = activity!!.getSharedPreferences("count_fragment", Context.MODE_PRIVATE)
            val editor : SharedPreferences.Editor = sf.edit()

            editor.putInt("count", 0)
            editor.apply()

            Handler().postDelayed({
                fragmentManager.beginTransaction().remove(this).commit()

            }, anim_reduction.duration)

        }

        //버튼 외에 다른 부분 클릭 처리

        background_view.setOnClickListener {

            val fragmentManager : FragmentManager= activity!!.supportFragmentManager

            mainButton.clearAnimation()
            cameraButton.clearAnimation()
            pencilButton.clearAnimation()

            background_view.alpha = 0f

            reSize(80)

            mainButton.animation = anim_reduction
            cameraButton.animation = disappearance_left
            pencilButton.animation = disappearance_right

            Handler().postDelayed({
                fragmentManager.beginTransaction().remove(this).commit()
            }, anim_reduction.duration)

            val sf : SharedPreferences = activity!!.getSharedPreferences("count_fragment", Context.MODE_PRIVATE)
            val editor : SharedPreferences.Editor = sf.edit()

            editor.putInt("count", 0)
            editor.apply()

        }

        //PictureFragment로 넘어가기
        cameraButton.setOnClickListener {

            fragmentManager!!.beginTransaction().remove(this).commit()
            fragmentManager!!.beginTransaction().add(
                R.id.Main_frame,
                PictureFragment()
            ).commit()

            val sf : SharedPreferences = activity!!.getSharedPreferences("count_fragment", Context.MODE_PRIVATE)
            val editor : SharedPreferences.Editor = sf.edit()
            editor.putInt("count", 2)
            editor.apply()

        }

        pencilButton.setOnClickListener {
            startActivity(Intent(activity, MakeSetActivity::class.java))
        }

        return view

    }

    //메인에서 호출되는, 뒤로가기 눌렀을 때 애니메이션을 보여주는 함수

    fun back() {

        val fragmentManager : FragmentManager= activity!!.supportFragmentManager

        mainButton.clearAnimation()
        cameraButton.clearAnimation()
        pencilButton.clearAnimation()

        background_view.alpha = 0f

        //뒤로가기를 눌렀을 때 부자연스러운 현상 해결

        if(count_mainFragment == 1) reSize(80)

        mainButton.animation = anim_reduction
        cameraButton.animation = disappearance_left
        pencilButton.animation = disappearance_right

        val sf : SharedPreferences = activity!!.getSharedPreferences("count_fragment", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sf.edit()

        editor.putInt("count", 0)

        editor.apply()

        Handler().postDelayed({
            fragmentManager.beginTransaction().remove(this).commit()
        }, anim_reduction.duration)

    }

    fun reSize(size_mainButton : Int) {

        val params_mainButton : ViewGroup.LayoutParams = mainButton.layoutParams

        val marginParams_mainButton : ViewGroup.MarginLayoutParams = mainButton.layoutParams as ViewGroup.MarginLayoutParams
        val marginParams_cameraButton : ViewGroup.MarginLayoutParams = cameraButton.layoutParams as ViewGroup.MarginLayoutParams
        val marginParams_pencilButton : ViewGroup.MarginLayoutParams = pencilButton.layoutParams as ViewGroup.MarginLayoutParams

        val scale : Float = context!!.resources.displayMetrics.density

        params_mainButton.width = (size_mainButton * scale + 0.5f).toInt()
        params_mainButton.height = (size_mainButton * scale + 0.5f).toInt()

        val temp : Int = (((size_mainButton - 80) / 2) *scale + 0.5f).toInt()

        marginParams_mainButton.topMargin = (475 * scale + 0.5f).toInt() - temp
        marginParams_cameraButton.leftMargin = (60 * scale + 0.5f).toInt() - temp
        marginParams_pencilButton.rightMargin = (60 * scale + 0.5f).toInt() - temp

        mainButton.layoutParams = params_mainButton

        mainButton.layoutParams = marginParams_mainButton
        cameraButton.layoutParams = marginParams_cameraButton
        pencilButton.layoutParams = marginParams_pencilButton

    }

}