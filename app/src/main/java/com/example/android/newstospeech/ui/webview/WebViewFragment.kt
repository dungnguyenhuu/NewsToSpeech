package com.example.android.newstospeech.ui.webview

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.Toast
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
import com.example.android.newstospeech.data.constant.FILENAME
import com.example.android.newstospeech.data.constant.SENT_DATA_TO_FRAGMENT
import com.example.android.newstospeech.data.constant.TTSStatus
import com.example.android.newstospeech.data.constant.VnExpressConstant
import com.example.android.newstospeech.data.model.ItemNews
import com.example.android.newstospeech.data.model.VnExpressNews
import com.example.android.newstospeech.databinding.FragmentWebViewBinding
import com.example.android.newstospeech.service.SpeechService
import java.io.File
import java.io.IOException
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    lateinit var mMediaPlayer: MediaPlayer

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
           val bundle = intent?.extras ?: return
            viewModel.isSpeak.value = bundle.getInt(ACTION_SPEECH)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViews = args.itemNews
        mMediaPlayer = MediaPlayer()
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
        println("AAA on view created")
        setWebView()
        setupObserve()
        setupViewEvent()
        getHtmlFromWeb()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadCastReceiver, IntentFilter(SENT_DATA_TO_FRAGMENT))
    }

    private fun setupViewEvent() {
        tts = TextToSpeech(requireContext(), this)
        binding.fabPlay.setDebounceClickListener {
            if(viewModel.isSpeak.value != TTSStatus.LOADING.ordinal) {
                startSpeechService()
            }
        }
    }

    private fun startSpeechService() {
        val intent = Intent(requireContext(), SpeechService::class.java)
        intent.putExtra(ACTION_SPEECH_SERVICE, TTSStatus.PLAY.ordinal)
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
                val contents = mutableListOf<String>()
                document.select(VnExpressConstant.NORMAL).forEach { element ->
                    contents.add(element.text())
                }

                val vnExpressNews = VnExpressNews(
                    title = titleDetail,
                    desc = description,
                    contents = contents
                )
                recordText(vnExpressNews)
                viewModel.vnExpressNews.postValue(vnExpressNews)
            } catch (e: IOException) {
                Timber.d(e)
                viewModel.isShowPlay.postValue(false)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale("vi_VN"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                binding.fabPlay.isEnabled = true
            }

            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    println("AAA onStart")
                    viewModel.isSpeak.postValue(TTSStatus.LOADING.ordinal)
                }

                override fun onDone(utteranceId: String) {
                    println("AAA ondone")
//                    initializeMediaPlayer()
                    viewModel.isSpeak.postValue(TTSStatus.DONE.ordinal)
                }

                override fun onError(utteranceId: String) {
                    println("AAA onError")
                    viewModel.isSpeak.postValue(TTSStatus.ERROR.ordinal)
                }
            })

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    private fun initializeMediaPlayer() {
        val fileName = Environment.getExternalStorageDirectory().absolutePath + FILENAME
        val uri = Uri.parse("file://$fileName")
        mMediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        try {
            mMediaPlayer.setDataSource(requireContext(), uri)
            mMediaPlayer.prepare()
            mMediaPlayer.setOnCompletionListener(OnCompletionListener {
                Toast.makeText(
                    activity,
                    "I'm Finished",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.isSpeak.postValue(TTSStatus.DONE.ordinal)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playMediaPlayer(status: Int) {
        // Start Playing
        if (status == 0) {
            mMediaPlayer.start()
            viewModel.isSpeak.value = TTSStatus.PLAY.ordinal
        }

        // Pause Playing
        if (status == 1) {
            mMediaPlayer.pause()
            viewModel.isSpeak.value = TTSStatus.PAUSE.ordinal
        }
    }

    private fun recordText(vnExpressNews: VnExpressNews) {
        lifecycleScope.launch(Dispatchers.IO) {
            val fileName = Environment.getExternalStorageDirectory().absolutePath + FILENAME
            val soundFile = File(fileName)
            if (soundFile.exists()) soundFile.delete()
            val myBundleAlarm = Bundle()
            myBundleAlarm.putString(
                TextToSpeech.Engine.KEY_PARAM_STREAM,
                AudioManager.STREAM_ALARM.toString()
            )
            myBundleAlarm.putString(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1")
            myBundleAlarm.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "dung_nh99")

            if (tts!!.synthesizeToFile(
                    vnExpressNews.getAllContent(),
                    myBundleAlarm,
                    File(fileName),
                    "dung_nh99"
                ) == TextToSpeech.SUCCESS
            ) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Creating sound file", Toast.LENGTH_SHORT).show()
                }
                println("AAA Sound file created")
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Oops! Sound file not created", Toast.LENGTH_SHORT).show()
                }
                println("AAA Oops! Sound file not created")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (tts != null) {
            tts!!.stop()
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMediaPlayer != null) {
            mMediaPlayer.release()
        }
        if (tts != null) {
            tts!!.shutdown()
        }
//        stopSpeechService()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(broadCastReceiver)
    }

    private fun stopSpeechService() {
        val intent = Intent(requireContext(), SpeechService::class.java)
        requireActivity().stopService(intent)
    }
}