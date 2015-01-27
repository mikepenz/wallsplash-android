package com.mikepenz.unsplash.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
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

import java.io.File;
import java.io.InputStream;


public class DetailActivity extends ActionBarActivity {
    private static final int ACTIVITY_CROP = 13451;

    private static final int ANIMATION_DURATION_MEDIUM = 300;
    private static final int ANIMATION_DURATION_LONG = 450;
    private static final int ANIMATION_DURATION_EXTRA_LONG = 850;

    private ImageView mFabButton;
    private ImageView mFabShareButton;
    private DonutProgress mFabProgress;
    private View mTitleContainer;
    private View mTitlesContainer;
    private Image mSelectedImage;

    private Drawable mDrawablePhoto;
    private Drawable mDrawableClose;
    private Drawable mDrawableSuccess;

    private ResponseFuture<InputStream> future = null;

    private int mWallpaperWidth;
    private int mWallpaperHeight;

    private Animation mProgressFabAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //get the desired wallpaper size so older phones won't die :D
        mWallpaperWidth = WallpaperManager.getInstance(DetailActivity.this).getDesiredMinimumWidth();
        mWallpaperHeight = WallpaperManager.getInstance(DetailActivity.this).getDesiredMinimumHeight();

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
        //just allow the longClickAction on Devices newer than api level v19
        if (Build.VERSION.SDK_INT >= 19) {
            mFabButton.setOnLongClickListener(onFabButtonLongListener);
        }

