package com.thaidt.demomvvm.data.callback

interface OnDataReceiveResult<in T> {
    fun onResponse(data: T)
    fun onError(message: Throwable)
}