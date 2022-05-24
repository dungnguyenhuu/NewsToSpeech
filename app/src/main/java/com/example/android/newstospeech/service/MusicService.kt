package com.example.android.newstospeech.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.example.android.newstospeech.R

class MusicService : MediaBrowserServiceCompat(),
    AudioManager.OnAudioFocusChangeListener {

    companion object {
        const val COMMAND_EXAMPLE = "command_example"
    }

    var mMediaPlayer: MediaPlayer? = null
    var mMediaSessionCompat: MediaSessionCompat? = null

    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("AAA onReceive mNoisyReceiver")
            if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
            }
        }
    }

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            println("AAA on play service")
            if (!successfullyRetrievedAudioFocus()) {
                return
            }
            mMediaSessionCompat!!.isActive = true
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)

            showPlayingNotification()
            mMediaPlayer?.start()
            mMediaPlayer?.setOnCompletionListener { mp ->
                println("AAA setOnCompletionListener")
                showPausedNotification()
                mp.stop()
                mp.release()
            }
        }

        override fun onPause() {
            super.onPause()
            println("AAA onPause service")
            if (mMediaPlayer?.isPlaying == true) {
                mMediaPlayer!!.pause()
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                showPausedNotification()
            }
        }

        override fun onStop() {
            super.onStop()
            println("AAA onStop service")
            mMediaPlayer!!.stop()
            stopSelf()
        }

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
            super.onPlayFromMediaId(mediaId, extras)
            try {
                val uri = Uri.parse("file://$mediaId")
                mMediaPlayer!!.setDataSource(applicationContext, uri)
                initMediaSessionMetadata()
                mMediaPlayer!!.prepare()
            } catch (e: Exception) {
                mMediaPlayer!!.release()
                initMediaPlayer()
                e.printStackTrace()
            }
        }
    }

    fun initMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer!!.setVolume(1.0f, 1.0f)
    }

    fun initMediaSessionMetadata() {
        val metadataBuilder = MediaMetadataCompat.Builder()
        //Notification icon in card
        metadataBuilder.putBitmap(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(
                resources, R.mipmap.ic_launcher
            )
        )
        metadataBuilder.putBitmap(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(
                resources, R.mipmap.ic_launcher
            )
        )

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(
            MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(
                resources, R.mipmap.ic_launcher
            )
        )
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Display Title")
        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
            "Display Subtitle"
        )
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1)
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1)

        mMediaSessionCompat?.setMetadata(metadataBuilder.build())
    }

    fun showPausedNotification() {
        val builder = mMediaSessionCompat?.let { MediaStyleHelper.from(this, it) } ?: return

        builder.addAction(
            R.drawable.ic_play_arrow,
            "Play",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this,
                PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
        )
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
        )
        builder.setSmallIcon(R.mipmap.ic_launcher)
        startForeground(1, builder.build())
    }

    private fun showPlayingNotification() {
        val builder = mMediaSessionCompat?.let { MediaStyleHelper.from(this, it) } ?: return

        builder.addAction(
            R.drawable.ic_pause,
            "Pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this,
                PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
        )
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
        )
        builder.setSmallIcon(R.mipmap.ic_launcher)

        startForeground(1, builder.build())
    }

    fun setMediaPlaybackState(state: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0F)
        mMediaSessionCompat?.setPlaybackState(playbackStateBuilder.build())
    }

    fun successfullyRetrievedAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_GAIN
    }

    override fun onCreate() {
        super.onCreate()
        println("AAA onCreate service")
        initMediaPlayer()
        initMediaSession()
        initNoisyReceiver()
    }

    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        mMediaSessionCompat =
            MediaSessionCompat(applicationContext, "Tag", mediaButtonReceiver, null)
        mMediaSessionCompat!!.setCallback(mMediaSessionCallback)
        mMediaSessionCompat!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        mMediaSessionCompat!!.setMediaButtonReceiver(pendingIntent)

        sessionToken = mMediaSessionCompat!!.sessionToken
    }

    private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mNoisyReceiver, filter)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        println("AAA onGetRoot")
        return if (TextUtils.equals(clientPackageName, packageName)) {
            BrowserRoot(getString(R.string.app_name), null)
        } else null

    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        println("AAA onLoadChildren")
        result.sendResult(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("AAA onStartCommand $intent")
        val actionSpeech = intent?.getIntExtra("ACTION_SPEECH_SERVICE", -1)
        if (actionSpeech == 99) {
            stopSelf()
        } else {
            MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        println("AAA onAudioFocusChange $focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mMediaPlayer!!.isPlaying) {
                    mMediaPlayer!!.stop()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mMediaPlayer!!.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mMediaPlayer != null) {
                    mMediaPlayer!!.setVolume(0.3f, 0.3f)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mMediaPlayer != null) {
                    if (!mMediaPlayer!!.isPlaying) {
                        mMediaPlayer!!.start()
                    }
                    mMediaPlayer!!.setVolume(1.0f, 1.0f)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("AAA onDestroy service")
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
        unregisterReceiver(mNoisyReceiver)
        mMediaSessionCompat!!.release()
        NotificationManagerCompat.from(this).cancel(1)
    }
}