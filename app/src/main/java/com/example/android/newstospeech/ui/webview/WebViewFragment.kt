package com.example.android.newstospeech.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.android.newstospeech.R
import com.example.android.newstospeech.base.extention.setDebounceClickListener
import com.example.android.newstospeech.data.constant.VnExpressConstant
import com.example.android.newstospeech.data.model.ItemNews
import com.example.android.newstospeech.data.model.VnExpressNews
import com.example.android.newstospeech.databinding.FragmentWebViewBinding
import java.io.IOException
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber


class WebViewFragment : Fragment(), TextToSpeech.OnInitListener {

    companion object {
        fun newInstance() = WebViewFragment()
    }

    lateinit var binding: FragmentWebViewBinding
    private val viewModel: WebViewViewModel by viewModels()

    private val args: WebViewFragmentArgs by navArgs()
    lateinit var itemViews: ItemNews
    private var tts: TextToSpeech? = null

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
        setupViewEvent()
    }

    private fun setupViewEvent() {
        tts = TextToSpeech(requireContext(), this)
        binding.fabPlay.setDebounceClickListener {
            if (viewModel.isSpeak.value == false) {
                tts!!.speak(
                    viewModel.vnExpressNews.value?.getAllContent(),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                viewModel.isSpeak.value = true
            } else {
                viewModel.isSpeak.value = false
                tts!!.stop()
            }

        }
    }

    private fun setupObserve() {
        viewModel.isShowPlay.observe(viewLifecycleOwner, Observer {
            binding.fabPlay.visibility = if (it) View.VISIBLE else View.GONE

        })

        viewModel.isSpeak.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.fabPlay.setImageResource(R.drawable.ic_pause)
            } else {
                binding.fabPlay.setImageResource(R.drawable.ic_play_arrow)
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
                val contents = mutableListOf<String>()
                document.select(VnExpressConstant.NORMAL).forEach { element ->
                    contents.add(element.text())
                }

                viewModel.vnExpressNews.postValue(
                    VnExpressNews(
                        title = titleDetail,
                        desc = description,
                        contents = contents
                    )
                )
                viewModel.isShowPlay.postValue(true)
            } catch (e: IOException) {
                Timber.d(e)
                viewModel.isShowPlay.postValue(false)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                binding.fabPlay.isEnabled = true
            }

            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    viewModel.isSpeak.value = false
                }

                override fun onError(utteranceId: String) {}
                override fun onStart(utteranceId: String) {}
            })

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    override fun onStop() {
        super.onStop()
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }

}