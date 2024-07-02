package com.example.absensiapp.network

import com.example.absensiapp.model.response.history.HistoryResponse
import com.example.absensiapp.model.response.home.GetUserResponse
import com.example.absensiapp.model.response.login.LoginResponse
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Endpoint {

    @FormUrlEncoded
    @POST("login")
    fun login(@Field("email") email:String, @Field("password") password:String) : Observable<LoginResponse>

    @GET("user")
    fun home() : Observable<GetUserResponse>

    @GET("api-attendances")
    fun history(@Query("date") date: String): Observable<HistoryResponse>

}