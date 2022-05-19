package com.example.android.newstospeech.data.constant

import com.example.android.newstospeech.data.model.NewsCategory

object TuoiTreConstant {
    const val TITLE_DETAIL = ".article-title"
    const val DESCRIPTION = "h2.sapo"
    const val MAIN_DETAIL_BODY = "#main-detail-body > p"

    val listCategories = listOf(
        NewsCategory(
            "Trang chủ",
            "https://tuoitre.vn/rss/tin-moi-nhat.rss"
        ),
        NewsCategory(
            "Thế giới",
            "https://tuoitre.vn/rss/the-gioi.rss"
        ),
        NewsCategory(
            "Thời sự",
            "https://tuoitre.vn/rss/thoi-su.rss"
        ),
        NewsCategory(
            "Thể thao",
            "https://tuoitre.vn/rss/the-thao.rss"
        ),
        NewsCategory(
            "Pháp luật",
            "https://tuoitre.vn/rss/phap-luat.rss"
        ),
        NewsCategory(
            "Giáo dục",
            "https://tuoitre.vn/rss/giao-duc.rss"
        ),
        NewsCategory(
            "Khoa học",
            "https://tuoitre.vn/rss/khoa-hoc.rss"
        ),
        NewsCategory(
            "Công nghệ",
            "https://tuoitre.vn/rss/nhip-song-so.rss"
        ),
        NewsCategory(
            "Xe",
            "https://tuoitre.vn/rss/xe.rss"
        ),
        NewsCategory(
            "Thư giãn",
            "https://tuoitre.vn/rss/thu-gian.rss"
        ),
    )
}