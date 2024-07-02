package com.example.absensiapp.ui.history

import com.example.absensiapp.network.HttpClient
import com.example.absensiapp.ui.login.LoginContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HistoryPresenter (private val view:HistoryContract.View) : HistoryContract.Presenter {

    private val mCompositeDisposable : CompositeDisposable?

    init {
        this.mCompositeDisposable = CompositeDisposable()
    }

    override fun getHistory(tanggal: String) {
        view.showLoading()
        val disposable = HttpClient.getInstance().getApi()!!.history(tanggal)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {response ->
                    view.dismissLoading() // Tutup loading setelah login sukses
                    view.onHistorySucces(response)
                },
                {
                    view.dismissLoading()
                    view.onHistoryFailed(it.message ?: "Unknown Error")
                }
            )
        mCompositeDisposable!!.add(disposable)
    }

    override fun subscribe() {

    }

    override fun unSubscribe() {
        mCompositeDisposable!!.clear()
    }

}