package com.example.dtthai.activitylowmemory


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView


class MainFragment : Fragment() {
    private var mCount: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mCount = it.getInt(KEY_COUNT, 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView(), count: $mCount");

        val lp = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val iv = ImageView(activity)
        iv.layoutParams = lp
        iv.setImageBitmap(BitmapFactory.decodeResource(resources, getResId()))

        return iv
    }

    private fun getResId(): Int {
        // R.drawable.large
        // Jyothis / CC-BY-SA-3.0,
        // Author: https://commons.wikimedia.org/wiki/User:Jyothis
        // License: http://creativecommons.org/licenses/by-sa/3.0/legalcode
        // Original link: https://commons.wikimedia.org/wiki/File:Chess_Large.JPG

        // R.drawable.large2
        // Editor at Large / CC-BY-SA-2.5
        // Author: https://commons.wikimedia.org/wiki/User:Editor_at_Large
        // License: https://creativecommons.org/licenses/by-sa/2.5/legalcode
        // Original link: https://commons.wikimedia.org/wiki/File:Chocolate_chip_cookie_closeup.jpg
        return if (mCount % 2 == 0)
            R.drawable.large
        else
            R.drawable.large2
    }

    fun getCount(): Int {
        return mCount
    }

    companion object {
        val TAG = MainFragment::class.java.simpleName
        val KEY_COUNT = "fragment_count"

        @JvmStatic
        fun newInstance(count: Int) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_COUNT, count)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView(), count: $mCount");
    }

}
