package com.mikepenz.unsplash.models;

import android.support.v7.graphics.Palette;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Image implements Serializable {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private String color;
    private String image_src;
    private String author;
    private Date date;
    private Date modified_date;
    private float ratio;
    private int width;
    private int height;

    transient private Palette.Swatch swatch;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getHighResImage() {
        return image_src + "?q=100&fm=jpg";
    }

    public String getImage_src() {
        return image_src + "?q=75&w=720&fit=max&fm=jpg";
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
        return sdf.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getModified_date() {
        return modified_date;
    }

    public String getReadableModified_Date() {
        return sdf.format(modified_date);
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
}
