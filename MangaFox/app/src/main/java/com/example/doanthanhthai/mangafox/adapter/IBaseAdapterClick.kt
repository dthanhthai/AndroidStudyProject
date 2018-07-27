package com.example.doanthanhthai.mangafox.adapter

/**
 * Created by ThaiDT1 on 7/27/2018.
 */
interface IBaseAdapterClick<T> {
    fun onItemClick(t: T, position: Int)
}