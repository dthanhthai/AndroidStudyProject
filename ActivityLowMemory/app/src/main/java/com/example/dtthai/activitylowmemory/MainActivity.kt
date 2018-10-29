package com.example.dtthai.activitylowmemory

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    private var mActivityCount = -1
    private var mFragmentCount = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mActivityCount = intent.getIntExtra(KEY_ACTIVITY_COUNT, 1)

        Log.d(TAG, "onCreate(), count: $mActivityCount")

        val fm = supportFragmentManager
        fm.addOnBackStackChangedListener(this)
        var f = fm.findFragmentById(R.id.container)
        if (f == null) {
            mFragmentCount = 1
            f = MainFragment.newInstance(mFragmentCount)
            fm.beginTransaction().add(R.id.container, f).commit()
        } else {
            mFragmentCount = (f as MainFragment).getCount()
        }

        val tvActivity = findViewById<View>(R.id.tv_activity) as TextView
        val tvFragment = findViewById<View>(R.id.tv_fragment) as TextView
        tvActivity.text = mActivityCount.toString()
        tvFragment.text = mFragmentCount.toString()
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
        val KEY_ACTIVITY_COUNT = "activity_count"
    }

    override fun onBackStackChanged() {
        val fm = supportFragmentManager
        val f = fm.findFragmentById(R.id.container)
        mFragmentCount = (f as MainFragment).getCount()
        val tv = findViewById<View>(R.id.tv_fragment) as TextView
        tv.text = mFragmentCount.toString()
    }

    fun clickStartActivity(button: View) {
        val i = Intent(this, MainActivity::class.java)
        i.putExtra(KEY_ACTIVITY_COUNT, mActivityCount + 1)
        startActivity(i)
    }

    fun clickPushFragment(button: View) {
        val fm = supportFragmentManager
        fm.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance(mFragmentCount + 1))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy(), count: $mActivityCount");
    }

    override fun finish() {
        Log.d(TAG, "finish(), count: $mActivityCount");
        super.finish()
    }
}
