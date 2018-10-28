package com.thaidt.demomvvm.data.repository

import retrofit2.http.GET
import com.thaidt.demomvvm.data.model.Project
import io.reactivex.Observable
import retrofit2.http.Path


interface DemoService {
    @GET("users/{user}/repos")
    fun getProjectList(@Path("user") user: String): Observable<List<Project>>
}