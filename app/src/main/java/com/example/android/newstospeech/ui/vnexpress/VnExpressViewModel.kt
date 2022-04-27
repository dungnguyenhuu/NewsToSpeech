package com.example.android.newstospeech.ui.vnexpress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.newstospeech.data.model.RSSObject
import com.example.android.newstospeech.network.MarsApi
import com.example.android.newstospeech.ui.overview.NewsApiStatus
import kotlinx.coroutines.launch

class VnExpressViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<NewsApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<NewsApiStatus> = _status

    private val _rssObject = MutableLiveData<RSSObject>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val rssObject: LiveData<RSSObject> = _rssObject

    init {
        getFeeds()
    }

    private fun getFeeds() {
        viewModelScope.launch {
            _status.value = NewsApiStatus.LOADING
            println("AAA getFeeds")
            try {
                _rssObject.value = MarsApi.retrofitService.getAllFeeds("https://vnexpress.net/rss/tin-moi-nhat.rss")
                _status.value = NewsApiStatus.DONE
            } catch (e: Exception) {
                println("AAA rss object ${e.message}")
                _status.value = NewsApiStatus.ERROR
            }
        }
    }
}