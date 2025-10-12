package com.platform.platformdelivery.core.network

import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RouteHistory
import com.platform.platformdelivery.data.models.RoutePathModel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


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
//    @FormUrlEncoded
//    @POST(AuthEndpoints.resendOtp)
//    suspend fun resendOtp(
//        @Field("email") email: String
//    ): Response<BaseResponse>
//
//    @FormUrlEncoded
//    @POST(AuthEndpoints.forgotPassword)
//    suspend fun forgotPassword(
//        @Field("email") email: String
//    ): Response<BaseResponse>
//
//    @FormUrlEncoded
//    @POST(AuthEndpoints.resetPassword)
//    suspend fun resetPassword(
//        @Field("token") token: String,
//        @Field("password") password: String
//    ): Response<BaseResponse>
//
//    @FormUrlEncoded
//    @POST(AuthEndpoints.verifyOtp)
//    suspend fun verifyOtp(
//        @Field("email") email: String,
//        @Field("otp") otp: String
//    ): Response<BaseResponse>
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
    ): Response<RoutePathModel>

    //
//    @FormUrlEncoded
//    @POST(PlatformUrl.acceptRouteUrl)
//    suspend fun acceptRoute(
//        @Field("route_id") routeId: String
//    ): Response<BaseResponse>
//
//    @GET("${PlatformUrl.routeDetailsUrl}/{id}")
//    suspend fun getRouteDetails(
//        @Path("id") routeId: String
//    ): Response<RouteDetailsResponse>
//
//    @GET(PlatformUrl.myRouteUrl)
//    suspend fun getMyRoutes(): Response<MyRoutesResponse>
//
    @GET(RouteEndpoints.routeHistory)
    suspend fun getRouteHistory(
        @Query("page") page: Int,
        @Query("perpage") perPage: Int,
        @Query("date") date: String,
    ): Response<RouteHistory>
//
//    @GET(PlatformUrl.allNotificationUrl)
//    suspend fun getAllNotifications(): Response<NotificationResponse>
//
//    @GET(PlatformUrl.profileUpdateUrl)
//    suspend fun getProfile(): Response<ProfileResponse>
//
//    @FormUrlEncoded
//    @POST(PlatformUrl.profileUpdateUrl)
//    suspend fun updateProfile(
//        @Field("name") name: String,
//        @Field("phone") phone: String
//    ): Response<BaseResponse>
//
//    @GET(PlatformUrl.earningUrl)
//    suspend fun getEarnings(): Response<EarningsResponse>
//
//    @FormUrlEncoded
//    @POST(PlatformUrl.cancelRouteUrl)
//    suspend fun cancelRoute(
//        @Field("route_id") routeId: String
//    ): Response<BaseResponse>
//
//    @GET(PlatformUrl.driverDetailsUrl)
//    suspend fun getDriverDetails(): Response<DriverDetailsResponse>
}
