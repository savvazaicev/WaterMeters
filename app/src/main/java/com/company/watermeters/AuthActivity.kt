package com.company.watermeters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.company.watermeters.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

class AuthActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val USER_ID = "userId"
        const val EMAIL = "email"
        const val PASSWORD = "password"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private var sharedPref: SharedPreferences? = null
    private lateinit var binding: ActivityAuthBinding
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences("SaveData", Context.MODE_PRIVATE)
        reAuth()
        binding.btnSignIn.setOnClickListener {
            launch { signIn() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    private fun reAuth() {
        signOut()
        if (intent.getBooleanExtra("actionExit", false)) {
            saveAuthData(null, null, null)
        } else {
            launch { authWithSavedData() }
        }
    }

    private suspend fun signIn() {
        val email = binding.emailField
        val password = binding.passField
        if (validateFields(email, password)) {
            firebaseAuth.signInWithEmailAndPassword(
                email.text.toString(),
                password.text.toString()
            )
                .addOnSuccessListener {
                    saveAuthData(
                        firebaseAuth.currentUser?.uid,
                        email.text.toString(),
                        password.text.toString()
                    )
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    snackBar(getString(R.string.authError))
                }
                .await()
        }
    }

    private suspend fun authWithSavedData() {
        val email = sharedPref?.getString(EMAIL, null)
        val password = sharedPref?.getString(PASSWORD, null)
        if (email != null && password != null) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    snackBar(getString(R.string.authError))
                }
                .await()
        }
    }

    private fun signOut() {
        val gso = GoogleSignInOptions.DEFAULT_SIGN_IN
        val googleSignInClient = gso?.let { GoogleSignIn.getClient(this, it) }
        FirebaseAuth.getInstance().signOut()
        Auth.GoogleSignInApi.signOut(googleSignInClient?.asGoogleApiClient())
    }

    private fun validateFields(email: TextInputEditText, password: TextInputEditText): Boolean =
        when {
            TextUtils.isEmpty(email.text.toString()) -> {
                snackBar(getString(R.string.enterYourMail))
                false
            }
            password.text.toString().length < 5 -> {
                snackBar(getString(R.string.needMoreThanFiveCharacters))
                false
            }
            else -> true
        }

    private fun saveAuthData(uid: String?, email: String?, password: String?) {
        sharedPref?.edit()?.apply {
            putString(USER_ID, uid)
            putString(EMAIL, email)
            putString(PASSWORD, password)
            apply()
        }
    }

    private fun snackBar(str: String) =
        Snackbar.make(binding.root, str, Snackbar.LENGTH_SHORT).show()
}