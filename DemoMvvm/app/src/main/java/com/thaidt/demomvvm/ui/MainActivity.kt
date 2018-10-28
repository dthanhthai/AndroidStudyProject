package com.thaidt.demomvvm.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thaidt.demomvvm.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_fragment_container,
                ProjectListFragment.newInstance(),
                ProjectListFragment.TAG
            )
            .commit()
    }
}
