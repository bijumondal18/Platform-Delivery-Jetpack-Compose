package com.platform.platformdelivery.core.network

import com.platform.platformdelivery.data.models.BaseResponse
import com.platform.platformdelivery.data.models.DriverDetailsResponse
import com.platform.platformdelivery.data.models.EarningResponse
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.models.NotificationResponse
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RouteDetailsResponse
import com.platform.platformdelivery.data.models.RouteHistory
import com.platform.platformdelivery.data.models.ReferralDetailsResponse
import com.platform.platformdelivery.data.models.RoutePathModel
import com.platform.platformdelivery.data.models.StateListResponse
import com.platform.platformdelivery.data.models.ApiVersionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


// Retrofit API Service for all driver-related endpoints
interface ApiService {

    @FormUrlEncoded
    @POST(AuthEndpoints.login)
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    //
//    @FormUrlEncoded
//    @POST(AuthEndpoints.register)
//    suspend fun register(
//        @Field("name") name: String,
//        @Field("email") email: String,
//        @Field("password") password: String
//    ): Response<RegisterResponse>
//
    @FormUrlEncoded
    @POST(AuthEndpoints.resendOtp)
    suspend fun resendOtp(
        @Field("email") email: String
    ): Response<BaseResponse>

    @FormUrlEncoded
    @POST(AuthEndpoints.forgotPassword)
    suspend fun forgotPassword(
        @Field("email") email: String
    ): Response<BaseResponse>

    @FormUrlEncoded
    @POST(AuthEndpoints.resetPassword)
    suspend fun resetPassword(
        @Field("token") token: String,
        @Field("password") password: String
    ): Response<BaseResponse>

//
    @FormUrlEncoded
    @POST(AuthEndpoints.verifyOtp)
    suspend fun verifyOtp(
        @Field("user_id") userId: String,
        @Field("otp") otp: String
    ): Response<BaseResponse>

//
//    @POST(AuthEndpoints.logout)
//    suspend fun logout(): Response<BaseResponse>
//
//
//
    @GET(RouteEndpoints.availableRoutes)
    suspend fun getAvailableRoutes(
        @Query("page") page: Int,
        @Query("perpage") perPage: Int,
        @Query("date") date: String,
        @Query(value = "radius", encoded = true) radius: String? = null,
        @Query(value = "lat", encoded = true) latitude: String? = null,
        @Query(value = "lng", encoded = true) longitude: String? = null,
    ): Response<RoutePathModel>

    @FormUrlEncoded
    @POST(RouteEndpoints.acceptRoute)
    suspend fun acceptRoute(
        @Field("route_id") routeId: String
    ): Response<BaseResponse>

    @POST(RouteEndpoints.routeDetails)
    suspend fun getRouteDetails(
        @Body requestRouteDetails: RequestRouteDetails
    ): Response<RouteDetailsResponse>

    @GET(RouteEndpoints.routeHistory)
    suspend fun getRouteHistory(
        @Query("page") page: Int,
        @Query("perpage") perPage: Int,
        @Query("date") date: String,
    ): Response<RouteHistory>

    @GET(RouteEndpoints.myRoutes)
    suspend fun getAcceptedTrips(
        @Query("page") page: Int,
        @Query("perpage") perPage: Int,
        @Query("date") date: String,
    ): Response<RoutePathModel>

    @FormUrlEncoded
    @POST(RouteEndpoints.cancelRoute)
    suspend fun cancelRoute(
        @Field("route_id") routeId: String,
        @Field("current_time") currentTime: String
    ): Response<BaseResponse>

    @FormUrlEncoded
    @POST(RouteEndpoints.tripStartTime)
    suspend fun tripStartTime(
        @Field("route_id") routeId: String,
        @Field("current_time") currentTime: String
    ): Response<BaseResponse>

    @FormUrlEncoded
    @POST(VehicleEndpoints.vehicleLoaded)
    suspend fun vehicleLoaded(
        @Field("route_id") routeId: String,
        @Field("waypoint_ids") waypointIds: String,
        @Field("lat") lat: String,
        @Field("lng") lng: String,
        @Field("datetime") datetime: String
    ): Response<BaseResponse>

    @FormUrlEncoded
    @POST(RouteEndpoints.routeDeliveryWithOptions)
    suspend fun routeDeliveryWithOptions(
        @Field("route_id") routeId: String,
        @Field("waypoint_id") waypointId: String,
        @Field("delivery_status") deliveryStatus: String, // "delivered" or "failed"
        @Field("current_time") currentTime: String
    ): Response<BaseResponse>

    @GET(EarningsEndpoints.totalEarnings)
    suspend fun getEarningDetails(): Response<EarningResponse>

    @GET(NotificationEndpoints.allNotifications)
    suspend fun getAllNotifications(
        @Query("page") page: Int,
        @Query("perpage") perPage: Int
    ): Response<NotificationResponse>

    @GET(NotificationEndpoints.unreadNotifications)
    suspend fun getUnreadNotifications(
        @Query("page") page: Int,
        @Query("perpage") perPage: Int
    ): Response<NotificationResponse>

    @POST(NotificationEndpoints.markAllAsRead)
    suspend fun markAllAsRead(): Response<BaseResponse>

    @GET(ProfileEndpoints.driverDetails)
    suspend fun getDriverDetails(): Response<DriverDetailsResponse>

    @GET
    suspend fun getStateList(@Url url: String = "https://platform.hannastransport.com/platform/public/api/state-list"): Response<StateListResponse>

    @Multipart
    @POST(ProfileEndpoints.updateProfile)
    suspend fun updateProfile(
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("street") street: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("state") state: RequestBody?,
        @Part("zip") zip: RequestBody?,
        @Part("base_location") baseLocation: RequestBody?,
        @Part("base_location_lat") baseLocationLat: RequestBody?,
        @Part("base_location_lng") baseLocationLng: RequestBody?,
        @Part profilePic: MultipartBody.Part?
    ): Response<DriverDetailsResponse>

    @FormUrlEncoded
    @POST(RouteEndpoints.updateCurrentLocation)
    suspend fun updateCurrentLocation(
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String
    ): Response<BaseResponse>

    @GET
    suspend fun getApiVersion(@Url url: String = ApiConfig.apiVersion): Response<ApiVersionResponse>

    @GET(ReferralEndpoints.referralDetails)
    suspend fun getReferralDetails(): Response<ReferralDetailsResponse>
}
