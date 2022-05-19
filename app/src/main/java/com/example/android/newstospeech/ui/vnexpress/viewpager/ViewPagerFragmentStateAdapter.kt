package com.example.android.newstospeech.ui.vnexpress.viewpager

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.android.newstospeech.ui.vnexpress.pager.PagerFragment

class ViewPagerFragmentStateAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
       return 10
    }

    override fun createFragment(position: Int): Fragment {
        return PagerFragment().apply {
            arguments = bundleOf(
                "position" to position
            )
        }
    }
}