package com.mikepenz.unsplash.other.logansquare;

import com.bluelinelabs.logansquare.typeconverters.DateTypeConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DateConverter extends DateTypeConverter {

    private SimpleDateFormat mDateFormat;

    public DateConverter() {
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public DateFormat getDateFormat() {
        return mDateFormat;
    }

}