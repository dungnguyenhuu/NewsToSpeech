package com.example.android.newstospeech.data.constant

object VnExpressConstant {
    const val TITLE_DETAIL = ".title-detail"
    const val DESCRIPTION = ".description"
    const val LOCAL_STAMP = ".location-stamp"
    const val FCK_DETAIL = ".fck_detail"
    const val NORMAL = ".Normal"
}

enum class TTSStatus {
    LOADING,
    ERROR,
    DONE,
    PLAY,
    PAUSE,
    RESUME,
    CLEAR,
}

const val FILENAME = "/wpta_tts.wav"
const val ACTION_SPEECH = "action_speech"
const val ACTION_SPEECH_SERVICE = "action_speech_service"
const val SENT_DATA_TO_FRAGMENT = "sent_data_to_fragment"
const val STATUS_MEDIA_PLAYER = "status_media_player"
const val LIST_STRING_NEWS = "list_string_news"