package com.example.android.newstospeech.ui.webview

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import com.example.android.newstospeech.R
import com.example.android.newstospeech.base.extention.setDebounceClickListener
import com.example.android.newstospeech.data.constant.ACTION_SPEECH
import com.example.android.newstospeech.data.constant.ACTION_SPEECH_SERVICE
import com.example.android.newstospeech.data.constant.LIST_STRING_NEWS
import com.example.android.newstospeech.data.constant.SENT_DATA_TO_FRAGMENT
import com.example.android.newstospeech.data.constant.TTSStatus
import com.example.android.newstospeech.data.constant.VnExpressConstant
import com.example.android.newstospeech.data.model.ItemNews
import com.example.android.newstospeech.data.model.VnExpressNews
import com.example.android.newstospeech.databinding.FragmentWebViewBinding
import com.example.android.newstospeech.service.VoiceService
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
           val bundle = intent?.extras ?: return
            viewModel.isSpeak.value = bundle.getInt(ACTION_SPEECH)

        }
    }

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
        setupObserve()
        setupViewEvent()
        getHtmlFromWeb()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadCastReceiver, IntentFilter(SENT_DATA_TO_FRAGMENT))
    }

    private fun setupViewEvent() {
        binding.fabPlay.setDebounceClickListener {
            if(viewModel.isSpeak.value != TTSStatus.LOADING.ordinal) {
                startVoiceService()
            }
        }
    }

    private fun startVoiceService() {
        val intent = Intent(requireContext(), VoiceService::class.java)
        val bundle = Bundle()
        bundle.putInt(ACTION_SPEECH_SERVICE, TTSStatus.PLAY.ordinal)
        bundle.putStringArrayList(LIST_STRING_NEWS, viewModel.contentsList)
        intent.putExtras(bundle)
        requireActivity().startService(intent)
    }

    private fun setupObserve() {
        viewModel.isSpeak.observe(viewLifecycleOwner, Observer {
            binding.apply {
                when (it) {
                    TTSStatus.LOADING.ordinal -> {
                        fabPlay.setImageResource(R.drawable.loading_img)
                        fabPlay.isEnabled = false
                        fabPlay.visibility = View.GONE
                        progressCircularLoading.visibility = View.VISIBLE
                    }
                    TTSStatus.PLAY.ordinal -> {
                        fabPlay.setImageResource(R.drawable.ic_pause)
                        fabPlay.isEnabled = true
                        fabPlay.visibility = View.VISIBLE
                        progressCircularLoading.visibility = View.GONE
                    }
                    TTSStatus.PAUSE.ordinal, TTSStatus.DONE.ordinal -> {
                        fabPlay.setImageResource(R.drawable.ic_play_arrow)
                        fabPlay.isEnabled = true
                        fabPlay.visibility = View.VISIBLE
                        progressCircularLoading.visibility = View.GONE
                    }
                    TTSStatus.ERROR.ordinal -> {
                        fabPlay.setImageResource(R.drawable.ic_play_arrow)
                        fabPlay.isEnabled = false
                        fabPlay.visibility = View.GONE
                        progressCircularLoading.visibility = View.VISIBLE
                    }
                }
            }
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
                viewModel.contentsList.add(titleDetail)
                viewModel.contentsList.add(description)
                val contents = mutableListOf<String>()
                document.select(VnExpressConstant.NORMAL).forEach { element ->
                    contents.add(element.text())
                    viewModel.contentsList.add(element.text())
                }

                val vnExpressNews = VnExpressNews(
                    title = titleDetail,
                    desc = description,
                    contents = contents
                )
                viewModel.vnExpressNews.postValue(vnExpressNews)
                viewModel.isSpeak.postValue(TTSStatus.DONE.ordinal)
            } catch (e: IOException) {
                Timber.d(e)
                viewModel.isShowPlay.postValue(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(broadCastReceiver)
    }

}