package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<LoginResponse>>(Result.Idle)
    val loginState: StateFlow<Result<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Result.Loading
            val result = authRepository.login(email, password)
            _loginState.value = result
            if (result is Result.Success) {
                result.data?.data?.token?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveAccessToken(it)
                }
                result.data?.data?.user?.id?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveUserId(it)
                }
                result.data?.data?.user?.name?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveName(it)
                }
                result.data?.data?.user?.email?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveEmail(it)
                }
                result.data?.data?.user?.profile_pic?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveProfilePic(it)
                }

                (RetrofitClient.tokenProvider as TokenManager).setIsLoggedIn(true)
            }
        }
    }


}