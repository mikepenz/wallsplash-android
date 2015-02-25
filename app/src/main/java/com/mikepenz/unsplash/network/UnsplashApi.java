package com.mikepenz.unsplash.network;


import com.bluelinelabs.logansquare.LoganSquare;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikepenz.unsplash.CustomApplication;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.models.ImageList;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.http.GET;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import rx.Observable;

public class UnsplashApi {
    public static final String ENDPOINT = "http://wallsplash.lanora.io";
    private final UnsplashService mWebService;

    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create(); //2015-01-18 15:48:56

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
                .setConverter(new Converter() {
                    @Override
                    public Object fromBody(TypedInput body, Type type) throws ConversionException {
                        try {
                            Object o = LoganSquare.parse(body.in(), ImageList.class);
                            return o;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override

                    public TypedOutput toBody(Object object) {
                        return null;
                    }
                })
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

    public interface RandomUnsplashService {
        @GET("/random")
        Image random();
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
                if (it.next().getFeatured() != 1) {
                    it.remove();
                }
            }
            featured = list;
        }
        return featured;
    }

    public static int countFeatured(ArrayList<Image> images) {
        int count = 0;
        for (Image image : images) {
            if (image.getFeatured() == 1) {
                count = count + 1;
            }
        }
        return count;
    }

    public ArrayList<Image> filterCategory(ArrayList<Image> images, int filter) {
        ArrayList<Image> list = new ArrayList<Image>(images);
        for (Iterator<Image> it = list.iterator(); it.hasNext(); ) {
            if ((it.next().getCategory() & filter) != filter) {
                it.remove();
            }
        }
        return list;
    }

    public static int countCategory(ArrayList<Image> images, int filter) {
        int count = 0;
        for (Image image : images) {
            if ((image.getCategory() & filter) == filter) {
                count = count + 1;
            }
        }
        return count;
    }
}
