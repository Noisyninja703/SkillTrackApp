package com.example.skilltrackv20_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    // Variables
    private lateinit var imgBackground: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var edRegEmail: EditText
    private lateinit var edRegPassword: EditText
    private lateinit var edRegConPassword: EditText
    private lateinit var btnReg: Button
    private lateinit var btnRegCancel: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Type cast the input variables
        tvTitle = findViewById(R.id.TvHeader)
        edRegEmail = findViewById(R.id.EtEmail)
        edRegPassword = findViewById(R.id.EtPassword)
        edRegConPassword = findViewById(R.id.EtConfirmPassword)
        btnReg = findViewById(R.id.BtnRegister)
        btnRegCancel = findViewById(R.id.BtnCancel)
        mAuth = FirebaseAuth.getInstance()

        // Type cast the image view
        imgBackground = findViewById(R.id.imgBackground)
        // Use Glide to animate image with gif
        Glide.with(this).asGif().load(R.drawable.regani).into(imgBackground)

        // Register button onClick Listener
        btnReg.setOnClickListener {
            regUser(it)
        }
    }

    // Register button code
    private fun regUser(view: View) {

        // If the reg btn is clicked --> nesting
        if (view.id == R.id.BtnRegister) {
            // Fetch the values from the 3 edit text boxes
            val email = edRegEmail.text.toString().trim()
            val password = edRegPassword.text.toString().trim()
            val conPassword = edRegConPassword.text.toString().trim()

            // Checks ... not allow --> blank input, invalid input
            // Password > 8 characters
            if (edRegPassword.text.toString().length < 8) {
                edRegPassword.error = "password minimum contain 8 characters"
                edRegPassword.requestFocus()
                return
            }

            // Passwords match
            if (password != conPassword) {
                edRegConPassword.error = "passwords do not match"
                edRegConPassword.requestFocus()
                return
            }

            // Valid Email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edRegEmail.error = "please enter a valid email address"
                edRegEmail.requestFocus()
                return
            }

            // Email Empty
            if (edRegEmail.text.toString().isEmpty()) {
                edRegEmail.error = "please enter an email address"
                edRegEmail.requestFocus()
                return
            }

            // If all valid --> try to register user
            if (!email.isEmpty() && edRegPassword.text.toString().length >= 8 &&
                !edRegEmail.text.toString().trim().isEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && (password == conPassword)
            ) {
                // Add an onCompleteListener to create user
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            // Registration successful --> Goto login
                            Toast.makeText(this, "Registered User successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(applicationContext, Login::class.java))
                            finish()
                        } else {
                            // Registration failed --> Redo registration
                            edRegEmail.error = "Email is Taken"
                        }
                    }
                finish()
            }
        }
    }

    // Cancel btn method
    fun cancelFields(view: View) {
        Toast.makeText(this, "Register option cancelled", Toast.LENGTH_SHORT).show()
        edRegEmail.setText("")
        edRegPassword.setText("")
        edRegConPassword.setText("")
        edRegEmail.requestFocus()
    }
}
