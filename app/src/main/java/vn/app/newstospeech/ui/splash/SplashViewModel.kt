package vn.app.newstospeech.ui.splash

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.base.common.base.viewmodel.BaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import vn.app.newstospeech.BuildConfig
import vn.app.newstospeech.data.model.CheckVersionResponse
import vn.app.newstospeech.data.remote.usecase.CheckVersionUseCase
import vn.app.newstospeech.data.request.CheckVersionRequest
import vn.app.newstospeech.data.usermanager.UserManager

class SplashViewModel : BaseViewModel<SplashEvent>() {

}

sealed class SplashEvent {
    object GoToLogin : SplashEvent()
    object GoToMain : SplashEvent()
}