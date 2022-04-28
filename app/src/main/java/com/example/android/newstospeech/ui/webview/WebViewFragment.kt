package com.example.android.newstospeech.ui.webview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.android.newstospeech.data.model.ItemNews
import com.example.android.newstospeech.databinding.FragmentWebViewBinding

class WebViewFragment : Fragment() {

    companion object {
        fun newInstance() = WebViewFragment()
    }

    lateinit var binding : FragmentWebViewBinding
    private val viewModel: WebViewViewModel by viewModels()

    private val args: WebViewFragmentArgs by navArgs()
    lateinit var itemViews: ItemNews

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViews = args.itemNews
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebViewBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWebView()
    }

    private fun setWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient()
            loadUrl(itemViews.link)
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
        }

    }

}