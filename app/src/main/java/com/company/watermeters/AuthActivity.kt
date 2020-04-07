package com.company.watermeters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.company.watermeters.model.User
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText

class AuthActivity : AppCompatActivity() {
    private lateinit var btnSignIn: Button
    private var btnRegister: Button? = null
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
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val idToken = sharedPref?.getString(USER_ID, null)
        signOut()
        authWithSavedData()
        val user = firebaseAuth?.currentUser
        if (user?.uid == null || idToken != user?.uid) {
            db = FirebaseDatabase.getInstance()
            users = db!!.getReference("Users")

            btnSignIn = findViewById(R.id.btnSignIn)
//            btnRegister = findViewById(R.id.btnRegister)
//            btnRegister?.setOnClickListener {
//                showRegisterWindow()
//            }
            btnSignIn.setOnClickListener {
                showSignInWindow()
            }
        }
    }

    private fun showSignInWindow() {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("Войти")
        dialog.setMessage("Введите данные для входа")

        val inflater: LayoutInflater = LayoutInflater.from(this)
        val signInWindow: View = inflater.inflate(R.layout.sign_in_window, null)
        dialog.setView(signInWindow)

        val email: MaterialEditText = signInWindow.findViewById(R.id.emailField)
        val password: MaterialEditText = signInWindow.findViewById(R.id.passField)

        dialog.setNegativeButton(
            "Отменить"
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        dialog.setPositiveButton(
            "Войти"
        ) { _, _ ->
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
                            //Snackbar.make(root, "Ошибка авторизации", Snackbar.LENGTH_SHORT).show()
                        }
                }
            }
        }
        dialog.show()
    }

    private fun saveAuthData(uid: String?, email: String?, password: String?) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(USER_ID, uid)
        editor.putString(EMAIL, email)
        editor.putString(PASSWORD, password)
        editor.apply()
    }

//    private fun showRegisterWindow() {
//        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
//        dialog.setTitle("Зарегистрироваться")
//        dialog.setMessage("Введите все данные для регистрации")
//
//        val inflater: LayoutInflater = LayoutInflater.from(this)
//        val registerWindow: View = inflater.inflate(R.layout.register_window, null)
//        dialog.setView(registerWindow)
//
//        val email: MaterialEditText = registerWindow.findViewById(R.id.emailField)
//        val pass: MaterialEditText = registerWindow.findViewById(R.id.passField)
//        val name: MaterialEditText = registerWindow.findViewById(R.id.nameField)
//        val phone: MaterialEditText = registerWindow.findViewById(R.id.phoneField)
//
//        dialog.setNegativeButton(
//            "Отменить"
//        ) { dialogInterface, _ ->
//            dialogInterface.dismiss()
//        }
//
//        dialog.setPositiveButton(
//            "Добавить"
//        ) { _, _ ->
//            when {
//                TextUtils.isEmpty(email.text.toString()) -> {
//                    Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show()
//                }
//                TextUtils.isEmpty(name.text.toString()) -> {
//                    Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show()
//                }
//                TextUtils.isEmpty(phone.text.toString()) -> {
//                    Snackbar.make(root, "Введите ваш телефон", Snackbar.LENGTH_SHORT).show()
//                }
//                pass.text.toString().length < 5 -> {
//                    Snackbar.make(
//                        root,
//                        "Введите пароль, содержащий более 5 символов",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                }
//
//                //Регистрация пользователя
//                else -> {
//                    firebaseAuth?.createUserWithEmailAndPassword(
//                        email.text.toString(),
//                        pass.text.toString()
//                    )
//                        ?.addOnSuccessListener {
//                            val user = User(
//                                name.text.toString(),
//                                email.text.toString(),
//                                pass.text.toString(),
//                                phone.text.toString()
//                            )
//
//                            FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
//                                users?.child(it1)?.setValue(user)
//                                    ?.addOnSuccessListener {
//                                        Snackbar.make(
//                                            root,
//                                            "Пользователь добавлен!",
//                                            Snackbar.LENGTH_SHORT
//                                        )
//                                            .show()
//                                    }
//                            }
//                        }?.addOnFailureListener {
//                            Snackbar.make(root, "Ошибка регистрации", Snackbar.LENGTH_LONG).show()
//                        }
//                }
//            }
//        }
//        dialog.show()
//    }

    private fun authWithSavedData() {
        val email = sharedPref?.getString(EMAIL, null)
        val password = sharedPref?.getString(PASSWORD, null)
        if (email != null && password != null) {
            firebaseAuth?.signInWithEmailAndPassword(email, password)
                ?.addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
//                ?.addOnFailureListener {
//                    Snackbar.make(root, "Ошибка авторизации", Snackbar.LENGTH_SHORT).show()
//                }
        }
    }

    private fun signOut() {
        gso = GoogleSignInOptions.DEFAULT_SIGN_IN
        googleSignInClient = gso?.let { GoogleSignIn.getClient(this, it) }
        FirebaseAuth.getInstance().signOut()
        Auth.GoogleSignInApi.signOut(googleSignInClient?.asGoogleApiClient())
    }
}