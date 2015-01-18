package com.mikepenz.unsplash.views.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.mikepenz.unsplash.OnItemClickListener;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.views.Utils;

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

        Ion.with(context)
                .load(images.get(position).getImage_src())
                .intoImageView(imagesViewHolder.imageView)
                .withBitmapInfo()
                .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                    @Override
                    public void onCompleted(Exception e, ImageViewBitmapInfo result) {

                        if (e == null && result != null && result.getBitmapInfo() != null && result.getBitmapInfo().bitmap != null) {

                            Palette.generateAsync(result.getBitmapInfo().bitmap, new Palette.PaletteAsyncListener() {
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
                });
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

