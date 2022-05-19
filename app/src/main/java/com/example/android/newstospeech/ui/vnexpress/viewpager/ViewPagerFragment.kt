package com.example.android.newstospeech.ui.vnexpress.viewpager

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.android.newstospeech.R
import com.example.android.newstospeech.data.constant.News
import com.example.android.newstospeech.data.constant.TuoiTreConstant
import com.example.android.newstospeech.data.constant.VnExpress
import com.example.android.newstospeech.data.model.NewsCategory
import com.example.android.newstospeech.databinding.FragmentPagerBinding
import com.example.android.newstospeech.databinding.FragmentViewPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment() {

    companion object {
        fun newInstance() = ViewPagerFragment()
    }

    lateinit var binding: FragmentViewPagerBinding
    private val viewModel: ViewPagerViewModel by viewModels()
    private val args: ViewPagerFragmentArgs by navArgs()
    var newsType = -1
    lateinit var listCategories: List<NewsCategory>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewPagerBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        newsType = args.newsType
        when (newsType) {
            News.VN_EXPRESS.ordinal -> listCategories = VnExpress.listCategories
            News.TUOI_TRE.ordinal -> listCategories = TuoiTreConstant.listCategories
            else -> listOf<NewsCategory>()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (listCategories.isNotEmpty()) {
            binding.viewPager2.adapter = ViewPagerFragmentStateAdapter(requireActivity(), newsType)
            TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
                tab.text = listCategories[position].name
            }.attach()
        }
    }

}