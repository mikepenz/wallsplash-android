package com.mikepenz.unsplash.fragments;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.mikepenz.unsplash.OnItemClickListener;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.activities.DetailActivity;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.models.ImageList;
import com.mikepenz.unsplash.network.Api;
import com.mikepenz.unsplash.views.adapters.ImageAdapter;

import java.util.ArrayList;

public class ImagesFragment extends Fragment {

    public static SparseArray<Bitmap> photoCache = new SparseArray<Bitmap>(1);

    private ProgressDialog loadingDialog;
    private ImageAdapter imageAdapter;
    private ArrayList<Image> images;
    private RecyclerView imageRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_images, container, false);
        imageRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_last_images_recycler);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        imageRecycler.setLayoutManager(gridLayoutManager);
        imageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        // Init and show progress dialog
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setMessage(getResources().getString(R.string.loading_images));
        loadingDialog.show();

        // Load images from API
        Ion.with(getActivity())
                .load(Api.getLastImages())
                .asString()
                .setCallback(imagesCallback);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private FutureCallback<String> imagesCallback = new FutureCallback<String>() {


        @Override
        public void onCompleted(Exception e, String result) {

            if (e == null) {
                // Serialize reader into objects
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create(); //2015-01-18 15:48:56
                ImageList imageList = gson.fromJson(result, ImageList.class);
                images = imageList.getImages();

                imageAdapter = new ImageAdapter(images);
                imageAdapter.setOnItemClickListener(recyclerRowClickListener);

                // Update adapter
                imageRecycler.setAdapter(imageAdapter);

                // Dismiss loading dialog
                loadingDialog.dismiss();


            } else {
                Log.d("[DEBUG]", "ImagesFragment onCompleted - ERROR: " + e.getMessage());
            }
        }
    };

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {

            Image selectedImage = images.get(position);

            Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
            detailIntent.putExtra("position", position);
            detailIntent.putExtra("selected_image", selectedImage);

            ImageView coverImage = (ImageView) v.findViewById(R.id.item_image_img);
            if (coverImage == null) {
                coverImage = (ImageView) ((View) v.getParent()).findViewById(R.id.item_image_img);
            }

            if (Build.VERSION.SDK_INT >= 21) {
                if (coverImage.getParent() != null) {
                    ((ViewGroup) coverImage.getParent()).setTransitionGroup(false);
                }
            }
            photoCache.put(position, coverImage.getDrawingCache());

            // Setup the transition to the detail activity
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), coverImage, "cover");

            startActivity(detailIntent, options.toBundle());
        }
    };
}
