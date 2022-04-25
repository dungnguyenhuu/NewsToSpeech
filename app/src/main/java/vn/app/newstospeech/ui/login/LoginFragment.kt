package vn.app.newstospeech.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.text.toSpannable
import androidx.databinding.Observable
import androidx.navigation.fragment.findNavController
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.data.result.ErrorApi
import com.base.common.utils.ext.setDebounceClickListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import vn.app.newstospeech.R
import vn.app.newstospeech.databinding.FragmentLoginBinding

class LoginFragment : BaseMVVMFragment<LoginEvent, FragmentLoginBinding, LoginViewModel>() {

    companion object {
        const val TAG = "LoginFragment"
    }

    override val layoutId: Int
        get() = R.layout.fragment_login
    override val viewModel: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewEvent()
    }

    private fun setupViewEvent() {
        viewDataBinding.apply {

        }
    }

}