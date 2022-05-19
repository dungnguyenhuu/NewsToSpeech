package com.example.android.newstospeech.ui.vnexpress.pager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.example.android.newstospeech.data.constant.NEWS_TYPE
import com.example.android.newstospeech.data.constant.News
import com.example.android.newstospeech.data.constant.POSITION_PAGER
import com.example.android.newstospeech.data.constant.TuoiTreConstant
import com.example.android.newstospeech.data.constant.VnExpress
import com.example.android.newstospeech.data.model.ItemNews
import com.example.android.newstospeech.data.model.NewsCategory
import com.example.android.newstospeech.databinding.FragmentPagerBinding
import com.example.android.newstospeech.ui.vnexpress.ItemNewsAdapter
import com.example.android.newstospeech.ui.vnexpress.ItemNewsListener
import com.example.android.newstospeech.ui.vnexpress.viewpager.ViewPagerFragmentDirections

class PagerFragment : Fragment() {

    companion object {
        fun newInstance() = PagerFragment()
    }

    lateinit var binding: FragmentPagerBinding
    private val viewModel: PagerViewModel by viewModels()

    var position = -1
    var newsType = -1
    lateinit var listCategories: List<NewsCategory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(POSITION_PAGER)
            newsType = it.getInt(NEWS_TYPE)
        }

        when (newsType) {
            News.VN_EXPRESS.ordinal -> listCategories = VnExpress.listCategories
            News.TUOI_TRE.ordinal -> listCategories = TuoiTreConstant.listCategories
            else -> listOf<NewsCategory>()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPagerBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pagerViewModel = viewModel
        if (listCategories.isNotEmpty()) {
            viewModel.getFeeds(listCategories[position].url)
        }
        setRecyclerView()
    }

    private fun setRecyclerView() {
        val itemNewsListener = ItemNewsListener { item -> navigateWebView(item) }
        val adapter = ItemNewsAdapter(itemNewsListener)
        binding.rvListNews.adapter = adapter
        viewModel.rssObject.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it.items)
        })
    }

    private fun navigateWebView(item: ItemNews) {
        NavHostFragment.findNavController(this).navigate(
            ViewPagerFragmentDirections.actionViewPagerFragmentToWebViewFragment(item, newsType)
        )
    }

}