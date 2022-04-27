/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.newstospeech.ui.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.newstospeech.network.MarsApi
import com.example.android.newstospeech.data.model.MarsPhoto
import com.example.android.newstospeech.data.model.RSSObject
import kotlinx.coroutines.launch

enum class NewsApiStatus { LOADING, ERROR, DONE }

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<NewsApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<NewsApiStatus> = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsPhoto
    // with new values
    private val _photos = MutableLiveData<List<MarsPhoto>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val photos: LiveData<List<MarsPhoto>> = _photos

    private val _rssObject = MutableLiveData<RSSObject>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val rssObject: LiveData<RSSObject> = _rssObject

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
//        getMarsPhotos()
//        getFeeds()
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [LiveData].
     */
    private fun getMarsPhotos() {
        viewModelScope.launch {
            _status.value = NewsApiStatus.LOADING
            try {
                _photos.value = MarsApi.retrofitService.getPhotos()
                println("AAA photo")
                _status.value = NewsApiStatus.DONE
            } catch (e: Exception) {
                _status.value = NewsApiStatus.ERROR
                _photos.value = listOf()
            }
        }
    }
    private fun getFeeds() {
        viewModelScope.launch {
            _status.value = NewsApiStatus.LOADING
            println("AAA getFeeds")
            try {
                val feeds = MarsApi.retrofitService.getAllFeeds("https://vnexpress.net/rss/tin-moi-nhat.rss")
                _rssObject.value = feeds
                println("AAA rss object $feeds")
                _status.value = NewsApiStatus.DONE
            } catch (e: Exception) {
                println("AAA rss object ${e.message}")
                _status.value = NewsApiStatus.ERROR
                _photos.value = listOf()
            }
        }
    }
}
