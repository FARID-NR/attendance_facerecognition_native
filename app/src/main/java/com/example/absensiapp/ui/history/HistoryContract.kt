package com.example.absensiapp.ui.history

import com.example.absensiapp.base.BasePresenter
import com.example.absensiapp.base.BaseView
import com.example.absensiapp.model.response.history.HistoryResponse

interface HistoryContract {

    interface View: BaseView {
        fun onHistorySucces(historyResponse: HistoryResponse)
        fun onHistoryFailed(message:String)

    }

    interface Presenter:HistoryContract, BasePresenter {
        fun getHistory(tanggal:String)
    }
}