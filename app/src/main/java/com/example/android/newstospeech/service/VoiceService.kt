package com.example.android.newstospeech.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.android.newstospeech.App
import com.example.android.newstospeech.MainActivity
import com.example.android.newstospeech.R
import com.example.android.newstospeech.data.constant.ACTION_SPEECH
import com.example.android.newstospeech.data.constant.ACTION_SPEECH_SERVICE
import com.example.android.newstospeech.data.constant.LIST_STRING_NEWS
import com.example.android.newstospeech.data.constant.SENT_DATA_TO_FRAGMENT
import com.example.android.newstospeech.data.constant.STATUS_MEDIA_PLAYER
import com.example.android.newstospeech.data.constant.TTSStatus
import com.example.android.newstospeech.receiver.SpeechReceiver
import java.util.*
import kotlin.collections.ArrayList

class VoiceService: Service(), TextToSpeech.OnInitListener {
    private lateinit var textToSpeech: TextToSpeech

    var sentenceCounter: Int = 0
    var myList: List<String> = ArrayList()
    var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        textToSpeech = TextToSpeech(this, this)
    }

    fun resume() {
        sendActionToFragment(TTSStatus.PLAY.ordinal)
        sentenceCounter -= 1
        speakText()
    }

    fun play() {
        sendActionToFragment(TTSStatus.PLAY.ordinal)
        speakText()
    }

    fun pause() {
        textToSpeech.stop()
        isPlaying = false
        sendActionToFragment(TTSStatus.PAUSE.ordinal)
    }

    fun stop() {
        sentenceCounter = 0
        textToSpeech.stop()
    }

    fun speakText() {

//        val myText = "This is some text to speak. This is more text to speak."
        val utteranceId = "dungnh"
//        myList = myText.split(".")

        textToSpeech.speak(myList[sentenceCounter], TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        sentenceCounter++
        isPlaying = true
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendNotificationMedia()
        val bundle = intent?.extras
        if (bundle != null) {
            bundle.getStringArrayList(LIST_STRING_NEWS)?.let {
                myList = it
            }
        }

        val actionSpeech = intent?.getIntExtra(ACTION_SPEECH_SERVICE, -1)
//        println("AAA actionSpeech $actionSpeech")
        if (actionSpeech != null) {
            handleActionSpeech(actionSpeech)
        }
        return START_NOT_STICKY
    }

    private fun handleActionSpeech(action: Int) {
        when (action) {
            TTSStatus.PLAY.ordinal -> {
                playOrPause()
            }
            TTSStatus.PAUSE.ordinal -> {
                pause()
            }
            TTSStatus.RESUME.ordinal -> {
                resume()
            }
            TTSStatus.CLEAR.ordinal -> {
                stopSelf()
                sendActionToFragment(TTSStatus.PAUSE.ordinal)
            }
        }
    }

    private fun playOrPause() {
        isPlaying = if (isPlaying) {
            pause()
            false
        } else {
            if (sentenceCounter > 0) {
                resume()
                true
            } else {
                false
            }
        }
    }

    private fun sendNotificationMedia() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationCompat = NotificationCompat.Builder(this, App.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSubText("NTS")
            .setContentTitle("News to speech")
            .setContentText("Playing text to speech")
            .setContentIntent(pendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1))
            .setVibrate(null)
            .setSound(null)
        if (isPlaying) {
            notificationCompat
                .addAction(R.drawable.ic_play_arrow, "Play", getPendingIntent(this, TTSStatus.RESUME.ordinal))
                .addAction(R.drawable.ic_clear, "Clear", getPendingIntent(this, TTSStatus.CLEAR.ordinal))
        } else {
            notificationCompat
                .addAction(R.drawable.ic_pause, "Pause", getPendingIntent(this, TTSStatus.PAUSE.ordinal))
                .addAction(R.drawable.ic_clear, "Clear", getPendingIntent(this, TTSStatus.CLEAR.ordinal))
        }
        startForeground(1, notificationCompat.build())
    }

    private fun getPendingIntent(context: Context, action: Int): PendingIntent {
        val intent = Intent(this, SpeechReceiver::class.java)
        intent.putExtra(ACTION_SPEECH, action)
        return PendingIntent.getBroadcast(context.applicationContext, action, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        textToSpeech.shutdown()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = textToSpeech.setLanguage(Locale("vi_VN"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }

            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    println("AAA onStart")
                }

                override fun onDone(utteranceId: String) {
                    println("AAA ondone")
                    if (sentenceCounter < myList.size) {
                        speakText()
                    } else {
                        sendNotificationMedia()
                        sendActionToFragment(TTSStatus.PAUSE.ordinal)
                    }
                }

                override fun onError(utteranceId: String) {
                    println("AAA onError")
                }
            })

            if (sentenceCounter == 0) {
                speakText()
                sendActionToFragment(TTSStatus.PLAY.ordinal)
                isPlaying = true
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    fun sendActionToFragment(action: Int) {
        val intent = Intent(SENT_DATA_TO_FRAGMENT)
        val bundle = Bundle()
        bundle.putBoolean(STATUS_MEDIA_PLAYER, isPlaying)
        bundle.putInt(ACTION_SPEECH, action)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }
}