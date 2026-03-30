package com.heallots.mobile.api

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("Content-Type", "application/json")
            .build()

        return chain.proceed(request)
    }
}
