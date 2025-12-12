package com.platform.platformdelivery.presentation.view_models

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RouteDetailsResponse
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

     var hasLoadedAvailableRoutes = false
     var hasLoadedRouteHistory = false
     var hasLoadedAcceptedTrips = false

    // Pagination state
    private var currentPage = 1
    private var currentAcceptedTripsPage = 1
    private val perPage = 7


    private val _routeDetails = MutableStateFlow<RouteDetailsResponse?>(null)
    val routeDetails: StateFlow<RouteDetailsResponse?> get() = _routeDetails

    private val _isRouteDetailsLoading = MutableStateFlow(false)
    val isRouteDetailsLoading: StateFlow<Boolean> get() = _isRouteDetailsLoading

    private val _routeDetailsError = MutableStateFlow<String?>(null)
    val routeDetailsError: StateFlow<String?> get() = _routeDetailsError

    // Accepted trips state flows
    private val _acceptedTrips = MutableStateFlow<List<Route>>(emptyList())
    val acceptedTrips: StateFlow<List<Route>> get() = _acceptedTrips

    private val _isAcceptedTripsLoading = MutableStateFlow(false)
    val isAcceptedTripsLoading: StateFlow<Boolean> get() = _isAcceptedTripsLoading

    private val _acceptedTripsEmpty = MutableStateFlow(false)
    val acceptedTripsEmpty: StateFlow<Boolean> get() = _acceptedTripsEmpty

    private val _noMoreAcceptedTripsAvailable = MutableStateFlow(false)
    val noMoreAcceptedTripsAvailable: StateFlow<Boolean> get() = _noMoreAcceptedTripsAvailable

    private val _acceptedTripsError = MutableStateFlow<String?>(null)
    val acceptedTripsError: StateFlow<String?> get() = _acceptedTripsError

    fun loadAvailableRoutesOnce(date: String? = null, zipCode: String? = null) {
        if (!hasLoadedAvailableRoutes) {
            getAvailableRoutes(1, date, zipCode)
            hasLoadedAvailableRoutes = true
        }
    }

    fun loadRouteHistory(date: String? = null) {
        if (!hasLoadedRouteHistory) {
            getRouteHistory(1, date)
            hasLoadedRouteHistory = true
        }
    }

    fun resetAvailableRoutesFlag() {
        hasLoadedAvailableRoutes = false
    }

    fun resetRouteHistoryFlag() {
        hasLoadedRouteHistory = false
    }


    fun getAvailableRoutes(page: Int = 1, date: String? = null, zipCode: String? = null) {
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

                val result = routeRepository.getAvailableRoutes(page, perPage, formattedDate, zipCode?.takeIf { it.isNotBlank() })

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

    fun loadNextPage(zipCode: String? = null) {
        if (!_noMoreDataAvailable.value && !_isLoading.value) {
            getAvailableRoutes(currentPage + 1, zipCode = zipCode)
        }
    }



    fun getRouteHistory(page: Int = 1, date: String? = null) {
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

                val result = routeRepository.getRouteHistory(page, perPage, formattedDate)

                when (result) {
                    is Result.Success -> {
                        val newRoutes =  result.data.headdata?.bodydata?.routesData?: emptyList()
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

    fun getRouteDetails(requestRouteDetails: RequestRouteDetails) {
        viewModelScope.launch {
            _isRouteDetailsLoading.value = true
            _routeDetailsError.value = null
            _routeDetails.value = null

            try {
                val result = routeRepository.getRouteDetails(requestRouteDetails)

                when (result) {
                    is Result.Success -> {
                        _routeDetails.value = result.data
                    }

                    is Result.Error -> {
                        _routeDetailsError.value = result.message
                    }

                    Result.Idle -> Unit
                    Result.Loading -> _isRouteDetailsLoading.value = true
                }
            } catch (e: Exception) {
                _routeDetailsError.value = e.message
            } finally {
                _isRouteDetailsLoading.value = false
            }
        }
    }

    fun loadAcceptedTripsOnce(date: String? = null) {
        if (!hasLoadedAcceptedTrips) {
            getAcceptedTrips(1, date)
            hasLoadedAcceptedTrips = true
        }
    }

    fun getAcceptedTrips(page: Int = 1, date: String? = null) {
        viewModelScope.launch {
            if (page == 1) {
                _isAcceptedTripsLoading.value = true
                _acceptedTrips.value = emptyList()
                _noMoreAcceptedTripsAvailable.value = false
                _acceptedTripsEmpty.value = false
                _acceptedTripsError.value = null
            }

            try {
                val formattedDate = date ?: SimpleDateFormat(
                    "yyyy-MM-dd",
                    java.util.Locale.getDefault()
                ).format(Date())

                val result = routeRepository.getAcceptedTrips(page, perPage, formattedDate)

                when (result) {
                    is Result.Success -> {
                        val newTrips = result.data.headdata?.bodydata?.routesData ?: emptyList()
                        if (newTrips.isEmpty()) {
                            if (page == 1) {
                                _acceptedTripsEmpty.value = true
                            } else {
                                _noMoreAcceptedTripsAvailable.value = true
                            }
                        } else {
                            if (page == 1) {
                                _acceptedTrips.value = newTrips
                            } else {
                                _acceptedTrips.value = _acceptedTrips.value + newTrips
                            }
                            _acceptedTripsEmpty.value = false
                        }
                    }

                    is Result.Error -> {
                        _acceptedTripsError.value = result.message
                    }

                    Result.Idle -> Unit
                    Result.Loading -> _isAcceptedTripsLoading.value = true
                }
            } catch (e: Exception) {
                _acceptedTripsError.value = e.message
            } finally {
                _isAcceptedTripsLoading.value = false
                currentAcceptedTripsPage = page
            }
        }
    }

    fun loadNextAcceptedTripsPage() {
        if (!_noMoreAcceptedTripsAvailable.value && !_isAcceptedTripsLoading.value) {
            getAcceptedTrips(currentAcceptedTripsPage + 1)
        }
    }

}