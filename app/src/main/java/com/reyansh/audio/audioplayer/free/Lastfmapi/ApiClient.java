package com.reyansh.audio.audioplayer.free.Lastfmapi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    public static final String ITUNES_API_URL = "http://itunes.apple.com/search?";
    public static final String BASE_API_URL = "http://ws.audioscrobbler.com/2.0/";
    public static final String BASE_PARAMETERS_ALBUM = "?method=album.getinfo&api_key=c20a698e716e0c68a4463bd1c66c8109&format=json";
    public static final String BASE_PARAMETERS_ARTIST = "?method=artist.getinfo&api_key=c20a698e716e0c68a4463bd1c66c8109&format=json";
    private static final long CACHE_SIZE = 4 * 1024 * 1024;
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .client(new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .addNetworkInterceptor(new CachingControlInterceptor())
                            .build())
                    .baseUrl(BASE_API_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}