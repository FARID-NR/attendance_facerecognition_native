package com.example.absensiapp

import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.multidex.MultiDexApplication

import com.example.absensiapp.network.HttpClient

class AbsensiApp : MultiDexApplication() {

    companion object {
        lateinit var instance : AbsensiApp

        fun getApp() : AbsensiApp {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun getPreferences() : SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun setToken(token:String) {
        getPreferences().edit().putString("PREFERENCES_TOKEN", token).apply()
        HttpClient.getInstance().buildRetrofitClient(token)
    }

    fun getToken(): String? {
        val token = getPreferences().getString("PREFERENCES_TOKEN", null)
        Log.d("AbsensiApp", "Token diambil: $token")
        return token
    }

    fun setUser(user:String) {
        getPreferences().edit().putString("PREFERENCES_USER", user).apply()
        HttpClient.getInstance().buildRetrofitClient(user)
    }

    fun getUser(): String? {
        return getPreferences().getString("PREFERENCES_USER", null)
    }
}