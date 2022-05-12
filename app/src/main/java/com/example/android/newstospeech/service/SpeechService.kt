package com.example.android.newstospeech.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.android.newstospeech.App.Companion.CHANNEL_ID
import com.example.android.newstospeech.MainActivity
import com.example.android.newstospeech.R
import com.example.android.newstospeech.data.constant.ACTION_SPEECH
import com.example.android.newstospeech.data.constant.ACTION_SPEECH_SERVICE
import com.example.android.newstospeech.data.constant.FILENAME
import com.example.android.newstospeech.data.constant.TTSStatus
import com.example.android.newstospeech.receiver.SpeechReceiver
import timber.log.Timber

class SpeechService : Service() {

    lateinit var mMediaPlayer: MediaPlayer

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
        }

        // Pause Playing
        if (status == 1) {
            mMediaPlayer.pause()
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
        if (mMediaPlayer.isPlaying) {
            notificationCompat
                .addAction(R.drawable.ic_pause, "Pause", getPendingIntent(this, TTSStatus.RESUME.ordinal))
                .addAction(R.drawable.ic_clear, "Clear", getPendingIntent(this, TTSStatus.CLEAR.ordinal))
        } else {
            notificationCompat
                .addAction(R.drawable.ic_play_arrow, "Play", getPendingIntent(this, TTSStatus.PAUSE.ordinal))
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

    private fun playOrPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying) {
            playMediaPlayer(1)
        } else {
            playMediaPlayer(0)
        }
    }

    private fun resume() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying) {
            mMediaPlayer.start()
        }
    }

    private fun pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying) {
            mMediaPlayer.pause()
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