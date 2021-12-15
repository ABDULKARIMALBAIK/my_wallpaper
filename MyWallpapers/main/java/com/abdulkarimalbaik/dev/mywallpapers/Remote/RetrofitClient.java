package com.abdulkarimalbaik.dev.mywallpapers.Remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static Retrofit instance = null;

    public static Retrofit getInstance(String baseURL){

        if (instance == null){

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(120 , TimeUnit.SECONDS)
                    .connectTimeout(120 , TimeUnit.SECONDS)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }

        return instance;
    }
}
