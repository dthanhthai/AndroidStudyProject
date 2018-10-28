package com.thaidt.demomvvm.ui.viewholder

import android.app.Application
import android.app.SharedElementCallback
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thaidt.demomvvm.data.callback.OnDataReceiveResult
import com.thaidt.demomvvm.data.model.Project
import com.thaidt.demomvvm.data.repository.ApiClient
import com.thaidt.demomvvm.data.repository.DemoService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ProjectListViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var projectListObservable: LiveData<List<Project>>

    fun getProjectListObservable():LiveData<List<Project>>{
        return projectListObservable
    }

    fun loadProjectList(callback: OnDataReceiveResult<List<Project>>) {
        projectListObservable = MutableLiveData<List<Project>>()

        var disposable: Disposable = ApiClient.getInstance().getRetrofit()
            .create(DemoService::class.java).getProjectList("google")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    callback.onResponse(it)
                    (projectListObservable as MutableLiveData<List<Project>>).value = it
                },
                {
                    callback.onError(it)
                    (projectListObservable as MutableLiveData<List<Project>>).value = null
                }
            )
    }
}