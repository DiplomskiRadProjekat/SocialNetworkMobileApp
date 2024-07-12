package com.example.social_network.services;

import com.example.social_network.tools.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceUtils {

    private static final String SERVICE_API_PATH = "http://192.168.1.13:8081/api/v1/";

    protected static final String auth = "auth";

    protected static final String user = "users";

    protected static final String friendRequests = "friend-requests";

    public static OkHttpClient.Builder httpClientBuilder(String authToken) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(interceptor);

        AuthInterceptor authInterceptor = new AuthInterceptor(authToken);
        builder.addInterceptor(authInterceptor);

        return builder;
    }

    public static Retrofit retrofit(String authToken) {
        return new Retrofit.Builder()
                .baseUrl(SERVICE_API_PATH)
                .addConverterFactory(GsonConverterFactory.create(getCustomGson()))
                .client(httpClientBuilder(authToken).build())
                .build();
    }

    private static Gson getCustomGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        return gsonBuilder.create();
    }

    public static IAuthService authService(String authToken) {
        return retrofit(authToken).create(IAuthService.class);
    }

    public static IUserService userService(String authToken) {
        return retrofit(authToken).create(IUserService.class);
    }

    public static IFriendRequestService friendRequestService(String authToken) {
        return retrofit(authToken).create(IFriendRequestService.class);
    }

}

