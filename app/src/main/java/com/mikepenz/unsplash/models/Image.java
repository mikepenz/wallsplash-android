package com.mikepenz.unsplash.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Image implements Serializable {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private String color;
    private String image_src;
    private String author;
    private Date date;

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
}
