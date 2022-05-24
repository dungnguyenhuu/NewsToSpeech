package com.example.android.newstospeech.service

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.example.android.newstospeech.App

/**
 * Helper APIs for constructing MediaStyle notifications
 */
class MediaStyleHelper {
    /**
     * Build a notification using the information from the given media session. Makes heavy use
     * of {@link MediaMetadataCompat#getDescription()} to extract the appropriate information.
     * @param context Context used to construct the notification.
     * @param mediaSession Media session to get information.
     * @return A pre-built notification with information from the given media session.
     */
    companion object {
        fun from(context: Context, mediaSession: MediaSessionCompat): NotificationCompat.Builder {
            val controllerCompat = mediaSession.controller
            val mediaMetadata = controllerCompat.metadata
            val description = mediaMetadata.description

            val builder = NotificationCompat.Builder(context, App.CHANNEL_ID)
            builder.setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setSubText(description.description)
                .setLargeIcon(description.iconBitmap)
                .setContentIntent(controllerCompat.sessionActivity)
                .setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            return builder
        }
    }
}