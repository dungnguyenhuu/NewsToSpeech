package vn.app.newstospeech.di.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import vn.app.newstospeech.activity.MainNavFragmentViewModel
import vn.app.newstospeech.ui.dashboard.DashBoardViewModel
import vn.app.newstospeech.ui.login.LoginViewModel
import vn.app.newstospeech.ui.skipauth.SkipAuthViewModel
import vn.app.newstospeech.ui.splash.SplashViewModel

val viewModelModule = module {
    viewModel { MainNavFragmentViewModel() }
    viewModel { SplashViewModel() }
    viewModel { LoginViewModel() }
    viewModel { DashBoardViewModel() }
    viewModel { SkipAuthViewModel() }
}