/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mikepenz.unsplash.muzei;

import android.app.WallpaperManager;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.network.UnsplashApi;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

public class WallSplashSource extends RemoteMuzeiArtSource {
    private static final String SOURCE_NAME = "wall:splash";

    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours


    private final int mWallpaperWidth;
    private final int mWallpaperHeight;

    public WallSplashSource() {
        super(SOURCE_NAME);

        mWallpaperWidth = WallpaperManager.getInstance(this).getDesiredMinimumWidth();
        mWallpaperHeight = WallpaperManager.getInstance(this).getDesiredMinimumHeight();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(UnsplashApi.ENDPOINT)
                .setConverter(new GsonConverter(UnsplashApi.gson))
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError retrofitError) {
                        if (retrofitError != null && retrofitError.getResponse() != null && retrofitError.getKind() != null) {
                            int statusCode = retrofitError.getResponse().getStatus();
                            if (retrofitError.getKind() == RetrofitError.Kind.NETWORK || (500 <= statusCode && statusCode < 600)) {
                                return new RetryException();
                            }
                        }
                        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                        return retrofitError;
                    }
                })
                .build();

        UnsplashApi.RandomUnsplashService service = restAdapter.create(UnsplashApi.RandomUnsplashService.class);
        try {
            Image image = service.random();

            if (image == null) {
                scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                return;
            }

            publishArtwork(new Artwork.Builder()
                    .imageUri(Uri.parse(image.getHighResImage(mWallpaperWidth, mWallpaperHeight)))
                    .title(image.getAuthor())
                    .byline(image.getReadableModified_Date())
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(image.getUrl())))
                    .build());
        } catch (Exception ex) {
            Log.e("wallsplash", "WallSplashSource: " + ex.toString());
        }

        //schedule the next update ;)
        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}
