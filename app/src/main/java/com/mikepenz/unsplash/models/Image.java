package com.mikepenz.unsplash.models;

import android.support.v7.graphics.Palette;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Image implements Serializable {

    private static final DateFormat sdf = SimpleDateFormat.getDateInstance();

    private String color;
    private String image_src;
    private String author;
    private Date date;
    private Date modified_date;
    private float ratio;
    private int width;
    private int height;
    private int featured;
    private int category;

    transient private Palette.Swatch swatch;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getHighResImage(int minWidth, int minHeight) {
        String url = image_src + "?q=100&fm=jpg";

        if (minWidth > 0 && minHeight > 0) {
            int phoneRatio = minWidth / minHeight;
            if (phoneRatio < getRatio()) {
                url = url + "&h=" + minHeight;
            } else {
                url = url + "&w=" + minWidth;
            }
        }

        return url;
    }

    public String getImage_src(int screenWidth) {
        return image_src + "?q=75&w=720&fit=max&fm=jpg";

        /*
        wait with this one for now. i don't want to bring up the generation quota of unsplash
        String url = image_src + "?q=75&fit=max&fm=jpg";

        if (screenWidth > 0) {
            //it's enough if we load an image with 2/3 of the size
            url = url + "&w=" + (screenWidth / 3 * 2);
        }

        return url;
        */
    }

    public void setImage_src(String image_src) {
        this.image_src = image_src;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public String getReadableDate() {
        if (date != null) {
            return sdf.format(date);
        } else {
            return "";
        }
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getModified_date() {
        return modified_date;
    }

    public String getReadableModified_Date() {
        if (modified_date != null) {
            return sdf.format(modified_date);
        } else {
            return "";
        }
    }

    public void setModified_date(Date modified_date) {
        this.modified_date = modified_date;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Palette.Swatch getSwatch() {
        return swatch;
    }

    public void setSwatch(Palette.Swatch swatch) {
        this.swatch = swatch;
    }

    public int getFeatured() {
        return featured;
    }

    public void setFeatured(int featured) {
        this.featured = featured;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
