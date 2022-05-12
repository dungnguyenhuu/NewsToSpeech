package com.example.android.newstospeech.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.android.newstospeech.data.constant.ACTION_SPEECH
import com.example.android.newstospeech.data.constant.ACTION_SPEECH_SERVICE
import com.example.android.newstospeech.service.SpeechService

class SpeechReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getIntExtra(ACTION_SPEECH, -1)
        val intentToService = Intent(context, SpeechService::class.java)
        intentToService.putExtra(ACTION_SPEECH_SERVICE, action)
        context?.startService(intentToService)
    }

}