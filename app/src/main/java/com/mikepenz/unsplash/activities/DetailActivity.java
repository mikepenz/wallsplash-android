package com.mikepenz.unsplash.activities;

import android.animation.Animator;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.future.ResponseFuture;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.fragments.ImagesFragment;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.other.CustomAnimatorListener;
import com.mikepenz.unsplash.other.CustomTransitionListener;
import com.mikepenz.unsplash.views.Utils;

import java.io.InputStream;


public class DetailActivity extends ActionBarActivity {

    private ImageView mFabButton;
    private DonutProgress mFabProgress;
    private View mTitleContainer;
    private View mTitlesContainer;
    private Image mSelectedImage;

    private Drawable mDrawablePhoto;
    private Drawable mDrawableClose;
    private Drawable mDrawableSuccess;

    private ResponseFuture<InputStream> future = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        findViewById(R.id.scroll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coolBack();
            }
        });

        // Recover items from the intent
        final int position = getIntent().getIntExtra("position", 0);
        mSelectedImage = (Image) getIntent().getSerializableExtra("selected_image");

        mDrawablePhoto = new IconicsDrawable(this, FontAwesome.Icon.faw_photo).color(Color.WHITE).sizeDp(24);
        mDrawableClose = new IconicsDrawable(this, FontAwesome.Icon.faw_close).color(Color.WHITE).sizeDp(24);
        mDrawableSuccess = new IconicsDrawable(this, FontAwesome.Icon.faw_check).color(Color.WHITE).sizeDp(24);

        mTitlesContainer = findViewById(R.id.activity_detail_titles);

        // Fab progress
        mFabProgress = (DonutProgress) findViewById(R.id.activity_detail_progress);
        mFabProgress.setMax(100);
        mFabProgress.setScaleX(0);
        mFabProgress.setScaleY(0);

        // Fab button
        mFabButton = (ImageView) findViewById(R.id.activity_detail_fab);
        mFabButton.setScaleX(0);
        mFabButton.setScaleY(0);
        mFabButton.setImageDrawable(mDrawablePhoto);
        mFabButton.setOnClickListener(onFabButtonListener);
        mFabButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e("un:splash", "TODO");
                return false;
            }
        });

        // Image summary card
        FrameLayout contentCard = (FrameLayout) findViewById(R.id.card_view);
        Utils.configuredHideYView(contentCard);

        // Title container
        mTitleContainer = findViewById(R.id.activity_detail_title_container);
        Utils.configuredHideYView(mTitleContainer);

        // Define toolbar as the shared element
        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_detail_toolbar);
        Bitmap imageCoverBitmap = ImagesFragment.photoCache.get(position);
        toolbar.setBackground(new BitmapDrawable(getResources(), imageCoverBitmap));

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setTransitionName("cover");

            // Add a listener to get noticed when the transition ends to animate the fab button
            getWindow().getSharedElementEnterTransition().addListener(sharedTransitionListener);
        } else {
            Utils.showViewByScale(toolbar).setDuration(1000).start();
            sharedTransitionListener.onTransitionEnd(null);

        }

        //check if we already had the colors during click
        int swatch_title_text_color = getIntent().getIntExtra("swatch_title_text_color", -1);
        int swatch_rgb = getIntent().getIntExtra("swatch_rgb", -1);

        if (swatch_rgb != -1 && swatch_title_text_color != -1) {
            setColors(swatch_title_text_color, swatch_rgb);
        } else {
            // Generate palette colors
            Palette.generateAsync(imageCoverBitmap, paletteListener);
        }
    }


    private View.OnClickListener onFabButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (future == null) {
                //reset progress to prevent jumping
                mFabProgress.setProgress(0);

                //get the desired wallpaper size so older phones won't die :D
                int wallpaperWidth = WallpaperManager.getInstance(DetailActivity.this).getDesiredMinimumWidth();
                int wallpaperHeight = WallpaperManager.getInstance(DetailActivity.this).getDesiredMinimumHeight();

                //prepare the call
                future = Ion.with(DetailActivity.this)
                        .load(mSelectedImage.getHighResImage(wallpaperWidth, wallpaperHeight))
                        .progressHandler(new ProgressCallback() {
                            @Override
                            public void onProgress(long downloaded, long total) {
                                mFabProgress.setProgress((int) (downloaded * 100.0 / total));
                            }
                        })
                        .asInputStream();

                //some nice button animations
                Utils.showViewByScale(mFabProgress).setDuration(500).start();
                mFabButton.setImageDrawable(mDrawableClose);
                mFabButton.animate().rotationBy(360).setDuration(400).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        downloadAndSetImage();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        downloadAndSetImage();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

            } else {
                future.cancel(true);
                future = null;

                //animating everything back to default :D
                Utils.hideViewByScaleXY(mFabProgress).setDuration(500).start();
                //Utils.animateViewElevation(mFabButton, 0, mElavationPx);
                mFabButton.setImageDrawable(mDrawablePhoto);
                mFabButton.animate().rotationBy(360).setDuration(400).start();
            }
        }
    };


    private void downloadAndSetImage() {
        if (future != null) {
            //set the callback and start downloading
            future.withResponse().setCallback(new FutureCallback<Response<InputStream>>() {
                @Override
                public void onCompleted(Exception e, Response<InputStream> result) {
                    boolean success = false;
                    if (e == null && result != null && result.getResult() != null) {
                        try {
                            WallpaperManager.getInstance(DetailActivity.this).setStream(result.getResult());

                            //some nice animations so the user knows the wallpaper was set properly
                            mFabButton.animate().rotationBy(720).setDuration(700).start();
                            mFabButton.setImageDrawable(mDrawableSuccess);

                            //animate the butotn to green. just do it the first time
                            if (mFabButton.getTag() == null) {
                                TransitionDrawable transition = (TransitionDrawable) mFabButton.getBackground();
                                transition.startTransition(500);
                                mFabButton.setTag("");
                            }

                            success = true;
                        } catch (Exception ex) {
                            Log.e("un:splash", ex.toString());
                        }
                    }

                    //hide the progress again :D
                    Utils.hideViewByScaleXY(mFabProgress).setDuration(500).start();

                    // if we were not successful remove the x again :D
                    if (!success) {
                        //Utils.animateViewElevation(mFabButton, 0, mElavationPx);
                        mFabButton.setImageDrawable(mDrawablePhoto);
                        mFabButton.animate().rotationBy(360).setDuration(400).start();
                    }
                    future = null;
                }
            });
        }
    }


    /**
     * I use a listener to get notified when the enter transition ends, and with that notifications
     * build my own choreography built with the elements of the UI
     * <p/>
     * Animations order
     * <p/>
     * 1. The image is animated automatically by the SharedElementTransition
     * 2. The layout that contains the titles
     * 3. An alpha transition to show the text of the titles
     * 3. A scale animation to show the image info
     */
    private CustomTransitionListener sharedTransitionListener = new CustomTransitionListener() {

        @Override
        public void onTransitionEnd(Transition transition) {

            super.onTransitionEnd(transition);

            ViewPropertyAnimator showTitleAnimator = Utils.showViewByScale(mTitleContainer);
            showTitleAnimator.setListener(new CustomAnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {

                    super.onAnimationEnd(animation);
                    mTitlesContainer.startAnimation(AnimationUtils.loadAnimation(DetailActivity.this, R.anim.alpha_on));
                    mTitlesContainer.setVisibility(View.VISIBLE);

                    Utils.showViewByScale(mFabButton).start();
                    //Utils.showViewByScale(imageInfoLayout).start();
                }
            });

            showTitleAnimator.start();
        }
    };

    @Override
    public void onBackPressed() {

        ViewPropertyAnimator hideTitleAnimator = Utils.hideViewByScaleXY(mFabButton);
        hideTitleAnimator.setDuration(500);
        Utils.hideViewByScaleXY(mFabProgress).setDuration(500).start();

        mTitlesContainer.startAnimation(AnimationUtils.loadAnimation(DetailActivity.this, R.anim.alpha_off));
        mTitlesContainer.setVisibility(View.INVISIBLE);

        //Utils.hideViewByScaleY(imageInfoLayout);

        hideTitleAnimator.setListener(new CustomAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {

                ViewPropertyAnimator hideFabAnimator = Utils.hideViewByScaleY(mTitleContainer);
                hideFabAnimator.setListener(new CustomAnimatorListener() {

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        super.onAnimationEnd(animation);
                        coolBack();
                    }
                });
            }
        });

        hideTitleAnimator.start();
    }

    private Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {

        @Override
        public void onGenerated(Palette palette) {

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
                setColors(s.getTitleTextColor(), s.getRgb());
            }
        }
    };

    private void setColors(int titleTextColor, int rgb) {
        mTitleContainer.setBackgroundColor(rgb);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(titleTextColor);
        }
        //getWindow().setNavigationBarColor(vibrantSwatch.getRgb());

        //TextView summaryTitle = (TextView) findViewById(R.id.activity_detail_summary_title);
        //summaryTitle.setTextColor(vibrantSwatch.getRgb());

        TextView titleTV = (TextView) mTitleContainer.findViewById(R.id.activity_detail_title);
        titleTV.setTextColor(titleTextColor);
        titleTV.setText(mSelectedImage.getAuthor());

        TextView subtitleTV = (TextView) mTitleContainer.findViewById(R.id.activity_detail_subtitle);
        subtitleTV.setTextColor(titleTextColor);
        subtitleTV.setText(mSelectedImage.getReadableModified_Date());

        ((TextView) mTitleContainer.findViewById(R.id.activity_detail_subtitle))
                .setTextColor(titleTextColor);
    }

    private void coolBack() {
        try {
            super.onBackPressed();
        } catch (Exception e) {
            // TODO: workaround
        }
    }
}
