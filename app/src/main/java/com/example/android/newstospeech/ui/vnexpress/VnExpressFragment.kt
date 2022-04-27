package com.example.android.newstospeech.ui.vnexpress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.android.newstospeech.databinding.FragmentVnExpressBinding

class VnExpressFragment : Fragment() {

    companion object {
        fun newInstance() = VnExpressFragment()
    }

    lateinit var binding: FragmentVnExpressBinding
    private val viewModel: VnExpressViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVnExpressBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()

    }

    private fun setRecyclerView() {
        val adapter = ItemNewsAdapter()
        binding.rvListNews.adapter = adapter
        viewModel.rssObject.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it.items)
        })
    }

}