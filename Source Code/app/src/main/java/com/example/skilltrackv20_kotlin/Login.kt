package com.example.skilltrackv20_kotlin

import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    // Variables
    private lateinit var imgBackground: ImageView
    private lateinit var tvHeader: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnReg: Button
    private lateinit var btnCancel: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Type cast the variables
        tvHeader = findViewById(R.id.tvHeader)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnCancel = findViewById(R.id.btnCancel)
        btnReg = findViewById(R.id.btnReg)
        mAuth = FirebaseAuth.getInstance()

        // Type cast the image view
        imgBackground = findViewById(R.id.imgBackground)
        // Use Glide to animate image with gif
        Glide.with(this).asGif().load(R.drawable.loginani).into(imgBackground)

        // Login button onClick Listener
        btnLogin.setOnClickListener {
            loginUser(it)
        }
    }

    fun loginUser(view: View) {
        try {
            // Get the user input
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Check if email is valid
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter an email address!", Toast.LENGTH_SHORT).show()
                etEmail.requestFocus()
                return
            }

            // Check if password is valid
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter a password!", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
                return
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        // Login successful --> Goto about
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, About::class.java))
                        finish()
                    } else {
                        // Login failed --> Redo login
                        Toast.makeText(this, "Login Unsuccessful! Please try again.", Toast.LENGTH_SHORT).show()
                        etEmail.setText("")
                        etPassword.setText("")
                        etEmail.requestFocus()
                    }
                }
        } catch (exception: Exception) {
            Toast.makeText(this, "Error Occurred: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    // Cancel method
    fun cancelFields(view: View) {
        Toast.makeText(this, "Login option cancelled", Toast.LENGTH_SHORT).show()
        etEmail.setText("")
        etPassword.setText("")
        etEmail.requestFocus()
    }

    // Register method
    fun regScreen(view: View) {
        startActivity(Intent(applicationContext, Register::class.java))
    }
}
