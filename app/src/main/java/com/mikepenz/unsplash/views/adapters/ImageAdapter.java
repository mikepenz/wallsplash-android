package com.mikepenz.unsplash.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.unsplash.OnItemClickListener;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.other.PaletteTransformation;
import com.mikepenz.unsplash.views.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImagesViewHolder> {

    private final ArrayList<Image> images;
    private Context context;
    private int defaultTextColor;
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
        defaultTextColor = context.getResources().getColor(R.color.text_without_palette);
        defaultBackgroundColor = context.getResources().getColor(R.color.image_without_palette);

        return new ImagesViewHolder(rowView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final ImagesViewHolder imagesViewHolder, final int position) {

        final Image currentImage = images.get(position);
        imagesViewHolder.imageAuthor.setText(currentImage.getAuthor());
        imagesViewHolder.imageDate.setText(currentImage.getReadableModified_Date());
        imagesViewHolder.imageView.setDrawingCacheEnabled(true);

        //reset colors so we prevent crazy flashes :D
        imagesViewHolder.imageAuthor.setTextColor(defaultTextColor);
        imagesViewHolder.imageDate.setTextColor(defaultTextColor);
        imagesViewHolder.imageTextContainer.setBackgroundColor(defaultBackgroundColor);

        Picasso.with(context).load(images.get(position).getImage_src()).transform(PaletteTransformation.instance()).into(imagesViewHolder.imageView, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) imagesViewHolder.imageView.getDrawable()).getBitmap(); // Ew!
                Palette palette = PaletteTransformation.getPalette(bitmap);

                Palette.Swatch s = palette.getVibrantSwatch();
                if (s == null) {
                    s = palette.getDarkVibrantSwatch();
                }
                if (s == null) {
                    s = palette.getLightVibrantSwatch();
                }
                if (s == null) {
                    s = palette.getMutedSwatch();
                }

                if (s != null) {
                    if (images.get(position) != null) {
                        images.get(position).setSwatch(s);
                    }

                    imagesViewHolder.imageAuthor.setTextColor(s.getTitleTextColor());
                    imagesViewHolder.imageDate.setTextColor(s.getTitleTextColor());
                    Utils.animateViewColor(imagesViewHolder.imageTextContainer, defaultBackgroundColor, s.getRgb());
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

