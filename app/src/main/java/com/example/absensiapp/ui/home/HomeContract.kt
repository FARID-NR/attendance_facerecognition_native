package com.example.absensiapp.ui.home

import com.example.absensiapp.base.BasePresenter
import com.example.absensiapp.base.BaseView
import com.example.absensiapp.model.response.home.GetUserResponse
import com.example.absensiapp.model.response.login.LoginResponse

interface HomeContract {

    interface View: BaseView {
        fun onHomeSucces(homeResponse: GetUserResponse)
        fun onHomeFailed(message:String)

    }

    interface Presenter:HomeContract, BasePresenter {
        fun getHome()
    }
}