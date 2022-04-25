package vn.app.newstospeech.data.request

import com.google.gson.annotations.SerializedName

data class CheckVersionRequest(
    @SerializedName("version") var version: String,
    @SerializedName("type") var type: String
)