        // Fab share button
        mFabShareButton = (ImageView) findViewById(R.id.activity_detail_fab_share);
        mFabShareButton.setScaleX(0);
        mFabShareButton.setScaleY(0);
        mFabShareButton.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_share).color(Color.WHITE).sizeDp(16));
        mFabShareButton.setOnClickListener(onFabShareButtonListener);

        // Title container
        mTitleContainer = findViewById(R.id.activity_detail_title_container);
        Utils.configuredHideYView(mTitleContainer);

        // Define toolbar as the shared element
        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_detail_toolbar);
        Bitmap imageCoverBitmap = ImagesFragment.photoCache.get(position);
        toolbar.setBackground(new BitmapDrawable(getResources(), imageCoverBitmap));
        setSupportActionBar(toolbar);

        //override text
        setTitle("");

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setTransitionName("cover");
            // Add a listener to get noticed when the transition ends to animate the fab button
            getWindow().getSharedElementEnterTransition().addListener(sharedTransitionListener);
        } else {
            Utils.showViewByScale(toolbar).setDuration(ANIMATION_DURATION_LONG).start();
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

    private View.OnClickListener onFabShareButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener onFabButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (future == null) {
                //prepare the call
                future = Ion.with(DetailActivity.this)
                        .load(mSelectedImage.getHighResImage(mWallpaperWidth, mWallpaperHeight))
                        .progressHandler(progressCallback)
                        .asInputStream();

                animateStart();

                mFabButton.animate().rotationBy(360).setDuration(ANIMATION_DURATION_MEDIUM).setListener(new CustomAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        streamAndSetImage();
                        super.onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        streamAndSetImage();
                        super.onAnimationCancel(animation);
                    }
                }).start();
            } else {
                animateReset();
            }
        }
    };

    private View.OnLongClickListener onFabButtonLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (future == null) {
                //prepare the call
                future = Ion.with(DetailActivity.this)
                        .load(mSelectedImage.getHighResImage(mWallpaperWidth, mWallpaperHeight))
                        .progressHandler(progressCallback)
                        .asInputStream();

                animateStart();

                mFabButton.animate().rotationBy(360).setDuration(ANIMATION_DURATION_MEDIUM).setListener(new CustomAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        downloadAndSetImage();
                        super.onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        downloadAndSetImage();
                        super.onAnimationCancel(animation);
                    }
                }).start();

            } else {
                animateReset();
            }

            return true;
        }
    };

    private ProgressCallback progressCallback = new ProgressCallback() {
        @Override
        public void onProgress(long downloaded, long total) {
            int progress = (int) (downloaded * 100.0 / total);
            if (progress < 1) {
                progress = progress + 1;
            }

            mFabProgress.setProgress(progress);
        }
    };

    /**
     * download an InputStream of the image and set as Wallpaper
     * Animate
     */
    private void streamAndSetImage() {
        if (future != null) {
            //set the callback and start downloading
            future.withResponse().setCallback(new FutureCallback<Response<InputStream>>() {
                @Override
                public void onCompleted(Exception e, Response<InputStream> result) {
                    boolean success = false;
                    if (e == null && result != null && result.getResult() != null) {
                        try {
                            WallpaperManager.getInstance(DetailActivity.this).setStream(result.getResult());

                            //animate the first elements
                            animateCompleteFirst();

                            success = true;
                        } catch (Exception ex) {
                            Log.e("un:splash", ex.toString());
                        }

                        //animate after complete
                        animateComplete(success);
                    }
                }
            });
        }
    }

    /**
     *
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void downloadAndSetImage() {
        if (future != null) {
            //set the callback and start downloading
            future.withResponse().setCallback(new FutureCallback<Response<InputStream>>() {
                @Override
                public void onCompleted(Exception e, Response<InputStream> result) {
                    boolean success = false;
                    if (e == null && result != null && result.getResult() != null) {
                        try {
                            //create a temporary directory within the cache folder
                            File dir = new File(DetailActivity.this.getCacheDir() + "/images");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }

                            //create the file
                            File file = new File(dir, "unsplash.jpg");
                            if (!file.exists()) {
                                file.createNewFile();
                            }

                            //copy the image onto this file
                            Utils.copyInputStreamToFile(result.getResult(), file);

                            //get the contentUri for this file and start the intent
                            Uri contentUri = FileProvider.getUriForFile(DetailActivity.this, "com.mikepenz.fileprovider", file);
                            Intent intent = WallpaperManager.getInstance(DetailActivity.this).getCropAndSetWallpaperIntent(contentUri);
                            //start activity for result so we can animate if we finish
                            DetailActivity.this.startActivityForResult(intent, ACTIVITY_CROP);

                            success = true;
                        } catch (Exception ex) {
                            Log.e("un:splash", ex.toString());
                        }
                    }

                    //animate after complete
                    animateComplete(success);
                }
            });
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_CROP) {
            //animate the first elements
            animateCompleteFirst();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * animate the start of the download
     */
    private void animateStart() {
        //reset progress to prevent jumping
        mFabProgress.setProgress(0);

        //some nice button animations
        Utils.showViewByScale(mFabProgress).setDuration(ANIMATION_DURATION_MEDIUM).start();
        mFabProgress.setProgress(1);

        mProgressFabAnimation = new RotateAnimation(0.0f, 360.0f, mFabProgress.getWidth() / 2, mFabProgress.getHeight() / 2);
        mProgressFabAnimation.setDuration(ANIMATION_DURATION_EXTRA_LONG * 2);
        mProgressFabAnimation.setInterpolator(new LinearInterpolator());
        mProgressFabAnimation.setRepeatCount(Animation.INFINITE);
        mProgressFabAnimation.setRepeatMode(-1);
        mFabProgress.startAnimation(mProgressFabAnimation);

        mFabButton.setImageDrawable(mDrawableClose);
    }

    /**
     * animate the reset of the view
     */
    private void animateReset() {
        future.cancel(true);
        future = null;

        //animating everything back to default :D
        Utils.hideViewByScaleXY(mFabProgress).setDuration(ANIMATION_DURATION_MEDIUM).start();
        mProgressFabAnimation.cancel();
        //Utils.animateViewElevation(mFabButton, 0, mElavationPx);
        mFabButton.setImageDrawable(mDrawablePhoto);
        mFabButton.animate().rotationBy(360).setDuration(ANIMATION_DURATION_MEDIUM).start();
    }

    /**
     * animate the first parts of the UI after the download has successfully finished
     */
    private void animateCompleteFirst() {
        //some nice animations so the user knows the wallpaper was set properly
        mFabButton.animate().rotationBy(720).setDuration(ANIMATION_DURATION_EXTRA_LONG).start();
        mFabButton.setImageDrawable(mDrawableSuccess);

        //animate the butotn to green. just do it the first time
        if (mFabButton.getTag() == null) {
            TransitionDrawable transition = (TransitionDrawable) mFabButton.getBackground();
            transition.startTransition(ANIMATION_DURATION_LONG);
            mFabButton.setTag("");
        }
    }

    /**
     * finish the animations of the ui after the download is complete. reset the button to the start
     *
     * @param success
     */
    private void animateComplete(boolean success) {
        //hide the progress again :D
        Utils.hideViewByScaleXY(mFabProgress).setDuration(ANIMATION_DURATION_MEDIUM).start();
        mProgressFabAnimation.cancel();

        // if we were not successful remove the x again :D
        if (!success) {
            //Utils.animateViewElevation(mFabButton, 0, mElavationPx);
            mFabButton.setImageDrawable(mDrawablePhoto);
            mFabButton.animate().rotationBy(360).setDuration(ANIMATION_DURATION_MEDIUM).start();
        }
        future = null;
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

                    //animate the fab
                    Utils.showViewByScale(mFabButton).setDuration(ANIMATION_DURATION_MEDIUM).start();

                    //animate the share fab
                    Utils.showViewByScale(mFabShareButton)
                            .setDuration(ANIMATION_DURATION_MEDIUM * 2)
                            .start();
                    mFabShareButton.animate()
                            .translationYBy(Utils.pxFromDp(DetailActivity.this, 64))
                            .setStartDelay(ANIMATION_DURATION_MEDIUM)
                            .setDuration(ANIMATION_DURATION_MEDIUM)
                            .start();
                }
            });

            showTitleAnimator.start();
        }
    };

    /**
     * @param titleTextColor
     * @param rgb
     */
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

    /**
     *
     */
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


    @Override
    public void onBackPressed() {
        //move the share fab below the normal fab (64 because this is the margin top + the half
        mFabShareButton.animate()
                .translationYBy((-1) * Utils.pxFromDp(this, 64))
                .setDuration(ANIMATION_DURATION_MEDIUM)
                .setListener(new CustomAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //create the fab animation and hide fabProgress animation, set an delay so those will hide after the shareFab is below the main fab
                        ViewPropertyAnimator hideFabAnimator = Utils.hideViewByScaleXY(mFabButton)
                                .setDuration(ANIMATION_DURATION_MEDIUM);

                        Utils.hideViewByScaleXY(mFabShareButton)
                                .setDuration(ANIMATION_DURATION_MEDIUM)
                                .start();
                        Utils.hideViewByScaleXY(mFabProgress)
                                .setDuration(ANIMATION_DURATION_MEDIUM)
                                .start();

                        /*
                        mTitlesContainer.startAnimation(AnimationUtils.loadAnimation(DetailActivity.this, R.anim.alpha_off));
                        mTitlesContainer.setVisibility(View.INVISIBLE);
                        */

                        //add listener so we can react after the animation is finished
                        hideFabAnimator.setListener(new CustomAnimatorListener() {

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

                        hideFabAnimator.start();

                        super.onAnimationEnd(animation);
                    }
                })
                .start();
    }

    /**
     *
     */
    private void coolBack() {
        try {
            super.onBackPressed();
        } catch (Exception e) {
            // TODO: workaround
        }
    }
}
