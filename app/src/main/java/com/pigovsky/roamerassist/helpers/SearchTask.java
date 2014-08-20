package com.pigovsky.roamerassist.helpers;

import android.os.AsyncTask;

public class SearchTask extends
        AsyncTask<String, Void, RoutePointsHelper.Point> {

    private IPointFound foundListener;

    public SearchTask(IPointFound foundListener) {
        this.foundListener = foundListener;
    }

    @Override
    protected RoutePointsHelper.Point doInBackground(String... params) {
        return GeocoderHelper.fetchLocationFromAddressUsingGoogleMap(params[0]);
    }

    @Override
    protected void onPostExecute(RoutePointsHelper.Point location) {
        foundListener.pointFound(location);
    }
}