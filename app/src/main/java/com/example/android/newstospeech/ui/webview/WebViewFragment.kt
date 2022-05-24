package com.example.android.newstospeech.ui.webview

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
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
import android.os.RemoteException
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import com.example.android.newstospeech.service.MusicService
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
        const val STATE_PAUSED = 0
        const val STATE_PLAYING = 1
    }

    lateinit var binding: FragmentWebViewBinding
    private val viewModel: WebViewViewModel by viewModels()

    private val args: WebViewFragmentArgs by navArgs()
    lateinit var itemViews: ItemNews
    private var tts: TextToSpeech? = null

    var mCurrentState = STATE_PAUSED
    var mMediaBrowserCompat: MediaBrowserCompat? = null
    var mMediaControllerCompat: MediaControllerCompat? = null

    private val mMediaBrowserCompatConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                super.onConnected()
                try {
                    mMediaControllerCompat =
                        MediaControllerCompat(requireContext(), mMediaBrowserCompat!!.sessionToken)
                    mMediaControllerCompat!!.registerCallback(mMediaControllerCompatCallback)
                    MediaControllerCompat.setMediaController(
                        requireActivity(),
                        mMediaControllerCompat
                    )
                } catch (e: RemoteException) {
                    println("AAA $e")
                }
            }
        }

    val mMediaControllerCompatCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            if (state == null) {
                return
            }

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    mCurrentState = STATE_PLAYING
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    mCurrentState = STATE_PAUSED
                }
            }
        }
    }

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
        println("AAA on view created")
        setWebView()
        setupObserve()
        getHtmlFromWeb()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadCastReceiver, IntentFilter(SENT_DATA_TO_FRAGMENT))
        setupMediaBrowser()

        setupViewEvent()
    }

    private fun setupMediaBrowser() {
        mMediaBrowserCompat = MediaBrowserCompat(
            requireContext(), ComponentName(requireContext(), MusicService::class.java),
            mMediaBrowserCompatConnectionCallback, requireActivity().intent.extras
        )

        mMediaBrowserCompat!!.connect()
    }

    private fun setupViewEvent() {
        tts = TextToSpeech(requireContext(), this)
        binding.fabPlay.setDebounceClickListener {
            if (viewModel.isSpeak.value != TTSStatus.LOADING.ordinal) {
//                startSpeechService()
                speakSpeech()
            }
        }
    }

    private fun speakSpeech() {
        val fileName = requireContext().cacheDir.absolutePath + FILENAME
        MediaControllerCompat.getMediaController(requireActivity()).transportControls
            .playFromMediaId(java.lang.String.valueOf(fileName), null)
        mCurrentState = if (mCurrentState == STATE_PAUSED) {
            if ( MediaControllerCompat.getMediaController(requireActivity()) != null) {
                mMediaControllerCompat?.transportControls?.play()
                viewModel.isSpeak.value = TTSStatus.PLAY.ordinal
                STATE_PLAYING
            } else {
                viewModel.isSpeak.value = TTSStatus.PAUSE.ordinal
                STATE_PAUSED
            }

        } else {
            if (MediaControllerCompat.getMediaController(requireActivity()).playbackState
                    .state == PlaybackStateCompat.STATE_PLAYING
            ) {
                MediaControllerCompat.getMediaController(requireActivity()).transportControls.pause()
            }
            viewModel.isSpeak.value = TTSStatus.PAUSE.ordinal
            STATE_PAUSED
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

    private fun recordText(vnExpressNews: VnExpressNews) {
        lifecycleScope.launch(Dispatchers.IO) {
            val fileName = requireContext().cacheDir.absolutePath + FILENAME
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
                    Toast.makeText(activity, "Oops! Sound file not created", Toast.LENGTH_SHORT)
                        .show()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts != null) {
            tts!!.shutdown()
        }
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(broadCastReceiver)

        if( MediaControllerCompat.getMediaController(requireActivity()).playbackState.state == PlaybackStateCompat.STATE_PLAYING ) {
            MediaControllerCompat.getMediaController(requireActivity()).transportControls.pause()
        }

//        mMediaBrowserCompat?.disconnect()
    }

}