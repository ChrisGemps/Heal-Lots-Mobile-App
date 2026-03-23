package com.heallots.mobile.api;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class RequestInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Add logging if needed
        Request request = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build();

        return chain.proceed(request);
    }
}
