package com.example.android.newstospeech.ui.vnexpress.pager

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.android.newstospeech.R
import com.example.android.newstospeech.data.constant.VnExpress
import com.example.android.newstospeech.data.model.ItemNews
import com.example.android.newstospeech.databinding.FragmentPagerBinding
import com.example.android.newstospeech.databinding.FragmentVnExpressBinding
import com.example.android.newstospeech.databinding.FragmentWebViewBinding
import com.example.android.newstospeech.ui.vnexpress.ItemNewsAdapter
import com.example.android.newstospeech.ui.vnexpress.ItemNewsListener
import com.example.android.newstospeech.ui.vnexpress.VnExpressFragmentDirections
import com.example.android.newstospeech.ui.vnexpress.viewpager.ViewPagerFragmentDirections

class PagerFragment : Fragment() {

    companion object {
        fun newInstance() = PagerFragment()
    }

    lateinit var binding: FragmentPagerBinding
    private val viewModel: PagerViewModel by viewModels()

    var item = -1
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
        arguments?.let {
            item = it.getInt("position")
        }
        if (item > -1) {
            viewModel.getFeeds(VnExpress.listCategories[item].url)
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
        println("AAA click item")
        NavHostFragment.findNavController(this).navigate(
            ViewPagerFragmentDirections.actionViewPagerFragmentToWebViewFragment(item)
        )
    }

}