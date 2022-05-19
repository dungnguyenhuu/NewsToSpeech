package com.example.android.newstospeech.ui.vnexpress.pager

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.android.newstospeech.R
import com.example.android.newstospeech.databinding.FragmentPagerBinding
import com.example.android.newstospeech.databinding.FragmentVnExpressBinding
import com.example.android.newstospeech.databinding.FragmentWebViewBinding

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
    ): View? {
        binding = FragmentPagerBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            item = it.getInt("position")
            binding.tvTitle.text = "Item ${it.getInt("position")}"
        }
        println("AAA on onViewCreated $item")
        viewModel.getData()
    }

    override fun onStart() {
        super.onStart()
        println("AAA on onStart $item")
    }

    override fun onResume() {
        super.onResume()
        println("AAA on onResume $item")
    }

    override fun onPause() {
        super.onPause()
        println("AAA on onPause $item")
    }

    override fun onStop() {
        super.onStop()
        println("AAA on stop item $item")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("AAA on Destroy item $item")
    }

}