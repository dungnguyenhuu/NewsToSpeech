package vn.app.newstospeech.di.module

import org.koin.dsl.module
import vn.app.newstospeech.data.remote.usecase.*

val useCaseModule = module {
    factory { CheckVersionUseCase(get(), get()) }

}