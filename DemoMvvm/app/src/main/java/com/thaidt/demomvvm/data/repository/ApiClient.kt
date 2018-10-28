package com.thaidt.demomvvm.data.repository

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient private constructor() {

    private object HOLDER {
        val INSTANCE = ApiClient()
    }

    private var retrofit: Retrofit
//        public get() = this.retrofit

    fun getRetrofit():Retrofit{
        return retrofit
    }

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    companion object {
        const val BASE_URL = "https://api.github.com/"

        fun getInstance(): ApiClient {
            return HOLDER.INSTANCE
        }
    }
}