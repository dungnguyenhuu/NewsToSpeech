package com.example.android.newstospeech.ui.vnexpress.webview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.newstospeech.data.constant.TTSStatus
import com.example.android.newstospeech.data.model.VnExpressNews

class WebViewViewModel : ViewModel() {
    val vnExpressNews = MutableLiveData<VnExpressNews>()

    val isShowPlay = MutableLiveData<Boolean>()

    val isSpeak = MutableLiveData<Int>()

    val contentsList = ArrayList<String>()

    init {
        isShowPlay.value = false
        isSpeak.value = TTSStatus.LOADING.ordinal
    }
}