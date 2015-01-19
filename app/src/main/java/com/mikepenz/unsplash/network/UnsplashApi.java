package com.mikepenz.unsplash.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikepenz.unsplash.models.ImageList;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import rx.Observable;

/**
 * Created by mikepenz on 19.01.15.
 */
public class UnsplashApi {
    private static final String ENDPOINT = "http://mikepenz.com/android/unsplash";
    private final UnsplashService mWebService;

    public UnsplashApi() {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create(); //2015-01-18 15:48:56
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .build();
        mWebService = restAdapter.create(UnsplashService.class);
    }


    public interface UnsplashService {
        @GET("/images")
        Observable<ImageList> listImages();
    }

    public Observable<ImageList> fetchImages() {
        return mWebService.listImages();
    }
}
