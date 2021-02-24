package com.blucor.aoneenterprises

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface Api {

//    @FormUrlEncoded
//    @POST("Login.php")
//    fun userLogin(
//        @Field("mobileno") mobileno:String,
//        @Field("password") password: String
//    ): Call<LoginResponse>


    @GET("Login.php/{mobileno}/{password}")
    fun userLogin(@Path("mobileno") mobileno: String?, @Path("password") password: String?): Call<LoginResponse>?

//    @POST("Login.php")
//    fun userLogin(
//        @Body user: RequestLogin
//    ): Call<LoginResponse>

    class RetrofitInstance {
        companion object {
            val BASE_URL: String = "http://example.i-tech.consulting/AOne_Enterprises/API/"

            val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BODY
            }

            val client: OkHttpClient = OkHttpClient.Builder().apply {
                this.addInterceptor(interceptor)
            }.build()
            fun getRetrofitInstance(): Retrofit {
                return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }
    }


}