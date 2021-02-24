package com.vendorsapp.network
import Extra.AppController
import Helpers.C
import NullOnEmptyConverterFactory
import com.blucor.aoneenterprises.BuildConfig
import com.blucor.aoneenterprises.LoginResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface MyApi {
    @Headers("Content-Type: application/json")
    @POST("/AOne_Enterprises/API/Login.php")
    suspend fun login(@Header("Content-Type") content_type : String,
        @Body parameter: RequestBody
    ): Response<LoginResponse>


    companion object {
        operator fun invoke(
            networkConnectionInterceptors: NetworkConnectionInterceptors
        ): MyApi {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val headerInterceptor = CustomInterceptor()


            val okkHttpclient = OkHttpClient.Builder()

                .addInterceptor(networkConnectionInterceptors)
                .addInterceptor(headerInterceptor)
                .addInterceptor(interceptor)
                .build()

            return Retrofit.Builder()
                .client(okkHttpclient)

                .baseUrl(AppController.base_url)

                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(NullOnEmptyConverterFactory())
                .build()
                .create(MyApi::class.java)
        }
    }

    class CustomInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request().newBuilder()
                .header("Authorization",C.authToken)

                    .header("Accept", "text/html")
                .header("app_version", BuildConfig.VERSION_CODE.toString())
                .build()
            return chain.proceed(request)
        }
    }
}