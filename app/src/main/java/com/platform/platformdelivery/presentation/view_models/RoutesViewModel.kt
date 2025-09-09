package com.platform.platformdelivery.presentation.view_models

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RoutePathModel
import com.platform.platformdelivery.data.repositories.AuthRepository
import com.platform.platformdelivery.data.repositories.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class RoutesViewModel(
    private val routeRepository: RouteRepository = RouteRepository(),
) : ViewModel() {


    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> get() = _routes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> get() = _isEmpty

    private val _noMoreDataAvailable = MutableStateFlow(false)
    val noMoreDataAvailable: StateFlow<Boolean> get() = _noMoreDataAvailable

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    // Pagination state
    private var currentPage = 1
    private val perPage = 7

    fun getAvailableRoutes(page: Int = 1, date: String? = null) {
        viewModelScope.launch {
            if (page == 1) {
                _isLoading.value = true
                _routes.value = emptyList()
                _noMoreDataAvailable.value = false
                _isEmpty.value = false
                _error.value = null
            }

            try {
                val formattedDate = date ?: SimpleDateFormat(
                    "yyyy-MM-dd",
                    java.util.Locale.getDefault()
                ).format(Date())

                val result = routeRepository.getAvailableRoutes(page, perPage, formattedDate)

                when (result) {
                    is Result.Success -> {
                        val newRoutes = result.data.headdata?.bodydata?.routesData?: emptyList()
                        if (newRoutes.isEmpty()) {
                            if (page == 1) {
                                _isEmpty.value = true
                            } else {
                                _noMoreDataAvailable.value = true
                            }
                        } else {
                            if (page == 1) {
                                _routes.value = newRoutes
                            } else {
                                _routes.value = _routes.value + newRoutes
                            }
                            _isEmpty.value = false
                        }
                    }

                    is Result.Error -> {
                        _error.value = result.message
                    }

                    Result.Idle -> Unit
                    Result.Loading -> _isLoading.value = true
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
                currentPage = page
            }
        }
    }

    fun loadNextPage() {
        if (!_noMoreDataAvailable.value && !_isLoading.value) {
            getAvailableRoutes(currentPage + 1)
        }
    }
}