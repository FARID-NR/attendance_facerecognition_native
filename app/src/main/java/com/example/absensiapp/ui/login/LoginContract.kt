package com.example.absensiapp.ui.login

import com.example.absensiapp.base.BasePresenter
import com.example.absensiapp.base.BaseView
import com.example.absensiapp.model.response.login.LoginResponse

interface LoginContract {

    interface View: BaseView {
        fun onLoginSucces(loginResponse: LoginResponse)
        fun onLoginFailed(message:String)

    }

    interface Presenter:LoginContract, BasePresenter {
        fun submitLogin(email: String, password: String)
    }
}