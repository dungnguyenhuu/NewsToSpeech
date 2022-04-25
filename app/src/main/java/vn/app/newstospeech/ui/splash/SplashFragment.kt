package vn.app.newstospeech.ui.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.databinding.Observable
import androidx.navigation.fragment.findNavController
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.constant.AppConstant.UPDATE_APP_TAG
import com.base.common.utils.rx.bus.RxEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.newstospeech.R
import vn.app.newstospeech.databinding.FragmentSplashBinding
import vn.app.newstospeech.utils.sharepref.SharedPreUtils

class SplashFragment : BaseMVVMFragment<SplashEvent, FragmentSplashBinding, SplashViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_splash
    override val viewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreUtils.putBoolean("APP_START", true)
        isNeedLoading = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splash_to_login)
        }, 2000 )

    }
}
