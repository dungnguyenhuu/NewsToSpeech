package com.example.android.newstospeech.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.android.newstospeech.App.Companion.CHANNEL_ID
import com.example.android.newstospeech.MainActivity
import com.example.android.newstospeech.R
import com.example.android.newstospeech.data.constant.ACTION_SPEECH
import com.example.android.newstospeech.data.constant.ACTION_SPEECH_SERVICE
import com.example.android.newstospeech.data.constant.FILENAME
import com.example.android.newstospeech.data.constant.SENT_DATA_TO_FRAGMENT
import com.example.android.newstospeech.data.constant.STATUS_MEDIA_PLAYER
import com.example.android.newstospeech.data.constant.TTSStatus
import com.example.android.newstospeech.receiver.SpeechReceiver
import timber.log.Timber

class SpeechService : Service() {

    lateinit var mMediaPlayer: MediaPlayer
    var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        Timber.d("on create speech service")
        initMediaPlayer()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendNotificationMedia()
        val actionSpeech = intent?.getIntExtra(ACTION_SPEECH_SERVICE, -1)
        if (actionSpeech != null) {
            handleActionSpeech(actionSpeech)
        }
        return START_NOT_STICKY
    }

    private fun playMediaPlayer(status: Int) {
        // Start Playing
        if (status == 0) {
            mMediaPlayer.start()
            isPlaying = true
        }

        // Pause Playing
        if (status == 1) {
            mMediaPlayer.pause()
            isPlaying = false
        }
    }

    private fun initMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        val fileName = Environment.getExternalStorageDirectory().absolutePath + FILENAME
        val uri = Uri.parse("file://$fileName")
        mMediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        try {
            mMediaPlayer.setDataSource(applicationContext, uri)
            mMediaPlayer.prepare()
            mMediaPlayer.setOnCompletionListener {
                println("AAA Finished")
                isPlaying = false
                sendNotificationMedia()
                sendActionToFragment(TTSStatus.PAUSE.ordinal)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendNotificationMedia() {
        val mediaSessionCompat = MediaSessionCompat(this, "tag")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationCompat = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSubText("NTS")
            .setContentTitle("News to speech")
            .setContentText("Playing text to speech")
            .setContentIntent(pendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1)
                .setMediaSession(mediaSessionCompat.sessionToken))
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
            }
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

    private fun playOrPause() {
        isPlaying = if (mMediaPlayer != null && isPlaying) {
            mMediaPlayer.pause()
            sendActionToFragment(TTSStatus.PAUSE.ordinal)
            false
        } else {
            mMediaPlayer.start()
            sendActionToFragment(TTSStatus.PLAY.ordinal)
            true
        }
    }

    private fun resume() {
        if (mMediaPlayer != null && !isPlaying) {
            mMediaPlayer.start()
            sendActionToFragment(TTSStatus.PLAY.ordinal)
            isPlaying = true
        }
    }

    private fun pause() {
        if (mMediaPlayer != null && isPlaying) {
            mMediaPlayer.pause()
            sendActionToFragment(TTSStatus.PAUSE.ordinal)
            isPlaying = false
        }
    }

    private fun getPendingIntent(context: Context, action: Int): PendingIntent {
        val intent = Intent(this, SpeechReceiver::class.java)
        intent.putExtra(ACTION_SPEECH, action)
        return PendingIntent.getBroadcast(context.applicationContext, action, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("on destroy speech service")
        mMediaPlayer.release()
    }

}