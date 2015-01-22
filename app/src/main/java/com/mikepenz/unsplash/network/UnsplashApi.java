package com.mikepenz.unsplash.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikepenz.unsplash.models.ImageList;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import rx.Observable;

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
        @GET("/pictures/featured/1?by=date&order=desc")
        Observable<ImageList> listFeaturedImages();

        @GET("/pictures?by=modified_date&order=desc")
        Observable<ImageList> listImages();
    }

    public Observable<ImageList> fetchImages() {
        return mWebService.listImages();
    }

    public Observable<ImageList> fetchFeaturedImages() {
        return mWebService.listFeaturedImages();
    }
}
