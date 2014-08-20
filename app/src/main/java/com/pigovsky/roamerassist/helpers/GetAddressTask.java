package com.pigovsky.roamerassist.helpers;

import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;

import com.pigovsky.roamerassist.activity.MainActivity;
import com.pigovsky.roamerassist.model.Point;
import com.pigovsky.roamerassist.model.Trip;

import java.util.Locale;

/**
 * Created by yp on 17.07.2014.
 */
public class GetAddressTask extends
        AsyncTask<Trip, Void, Boolean> {

    private Geocoder geocoder = null;
    private MainActivity activity;

    public GetAddressTask(MainActivity context) {
        super();

        activity = context;

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.GINGERBREAD
                &&
                Geocoder.isPresent()) {
            geocoder =
                    new Geocoder(context, Locale.getDefault());
        }
    }

    @Override
    protected Boolean doInBackground(Trip... params) {

        Trip trip = params[0];

        for (Point p : trip.getPoints()) {
            p.readAddress(geocoder);
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean ok) {
        activity.addressesWereRead(ok);
    }
}