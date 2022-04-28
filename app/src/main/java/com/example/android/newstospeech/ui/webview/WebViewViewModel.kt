package com.example.android.newstospeech.ui.webview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.newstospeech.data.model.VnExpressNews

class WebViewViewModel : ViewModel() {
    val vnExpressNews = MutableLiveData<VnExpressNews>()

    val isShowPlay = MutableLiveData<Boolean>()

    init {
        isShowPlay.value = false
    }
}