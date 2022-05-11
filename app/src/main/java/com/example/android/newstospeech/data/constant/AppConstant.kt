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
    PAUSE
}