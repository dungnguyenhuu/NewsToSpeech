package vn.app.newstospeech.activity

import android.content.Context
import com.base.common.base.fragment.BaseNavFragmentHost
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import vn.app.newstospeech.R
import vn.app.newstospeech.databinding.FragmentMainBinding
import vn.app.newstospeech.di.module.networkModule
import vn.app.newstospeech.di.module.repositoryModule
import vn.app.newstospeech.di.module.useCaseModule
import vn.app.newstospeech.di.module.viewModelModule

class MainNavFragmentHost :
    BaseNavFragmentHost<MainNavEvent, FragmentMainBinding, MainNavFragmentViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_main
    override val viewModel: MainNavFragmentViewModel by viewModel()
    override fun initViewModelModule(ctx: Context): Module = viewModelModule
}