package com.mikepenz.unsplash.network;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikepenz.unsplash.CustomApplication;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.models.ImageList;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import rx.Observable;

public class UnsplashApi {
    private static final String ENDPOINT = "http://wallsplash.lanora.io";
    private final UnsplashService mWebService;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create(); //2015-01-18 15:48:56

    public UnsplashApi() {
        Cache cache = null;
        OkHttpClient okHttpClient = null;

        try {
            File cacheDir = new File(CustomApplication.getContext().getCacheDir().getPath(), "pictures.json");
            cache = new Cache(cacheDir, 10 * 1024 * 1024);
            okHttpClient = new OkHttpClient();
            okHttpClient.setCache(cache);
        } catch (Exception e) {
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Cache-Control", "public, max-age=" + 60 * 60 * 4);
                    }
                })
                .build();
        mWebService = restAdapter.create(UnsplashService.class);
    }


    public interface UnsplashService {
        @GET("/pictures")
        Observable<ImageList> listImages();
    }

    public Observable<ImageList> fetchImages() {
        return mWebService.listImages();
    }


    //keep the filtered array so we can reuse it later :D
    private ArrayList<Image> featured = null;

    public ArrayList<Image> filterFeatured(ArrayList<Image> images) {
        if (featured == null) {
            ArrayList<Image> list = new ArrayList<Image>(images);
            for (Iterator<Image> it = list.iterator(); it.hasNext(); ) {
                if (it.next().getFeatured() != 1)
                    it.remove();
            }
            featured = list;
        }
        return featured;
    }

    public ArrayList<Image> filterCategory(ArrayList<Image> images, int filter) {
        ArrayList<Image> list = new ArrayList<Image>(images);
        for (Iterator<Image> it = list.iterator(); it.hasNext(); ) {
            if ((it.next().getCategory() & filter) != filter)
                it.remove();
        }
        return list;
    }
}
