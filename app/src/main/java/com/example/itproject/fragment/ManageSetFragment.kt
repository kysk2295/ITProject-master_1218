package com.example.itproject.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itproject.MSItem
import com.example.itproject.R
import com.example.itproject.activity.MainActivity
import com.example.itproject.activity.SetActivity
import com.example.itproject.adapter.ManageSetAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class ManageSetFragment : Fragment() {

    private lateinit var dialog : AlertDialog
    private val array_title : ArrayList<String> = ArrayList()
    private lateinit var db : FirebaseFirestore
    private lateinit var tmp : CollectionReference
    private val list : ArrayList<MSItem> = ArrayList()
    private lateinit var recycler : RecyclerView
    private var adapter : ManageSetAdapter? = null
    private lateinit var emptyItem : MSItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.frame_manage_set, container, false)
        val alertBuilder : AlertDialog.Builder = AlertDialog.Builder(context!!)
        alertBuilder.setView(inflater.inflate(R.layout.dialog_loading, null))
        alertBuilder.setCancelable(false)
        dialog = alertBuilder.create()
        dialog.show()
        db = FirebaseFirestore.getInstance()
        val email = FirebaseAuth.getInstance().currentUser!!.email!!
        tmp = db.collection("users").document(email).collection("sets")
        recycler = view.findViewById(R.id.ManageSet_recycler)
        val search : EditText = view.findViewById(R.id.ManageSet_edit)
        search.addTextChangedListener(MyTextWatcher())

        emptyItem = MSItem()
        setRecycler()

        (activity as MainActivity).getTrashBtn().setOnClickListener {
            val builder : AlertDialog.Builder = AlertDialog.Builder(context!!)
            builder.setMessage("삭제하시겠습니까?")
            builder.setPositiveButton("삭제"
            ) { _, _ ->
                adapter!!.deleteItems()
                search.setText("")
            }
            builder.setNegativeButton("취소"
            ) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
        return view
    }

    private fun setRecycler() {
        tmp.get()
            .addOnSuccessListener {
                for(doc in it) {
                    array_title.add(doc.id)
                }
                //makeList(0)
                for(i in 0 until array_title.size) {
                    list.add(emptyItem)
                    makeList(i)
                }
            }
    }

    private fun makeList(i : Int) {
        val title : String = array_title[i]
        tmp.document(title).get()
            .addOnSuccessListener {
                val subtitle = it["subtitle"].toString()
                val progress = (it["progress"] as Double).toFloat()
                list[i] = MSItem(title, subtitle, progress)
                if(!list.contains(emptyItem)) {
                    setAdapter()
                }
            }
    }

    private fun setAdapter() {

        val onItemCheck : ManageSetAdapter.OnItemCheckListener = object : ManageSetAdapter.OnItemCheckListener {
            override fun onItemCheck(index : Int) {
                adapter!!.setSelectedArray(index, true)
            }
            override fun onItemUncheck(index: Int) {
                adapter!!.setSelectedArray(index, false)
            }
        }
        adapter = ManageSetAdapter(
            list,
            onItemCheck,
            this
        )
        recycler.layoutManager = LinearLayoutManager(activity)
        recycler.adapter = adapter
        dialog.dismiss()
    }

    inner class MyTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if(adapter != null) {
                adapter!!.filter(s.toString())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }

    fun moveToSet(title : String, subtitle : String) {
        val intent = Intent(activity, SetActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("subtitle", subtitle)
        startActivity(intent)
    }
}