package com.example.itproject.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.itproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_loading, null))
        builder.setCancelable(false)
        val dialog : AlertDialog = builder.create()

        SignUp_Btn.setOnClickListener {
            val name : String = SignUp_name.text.toString()
            val email : String = Signup_email.text.toString()
            val password : String = SignUp_password.text.toString()

            if(name.isEmpty())
                Toast.makeText(applicationContext, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show()

            else if(email.isEmpty() || !email.contains('@'))
                Toast.makeText(applicationContext, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()

            else if(password.isEmpty())
                Toast.makeText(applicationContext, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()

            else {
                dialog.show()
                val auth = FirebaseAuth.getInstance()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            Toast.makeText(applicationContext, "회원가입에 성공하였습니다", Toast.LENGTH_SHORT).show()
                            val db = FirebaseFirestore.getInstance()
                            val userName = hashMapOf("name" to name)
                            db.collection("users").document(email).set(userName)
                            dialog.dismiss()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }

                        else {
                            Toast.makeText(applicationContext, "회원가입에 실패하였습니다", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
            }
        }
    }

}
