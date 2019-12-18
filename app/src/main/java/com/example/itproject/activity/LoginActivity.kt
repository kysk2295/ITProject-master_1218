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
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_loading, null))
        builder.setCancelable(false)
        val dialog : AlertDialog = builder.create()

        LoginActivity_LoginBtn.setOnClickListener {

            val email : String = LoginActivity_email.text.toString()
            val password : String = LoginActivity_password.text.toString()

            if(email.isEmpty() || !email.contains('@'))
                Toast.makeText(applicationContext, "이메일을 입력해 주세요", Toast.LENGTH_SHORT).show()
            else if(email.isEmpty())
                Toast.makeText(applicationContext, "비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show()

            else {
                dialog.show()
                val firebaseAuth = FirebaseAuth.getInstance()

                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            dialog.dismiss()
                            Toast.makeText(applicationContext, "로그인되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                        }
                        else {
                            Toast.makeText(applicationContext, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
            }
        }

        LoginActivity_JoinBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}
