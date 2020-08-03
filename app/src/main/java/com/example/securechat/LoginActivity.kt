package com.example.securechat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton.setOnClickListener {
            performLogin()
        }

    }

    private fun performLogin() {
        val email = loginEmail.text.toString()
        val password = loginPassword.text.toString()

        if (email.isEmpty() or password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is: $email")
        Log.d("RegisterActivity", "Password is: $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("Login", "Successfully logged in with uid: ${it.result?.user?.uid}")
            }
            .addOnFailureListener {
                Log.d("Login", "Failed to login user: ${it.message}")
                Toast.makeText(this, "Failed to login user: ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }


}