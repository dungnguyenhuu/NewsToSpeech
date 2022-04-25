package vn.app.newstospeech.ui.login

import com.base.common.base.viewmodel.BaseViewModel

class LoginViewModel: BaseViewModel<LoginEvent>() {

    companion object {
        const val REQUEST_LOGIN_CODE = 1001
        const val TAG = "LoginViewModel"
    }

}

sealed class LoginEvent {
    object LoginSuccess : LoginEvent()
}
