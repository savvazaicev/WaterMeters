package com.company.watermeters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText

class AuthActivity : AppCompatActivity() {
    private lateinit var btnSignIn: Button
    private var firebaseAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var users: DatabaseReference? = null
    private var gso: GoogleSignInOptions? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var sharedPref: SharedPreferences? = null
    private lateinit var root: RelativeLayout

    companion object {
        const val USER_ID = "userId"
        const val EMAIL = "email"
        const val PASSWORD = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        root = findViewById(R.id.root_element)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences("SaveData", Context.MODE_PRIVATE)
        val idToken = sharedPref?.getString(USER_ID, null)
        signOut()
        if (intent.getBooleanExtra("actionExit", false)) {
            saveAuthData(null, null,null)
        } else {
            authWithSavedData()
        }
        val user = firebaseAuth?.currentUser
        if (user?.uid == null || idToken != user.uid) {
            db = FirebaseDatabase.getInstance()
            users = db!!.getReference("Users")

            btnSignIn = findViewById(R.id.btnSignIn)
            btnSignIn.setOnClickListener {
                showSignInWindow()
            }
        }
    }

    private fun showSignInWindow() {
        val email = findViewById<TextInputEditText>(R.id.emailField)
        val password = findViewById<TextInputEditText>(R.id.passField)
        when {
            TextUtils.isEmpty(email.text.toString()) -> {
                Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show()
            }
            password.text.toString().length < 5 -> {
                Snackbar.make(
                    root,
                    "Введите пароль, содержащий более 5 символов",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            //Вход пользователя
            else -> {
                firebaseAuth?.signInWithEmailAndPassword(
                    email.text.toString(),
                    password.text.toString()
                )
                    ?.addOnSuccessListener {
                        saveAuthData(
                            firebaseAuth?.currentUser?.uid,
                            email.text.toString(),
                            password.text.toString()
                        )
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    ?.addOnFailureListener {
                        Snackbar.make(root, "Ошибка авторизации", Snackbar.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun saveAuthData(uid: String?, email: String?, password: String?) {
        val sharedPref = getSharedPreferences("SaveData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(USER_ID, uid)
        editor.putString(EMAIL, email)
        editor.putString(PASSWORD, password)
        editor.apply()
    }

    private fun authWithSavedData() {
        val email = sharedPref?.getString(EMAIL, null)
        val password = sharedPref?.getString(PASSWORD, null)
        if (email != null && password != null) {
            firebaseAuth?.signInWithEmailAndPassword(email, password)
                ?.addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                ?.addOnFailureListener {
                    Snackbar.make(root, "Ошибка авторизации", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun signOut() {
        gso = GoogleSignInOptions.DEFAULT_SIGN_IN
        googleSignInClient = gso?.let { GoogleSignIn.getClient(this, it) }
        FirebaseAuth.getInstance().signOut()
        Auth.GoogleSignInApi.signOut(googleSignInClient?.asGoogleApiClient())
    }
}