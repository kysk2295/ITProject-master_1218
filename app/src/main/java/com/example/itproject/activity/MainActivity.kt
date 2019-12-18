package com.example.itproject.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.itproject.fragment.MainFragment
import com.example.itproject.fragment.ManageSetFragment
import com.example.itproject.fragment.PictureFragment
import com.example.itproject.R
import com.example.itproject.fragment.SearchFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import com.yarolegovich.slidingrootnav.callback.DragStateListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainButton : CircleImageView
    private lateinit var fragmentManager : FragmentManager
    private lateinit var nav : SlidingRootNav
    private lateinit var actionBar : ActionBar
    private lateinit var builder : SlidingRootNavBuilder
    private lateinit var dialog : AlertDialog
    private var count = 0
    private var where : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        else {
            setContentView(R.layout.activity_main)
            setSupportActionBar(Main_toolbar)

            fragmentManager = supportFragmentManager

            val alertBuilder : AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            alertBuilder.setView(inflater.inflate(R.layout.dialog_loading, null))
            alertBuilder.setCancelable(false)
            dialog = alertBuilder.create()
            dialog.show()

            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(firebaseAuth.currentUser!!.email.toString()).collection("sets")
                .get().addOnCompleteListener {
                    if(it.isSuccessful) {
                        var i = 0
                        for(document in it.result!!) {
                            i++
                        }
                        count = i
                        val sf : SharedPreferences = getSharedPreferences("count_sets", Context.MODE_PRIVATE)
                        val et: SharedPreferences.Editor = sf.edit()
                        et.putInt("sets", count).apply()
                        val tmp = "보유 중인 학습 세트 : ${count}개"
                        textview_allSet_main.text = tmp
                        dialog.dismiss()
                    }
                }
            builder = SlidingRootNavBuilder(this)
            setDragStateListener()
            nav = makeNav()
            actionBar = supportActionBar!!
            actionBar.setDisplayShowCustomEnabled(true)

            actionBar.setDisplayShowTitleEnabled(false)

            val permissionListener = object : PermissionListener
            {
                override fun onPermissionGranted() {}
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    finish()
                }
            }

            TedPermission.with(applicationContext)
                .setPermissionListener(permissionListener)
                .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()

            mainButton = findViewById(R.id.circleImgview_main)

            mainButton.setOnClickListener {

                if(nav.isMenuClosed) {
                    fragmentManager.beginTransaction().add(
                        R.id.Main_frame,
                        MainFragment()
                    ).commit()

                    val sf : SharedPreferences = getSharedPreferences("count_fragment", Context.MODE_PRIVATE)
                    val sf1 : SharedPreferences = getSharedPreferences("count_mainFragment", Context.MODE_PRIVATE)

                    val editor : SharedPreferences.Editor = sf.edit()
                    val editor1 : SharedPreferences.Editor = sf1.edit()

                    editor.putInt("count", 1)
                    editor1.putInt("count", 0)

                    editor.apply()
                    editor1.apply()

                }

            }

            textview_allSee_main.setOnClickListener {
                val f = fragmentManager.fragments
                val msf = fragmentManager.findFragmentByTag("ManageSet")
                when {
                    f.size == 0 -> {
                        fragmentManager.beginTransaction().add(R.id.Main_frame_sub, ManageSetFragment(), "ManageSet").commit()
                    }
                    f.contains(msf) -> {
                        fragmentManager.beginTransaction().show(msf!!).commit()
                    }
                    else -> {
                        fragmentManager.beginTransaction().replace(R.id.Main_frame_sub, ManageSetFragment(), "ManageSet").commit()
                    }
                }
                where = 1
                setToolbarTitle("학습 세트 관리")
                ManageSet_trash.visibility = View.VISIBLE
            }

            Main_background.setOnClickListener{
                nav.closeMenu()
            }

            Menu_home.setOnClickListener {
                when(where) {
                    0 -> { // home
                        nav.closeMenu()
                    }
                    1 -> {
                        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("ManageSet")!!).commit()
                        setToolbarTitle("")
                        nav.closeMenu()
                        where = 0
                        count = getSetsSize()
                        textview_allSet_main.text = "보유 중인 학습 세트 : ${count}개"
                        ManageSet_trash.visibility = View.GONE
                    }
                }
            }

            Menu_manage.setOnClickListener {
                where = 1
                if(fragmentManager.findFragmentByTag("ManageSet") == null) {
                    fragmentManager.beginTransaction().add(
                        R.id.Main_frame_sub,
                        ManageSetFragment(), "ManageSet").commit()
                }
                else {
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("ManageSet")!!).commit()
                }
                setToolbarTitle("학습 세트 관리")
                ManageSet_trash.visibility = View.VISIBLE
                nav.closeMenu()
            }

            Menu_search.setOnClickListener {
                where = 2
                if(fragmentManager.findFragmentByTag("Search") == null)
                    fragmentManager.beginTransaction().add(R.id.Main_frame_sub, SearchFragment(), "Search").commit()
                else
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Search")!!).commit()
                setToolbarTitle("학습 세트 검색")
                ManageSet_trash.visibility = View.GONE
                nav.closeMenu()
            }


            Menu_logout.setOnClickListener {
                val builder_ : AlertDialog.Builder = AlertDialog.Builder(this)
                builder_.setMessage("로그아웃하시겠습니까?")
                builder_.setPositiveButton("로그아웃") { _, _ ->
                    firebaseAuth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                builder_.setNegativeButton("취소") { dialog, _ ->
                    dialog.cancel()
                }
                builder_.show()
            }
        }
    }

    //뒤로가기를 눌렀을 때 어떤 프래그먼트인지 체크해서 애니메이션을 보여줌

    override fun onBackPressed() {
        val fragment : Fragment? = supportFragmentManager.findFragmentById(R.id.Main_frame)

        if(fragment != null) {

            val sf : SharedPreferences = getSharedPreferences("count_fragment", Context.MODE_PRIVATE)
            val count : Int = sf.getInt("count", 0)

            if(count == 1) {
                val mainFragment : MainFragment = supportFragmentManager.findFragmentById(
                    R.id.Main_frame
                ) as MainFragment
                mainFragment.back()
            }

            if(count == 2) {
                val pictureFragment : PictureFragment = supportFragmentManager.findFragmentById(
                    R.id.Main_frame
                ) as PictureFragment
                pictureFragment.back()
            }

        }

        else {
            if(nav.isMenuOpened) nav.closeMenu()
            else super.onBackPressed()
        }
    }

    private fun makeNav() : SlidingRootNav{

        val dm : DisplayMetrics = applicationContext.resources.displayMetrics
        val width = (dm.widthPixels * 0.35).toInt()
        return builder.withMenuLayout(R.layout.menu)
            .withDragDistancePx(width)
            .withRootViewScale(0.7f)
            .withRootViewElevation(10)
            .withRootViewYTranslation(4)
            .withToolbarMenuToggle(Main_toolbar)
            .inject()
    }

    private fun setDragStateListener() {

        val listener : DragStateListener = object : DragStateListener {

            override fun onDragEnd(isMenuOpened: Boolean) {
                if(isMenuOpened)
                    Main_background.visibility = View.VISIBLE
                else {
                    Main_background.visibility = View.INVISIBLE
                }
            }
            override fun onDragStart() {}
        }
        builder.addDragStateListener(listener)
    }

    override fun onResume() {
        super.onResume()
        val count_ : Int = getSetsSize()
        textview_allSet_main.text = "보유 중인 학습 세트 : ${count_}개"
        if(count_ != count) {
            removeMS()
            count = count_
        }
        if(shouldRefresh()) {
            refreshMS()
            val edit = getSharedPreferences("shouldRefresh", Context.MODE_PRIVATE).edit()
            edit.putBoolean("shouldRefresh", false).apply()
        }
    }

    private fun setToolbarTitle(s : String) {
        Main_toolbar_title.text = s
    }

    private fun refreshMS() {
        val f = fragmentManager.findFragmentByTag("ManageSet")
        if(f != null) {
            fragmentManager.beginTransaction().remove(f).commit()
            fragmentManager.beginTransaction().add(R.id.Main_frame_sub, ManageSetFragment(), "ManageSet").commit()
        }
    }

    private fun removeMS() {
        val f = fragmentManager.findFragmentByTag("ManageSet")
        if(f != null) {
            fragmentManager.beginTransaction().remove(f).commit()
        }
    }

    private fun getSetsSize() : Int{
        val sf : SharedPreferences = getSharedPreferences("count_sets", Context.MODE_PRIVATE)
        return sf.getInt("sets", 0)
    }

    fun getTrashBtn() : ImageView {
        val v : ImageView = findViewById(R.id.ManageSet_trash)
        return v
    }

    private fun shouldRefresh() : Boolean {
        val sf : SharedPreferences = getSharedPreferences("shouldRefresh", Context.MODE_PRIVATE)
        return sf.getBoolean("shouldRefresh", false)
    }

}
