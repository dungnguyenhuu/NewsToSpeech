package vn.app.newstospeech.data.remote.usecase

import com.base.common.usecase.UseCase
import com.base.common.utils.rx.SchedulerProvider
import io.reactivex.Single
import vn.app.newstospeech.data.model.CheckVersionResponse
import vn.app.newstospeech.data.remote.AuthenticateRepository
import vn.app.newstospeech.data.request.CheckVersionRequest

class CheckVersionUseCase(
    private val authenticateRepository: AuthenticateRepository,
    schedulerProvider: SchedulerProvider
) : UseCase<CheckVersionResponse>(schedulerProvider) {

    lateinit var checkVersionRequest: CheckVersionRequest

    override fun buildUseCaseObservable(): Single<CheckVersionResponse> {
        return authenticateRepository.checkVersion(checkVersionRequest)
    }
}