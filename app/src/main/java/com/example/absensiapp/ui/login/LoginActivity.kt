package com.example.absensiapp.ui.login

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.absensiapp.AbsensiApp
import com.example.absensiapp.R
import com.example.absensiapp.model.response.login.LoginResponse
import com.example.absensiapp.ui.MainActivity
import com.google.gson.Gson

class LoginActivity : AppCompatActivity(), LoginContract.View {

    lateinit var presenter: LoginPresenter
    var progressDialog : Dialog? = null
    lateinit var etEmail: EditText // Tambahkan ini untuk menyimpan referensi EditText
    lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        presenter = LoginPresenter(this)

        val token = AbsensiApp.getApp().getToken()

        println("ini adalah token ${token}")

        if (!token.isNullOrEmpty()) {
            val home = Intent(this, MainActivity::class.java)
            startActivity(home)
            this?.finish()
        }

        initView()

        // Inisialisasi etEmail dan etPassword dari layout XML
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        // Asumsikan ada tombol login di layout activity_login.xml dengan id "buttonLogin"
        val buttonLogin: Button = findViewById(R.id.btn_login)

        buttonLogin.setOnClickListener {

            // Pindah ke HomePageActivity
            var email = etEmail.text.toString()
            var password = etPassword.text.toString()

            if (email.isNullOrEmpty()) {
                etEmail.error = "Silahkan masukkan Email"
                etEmail.requestFocus()
            } else if (password.isNullOrEmpty()) {
                etPassword.error = "Silahkan masukkan Password"
                etPassword.requestFocus()
            } else {
                Log.d("LoginActivity", "Email: $email, Password: $password")
                presenter.submitLogin(email, password)
            }
        }
    }

    override fun onLoginSucces(loginResponse: LoginResponse) {

        AbsensiApp.getApp().setToken(loginResponse.token)

        val gson = Gson()
        val json = gson.toJson(loginResponse.user)
        AbsensiApp.getApp().setUser(json)
        Log.d("LoginActivity", "Login failed : ${AbsensiApp.getApp().setToken(loginResponse.token)}")

        // Log untuk memastikan token disimpan
        Log.d("LoginActivity", "Login berhasil, menyimpan token: ${loginResponse.token}")

        val home = Intent(this, MainActivity::class.java)
        startActivity(home)
        this?.finish()
    }


    override fun onLoginFailed(message: String) {
        Log.e("LoginActivity", "Login failed: $message")
        Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
    }

    private fun initDummy() {
        etEmail.setText("kha@gmail.com")
        etPassword.setText("12345678")
    }

    private fun initView() {
        progressDialog = Dialog(this)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_loader, null)

        progressDialog?.let {
            it.setContentView(dialogLayout)
            it.setCancelable(false)
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun showLoading() {
        progressDialog?.show()
    }

    override fun dismissLoading() {
        progressDialog?.dismiss()
    }
}