package com.mikepenz.unsplash.network;

/**
 * Created by saulmm on 15/11/14.
 */
public class Api {
    private final static String ENDPOINT = "http://mikepenz.com/android/unsplash/";

    public static String getLastImages() {
        return ENDPOINT + "images?by=date&order=desc";
    }
}
