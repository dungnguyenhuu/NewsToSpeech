package vn.app.newstospeech.data.remote

import io.reactivex.Single
import vn.app.newstospeech.data.model.CheckVersionResponse
import vn.app.newstospeech.data.request.CheckVersionRequest

interface AuthenticateRepository {

    fun checkVersion(versionRequest: CheckVersionRequest): Single<CheckVersionResponse>
}

class AuthenticateRepositoryImpl(private val authenticateSource: AuthenticateSource) :
    AuthenticateRepository {

    override fun checkVersion(versionRequest: CheckVersionRequest): Single<CheckVersionResponse> {
        return authenticateSource.checkVersion(versionRequest.version, versionRequest.type)
    }
}