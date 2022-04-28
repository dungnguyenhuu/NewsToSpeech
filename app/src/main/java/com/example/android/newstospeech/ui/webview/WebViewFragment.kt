package com.example.android.newstospeech.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.android.newstospeech.data.constant.VnExpressConstant
import com.example.android.newstospeech.data.model.ItemNews
import com.example.android.newstospeech.data.model.VnExpressNews
import com.example.android.newstospeech.databinding.FragmentWebViewBinding
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class WebViewFragment : Fragment() {

    companion object {
        fun newInstance() = WebViewFragment()
    }

    lateinit var binding: FragmentWebViewBinding
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
        getHtmlFromWeb()
        setupObserve()
    }

    private fun setupObserve() {
        viewModel.isShowPlay.observe(viewLifecycleOwner, Observer {
            Toast.makeText(activity, "$it", Toast.LENGTH_LONG).show()
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient()
            loadUrl(itemViews.link)
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
        }
    }

    private fun getHtmlFromWeb() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val document: Document = Jsoup.connect(itemViews.link).get()
                val titleDetail = document.select(VnExpressConstant.TITLE_DETAIL).text()
                val description = document.select(VnExpressConstant.DESCRIPTION).text()
                val contents = mutableListOf<String>()
                document.select(VnExpressConstant.NORMAL).forEach { element ->
                   contents.add(element.text())
                }

                viewModel.vnExpressNews.postValue(VnExpressNews(
                    titleDetail = titleDetail,
                    description = description,
                    contents = contents
                ))
                viewModel.isShowPlay.postValue(true)
            } catch (e: IOException) {
                Timber.d(e)
                viewModel.isShowPlay.postValue(false)
            }
        }
    }

}