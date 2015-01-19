package com.mikepenz.unsplash.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.unsplash.OnItemClickListener;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.views.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

/**
 * Created by saulmm on 08/12/14.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImagesViewHolder> {

    private final ArrayList<Image> images;
    private Context context;
    private int defaultBackgroundColor;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ImageAdapter(ArrayList<Image> images) {

        this.images = images;

    }

    @Override
    public ImagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        View rowView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_image, viewGroup, false);

        this.context = viewGroup.getContext();
        defaultBackgroundColor = context.getResources().getColor(R.color.image_without_palette);

        return new ImagesViewHolder(rowView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final ImagesViewHolder imagesViewHolder, final int position) {

        final Image currentImage = images.get(position);
        imagesViewHolder.imageAuthor.setText(currentImage.getAuthor());
        imagesViewHolder.imageDate.setText(currentImage.getReadableDate());
        imagesViewHolder.imageView.setDrawingCacheEnabled(true);

        Picasso.with(context).load(images.get(position).getImage_src()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imagesViewHolder.imageView.setImageBitmap(bitmap);

                Animation myFadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.alpha_on);
                imagesViewHolder.imageView.startAnimation(myFadeInAnimation);

                if (bitmap != null) {
                    Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {

                            Palette.Swatch s = palette.getVibrantSwatch();
                            if (s == null) {
                                s = palette.getMutedSwatch();
                            }

                            if (s != null) {
                                imagesViewHolder.imageAuthor.setTextColor(s.getTitleTextColor());
                                imagesViewHolder.imageDate.setTextColor(s.getTitleTextColor());
                            }

                            if (Build.VERSION.SDK_INT >= 21) {
                                imagesViewHolder.imageView.setTransitionName("cover" + position);
                            }
                            imagesViewHolder.imageTextContainer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onItemClickListener.onClick(v, position);
                                }
                            });

                            if (s != null) {
                                Utils.animateViewColor(imagesViewHolder.imageTextContainer, defaultBackgroundColor, s.getRgb());
                            }
                        }
                    });
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                imagesViewHolder.imageView.setImageDrawable(errorDrawable);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                imagesViewHolder.imageView.setImageDrawable(null);
            }
        });

        //calculate height of the list-item so we don't have jumps in the view
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        imagesViewHolder.imageView.setMinimumHeight((int) (displaymetrics.widthPixels / images.get(position).getRatio()));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}

class ImagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected final FrameLayout imageTextContainer;
    protected final ImageView imageView;
    protected final TextView imageAuthor;
    protected final TextView imageDate;
    private final OnItemClickListener onItemClickListener;

    public ImagesViewHolder(View itemView, OnItemClickListener onItemClickListener) {

        super(itemView);
        this.onItemClickListener = onItemClickListener;

        imageTextContainer = (FrameLayout) itemView.findViewById(R.id.item_image_text_container);
        imageView = (ImageView) itemView.findViewById(R.id.item_image_img);
        imageAuthor = (TextView) itemView.findViewById(R.id.item_image_author);
        imageDate = (TextView) itemView.findViewById(R.id.item_image_date);

        imageView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }
}

