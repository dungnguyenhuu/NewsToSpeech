package com.example.android.newstospeech.data.model

class VnExpressNews{
    var titleDetail: String = ""
    var description: String = ""
    var contents: MutableList<String> = mutableListOf()

    constructor(title: String, desc: String, contents: MutableList<String>) {
        this.titleDetail = title
        this.description = desc
        this.contents = contents
    }

    fun getAllContent(): String {
        return "$titleDetail. $description. ${contents.joinToString(".")}"
    }
}
