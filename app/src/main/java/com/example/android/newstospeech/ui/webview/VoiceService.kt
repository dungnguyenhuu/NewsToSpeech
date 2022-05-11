package com.example.android.newstospeech.ui.webview

import android.os.Build
import android.speech.tts.TextToSpeech

class VoiceService {
    private lateinit var textToSpeech: TextToSpeech

    var sentenceCounter: Int = 0
    var myList: List<String> = ArrayList()

    fun resume() {
        sentenceCounter -= 1
        speakText()
    }

    fun pause() {
        textToSpeech.stop()
    }

    fun stop() {
        sentenceCounter = 0
        textToSpeech.stop()
    }

    fun speakText() {

        var myText = "This is some text to speak. This is more text to speak."
        val utteranceId = "dungnh"
        myList = myText.split(".")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(myList[sentenceCounter], TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            sentenceCounter++
        } else {
            var map: HashMap<String, String> = LinkedHashMap<String, String>()
            map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
            textToSpeech.speak(myList[sentenceCounter], TextToSpeech.QUEUE_FLUSH, map)
            sentenceCounter++
        }
    }
}