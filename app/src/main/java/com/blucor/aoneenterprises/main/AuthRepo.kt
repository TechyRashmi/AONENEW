package com.vendorsapp.activities.auth

import com.blucor.aoneenterprises.LoginResponse
import com.blucor.aoneenterprises.main.RequestLogin
import com.google.gson.Gson

import com.vendorsapp.network.MyApi
import com.vendorsapp.network.SafeApiRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepo(
    private val api: MyApi
) : SafeApiRequest() {







    suspend fun login(request: RequestLogin): LoginResponse {
        var response = LoginResponse(
            false,
            ""

        )
        try {
            val jsonObject = Gson().toJson(request, RequestLogin::class.java)
            val body = jsonObject.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            response = apiRequest { api.login("application/json",body) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }

    /*suspend fun driverLogin(request: LoginRequest): DriverLoginResponse {
        var response = DriverLoginResponse(
            "Network Error",
            false,
            "",
            null
        )
        try {
            val jsonObject = Gson().toJson(request, LoginRequest::class.java)
            val body = jsonObject.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            response = apiRequest { api.driverLogin(body) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }*/

   /* suspend fun otpVerify(request: OTPRequest): OTPVerifyResponse {
        var response = OTPVerifyResponse(
            "Network Error",
            false,
            null
        )
        try {
            val jsonObject = Gson().toJson(request, OTPRequest::class.java)
            val body = jsonObject.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            response = apiRequest { api.verifyUser(body) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }*/



    /*suspend fun resendOTP(): OTPResendResponse {
        var response = OTPResendResponse(
            "Network Error",
            false
        )
        try {
            response = apiRequest { api.resendOTP() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }*/

   /* suspend fun forgetPassword(phone: String): ForgetPasswordResponse {
        var response = ForgetPasswordResponse(
            "Network Error",
            null,
            false
        )
        try {
            response = apiRequest { api.forgotPasswordRoute(phone) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }*/

   /* suspend fun changePassword(request: ChangePasswordRequest): ChangePasswordResponse {
        var response = ChangePasswordResponse(
            "Network Error",
            null,
            false
        )
        try {
            val jsonObject = Gson().toJson(request, ChangePasswordRequest::class.java)
            val body = jsonObject.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            response = apiRequest { api.updatePwdAfterOTPGen(body) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }*/


}