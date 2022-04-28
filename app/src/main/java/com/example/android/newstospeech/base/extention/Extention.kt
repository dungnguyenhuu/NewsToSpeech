package com.example.android.newstospeech.base.extention

import android.view.View

fun View.setDebounceClickListener(listener: View.OnClickListener) {
    setOnClickListener(object : View.OnClickListener {
        var currentClickTime = 0L
        override fun onClick(p0: View?) {
            if (currentClickTime == 0L || System.currentTimeMillis() - currentClickTime > 500) {
                currentClickTime = System.currentTimeMillis()
                listener.onClick(p0)
            }
        }

    })
}