package com.example.android.newstospeech.ui.vnexpress.viewpager

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.android.newstospeech.data.constant.NEWS_TYPE
import com.example.android.newstospeech.data.constant.News
import com.example.android.newstospeech.data.constant.POSITION_PAGER
import com.example.android.newstospeech.data.constant.TuoiTreConstant
import com.example.android.newstospeech.data.constant.VnExpress
import com.example.android.newstospeech.ui.vnexpress.pager.PagerFragment

class ViewPagerFragmentStateAdapter(
    fragmentActivity: FragmentActivity,
    private val type: Int
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
       return when (type) {
           News.VN_EXPRESS.ordinal -> VnExpress.listCategories.size
           News.TUOI_TRE.ordinal -> TuoiTreConstant.listCategories.size
           else -> 0
       }
    }

    override fun createFragment(position: Int): Fragment {
        return PagerFragment().apply {
            arguments = bundleOf(
                POSITION_PAGER to position,
               NEWS_TYPE to type
            )
        }
    }
}