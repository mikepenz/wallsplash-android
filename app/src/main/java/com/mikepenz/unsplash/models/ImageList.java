package com.mikepenz.unsplash.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;

@JsonObject
public class ImageList {

    @JsonField
    private ArrayList<Image> data;

    public ArrayList<Image> getData() {
        return data;
    }

    public void setData(ArrayList<Image> data) {
        this.data = data;
    }

    public ImageList() {

    }

    public ImageList(ArrayList<Image> data) {
        this.data = data;
    }
}
