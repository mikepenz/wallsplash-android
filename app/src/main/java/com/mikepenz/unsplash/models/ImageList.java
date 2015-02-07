package com.mikepenz.unsplash.models;

import java.util.ArrayList;

public class ImageList {

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
