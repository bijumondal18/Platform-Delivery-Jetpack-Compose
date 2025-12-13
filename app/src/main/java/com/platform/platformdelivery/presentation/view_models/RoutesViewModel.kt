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
    private val perPage = 10


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

    fun loadAvailableRoutesOnce(
        date: String? = null, 
        radius: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        if (!hasLoadedAvailableRoutes) {
            getAvailableRoutes(1, date, radius, latitude, longitude)
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


    fun getAvailableRoutes(
        page: Int = 1, 
        date: String? = null, 
        radius: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
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

                val result = routeRepository.getAvailableRoutes(
                    page, 
                    perPage, 
                    formattedDate, 
                    radius,
                    latitude,
                    longitude
                )

                when (result) {
                    is Result.Success -> {
                        val newRoutes = result.data?.data?.nestedData?.routesData
                            ?: result.data?.data?.headdata?.bodydata?.routesData
                            ?: emptyList()
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

    fun loadNextPage(
        radius: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        if (!_noMoreDataAvailable.value && !_isLoading.value) {
            getAvailableRoutes(
                currentPage + 1, 
                radius = radius,
                latitude = latitude,
                longitude = longitude
            )
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
                        val newTrips = result.data?.data?.nestedData?.routesData
                            ?: result.data?.data?.headdata?.bodydata?.routesData
                            ?: emptyList()
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

    private val _isAcceptingRoute = MutableStateFlow(false)
    val isAcceptingRoute: StateFlow<Boolean> get() = _isAcceptingRoute

    private val _acceptRouteResult = MutableStateFlow<Result<Unit>?>(null)
    val acceptRouteResult: StateFlow<Result<Unit>?> get() = _acceptRouteResult

    fun acceptRoute(routeId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAcceptingRoute.value = true
            _acceptRouteResult.value = null
            
            try {
                val result = routeRepository.acceptRoute(routeId)
                when (result) {
                    is Result.Success -> {
                        _acceptRouteResult.value = Result.Success(Unit)
                        // Refresh route details after accepting
                        _routeDetails.value?.routeDetailsData?.routeData?.id?.let { id ->
                            getRouteDetails(RequestRouteDetails(routeId = id.toString()))
                        }
                        onSuccess()
                    }
                    is Result.Error -> {
                        _acceptRouteResult.value = Result.Error(result.message)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _acceptRouteResult.value = Result.Error(e.message ?: "Failed to accept route")
            } finally {
                _isAcceptingRoute.value = false
            }
        }
    }

    private val _isCancellingRoute = MutableStateFlow(false)
    val isCancellingRoute: StateFlow<Boolean> get() = _isCancellingRoute

    private val _cancelRouteResult = MutableStateFlow<Result<Unit>?>(null)
    val cancelRouteResult: StateFlow<Result<Unit>?> get() = _cancelRouteResult

    fun cancelRoute(routeId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isCancellingRoute.value = true
            _cancelRouteResult.value = null
            
            try {
                // Format current time as HH:mm:ss (e.g., "15:59:00")
                val currentTime = SimpleDateFormat(
                    "HH:mm:ss",
                    java.util.Locale.getDefault()
                ).format(Date())
                
                val result = routeRepository.cancelRoute(routeId, currentTime)
                when (result) {
                    is Result.Success -> {
                        _cancelRouteResult.value = Result.Success(Unit)
                        // Remove cancelled route from accepted trips list
                        _acceptedTrips.value = _acceptedTrips.value.filter { it.id?.toString() != routeId }
                        // Refresh accepted trips list
                        val formattedDate = SimpleDateFormat(
                            "yyyy-MM-dd",
                            java.util.Locale.getDefault()
                        ).format(Date())
                        getAcceptedTrips(1, formattedDate)
                        onSuccess()
                    }
                    is Result.Error -> {
                        _cancelRouteResult.value = Result.Error(result.message)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _cancelRouteResult.value = Result.Error(e.message ?: "Failed to cancel route")
            } finally {
                _isCancellingRoute.value = false
            }
        }
    }

    private val _isStartingTrip = MutableStateFlow(false)
    val isStartingTrip: StateFlow<Boolean> get() = _isStartingTrip

    private val _tripStartResult = MutableStateFlow<Result<Unit>?>(null)
    val tripStartResult: StateFlow<Result<Unit>?> get() = _tripStartResult

    fun tripStartTime(routeId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isStartingTrip.value = true
            _tripStartResult.value = null
            
            try {
                // Format current time as HH:mm:ss (e.g., "15:59:00")
                val currentTime = SimpleDateFormat(
                    "HH:mm:ss",
                    java.util.Locale.getDefault()
                ).format(Date())
                
                val result = routeRepository.tripStartTime(routeId, currentTime)
                when (result) {
                    is Result.Success -> {
                        _tripStartResult.value = Result.Success(Unit)
                        // Refresh route details after starting trip
                        _routeDetails.value?.routeDetailsData?.routeData?.id?.let { id ->
                            getRouteDetails(RequestRouteDetails(routeId = id.toString()))
                        }
                        onSuccess()
                    }
                    is Result.Error -> {
                        _tripStartResult.value = Result.Error(result.message)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _tripStartResult.value = Result.Error(e.message ?: "Failed to start trip")
            } finally {
                _isStartingTrip.value = false
            }
        }
    }

}