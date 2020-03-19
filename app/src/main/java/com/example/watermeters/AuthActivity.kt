package com.example.watermeters

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.watermeters.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import java.lang.Exception

class AuthActivity : AppCompatActivity() {
    private lateinit var btnSignIn: Button
    private var btnRegister: Button? = null
    private var auth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var users: DatabaseReference? = null

    private lateinit var root: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        btnSignIn = findViewById(R.id.btnSignIn)
        btnRegister = findViewById(R.id.btnRegister)

        root = findViewById(R.id.root_element)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        users = db!!.getReference("Users")

        btnRegister?.setOnClickListener {
            showRegisterWindow()
        }
        btnSignIn.setOnClickListener {
            showSignInWindow()
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
        val pass: MaterialEditText = signInWindow.findViewById(R.id.passField)

        dialog.setNegativeButton(
            "Отменить"
        ) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        dialog.setPositiveButton(
            "Войти"
        ) { dialogInterface, which ->
            when {
                TextUtils.isEmpty(email.text.toString()) -> {
                    Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show()
                }
                pass.text.toString().length < 5 -> {
                    Snackbar.make(
                        root,
                        "Введите пароль, содержащий более 5 символов",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                //Вход пользователя
                else -> auth?.signInWithEmailAndPassword(email.text.toString(), pass.text.toString())
                    ?.addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    ?.addOnFailureListener {
                        Snackbar.make(root, "Ошибка авторизации", Snackbar.LENGTH_SHORT).show()
                    }
            }
        }
        dialog.show()
    }

    private fun showRegisterWindow() {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("Зарегистрироваться")
        dialog.setMessage("Введите все данные для регистрации")

        val inflater: LayoutInflater = LayoutInflater.from(this)
        val registerWindow: View = inflater.inflate(R.layout.register_window, null)
        dialog.setView(registerWindow)

        val email: MaterialEditText = registerWindow.findViewById(R.id.emailField)
        val pass: MaterialEditText = registerWindow.findViewById(R.id.passField)
        val name: MaterialEditText = registerWindow.findViewById(R.id.nameField)
        val phone: MaterialEditText = registerWindow.findViewById(R.id.phoneField)

        dialog.setNegativeButton(
            "Отменить"
        ) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        dialog.setPositiveButton(
            "Добавить"
        ) { dialogInterface, which ->
            when {
                TextUtils.isEmpty(email.text.toString()) -> {
                    Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(name.text.toString()) -> {
                    Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(phone.text.toString()) -> {
                    Snackbar.make(root, "Введите ваш телефон", Snackbar.LENGTH_SHORT).show()
                }
                pass.text.toString().length < 5 -> {
                    Snackbar.make(
                        root,
                        "Введите пароль, содержащий более 5 символов",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                //Регистрация пользователя
                else -> auth?.createUserWithEmailAndPassword(
                        email.text.toString(),
                        pass.text.toString()
                    )
                    ?.addOnSuccessListener {
                        val user = User(
                            email.text.toString(),
                            name.text.toString(),
                            pass.text.toString(),
                            phone.text.toString()
                        )

                        FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
                            users?.child(it1)?.setValue(user)
                                ?.addOnSuccessListener {
                                    Snackbar.make(root, "Пользователь добавлен!", Snackbar.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    }?.addOnFailureListener {
                        Snackbar.make(root, "Ошибка регистрации", Snackbar.LENGTH_LONG).show()
                    }
            }
        }
        dialog.show()
    }
}