package com.pigovsky.roamerassist.helpers;

/**
 * Created by Pascal (http://stackoverflow.com/questions/9272918/service-not-available-in-geocoder). Thanks for him.
 * Apr 6 '13 at 16:16
 */
import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeocoderHelper
{
    private static final AndroidHttpClient ANDROID_HTTP_CLIENT = AndroidHttpClient.newInstance(GeocoderHelper.class.getName());

    public static String fetchCityNameUsingGoogleMap(Location location)
    {
        String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + ","
                + location.getLongitude() + "&sensor=false&language=uk";

        try
        {
            JSONObject googleMapResponse = new JSONObject(ANDROID_HTTP_CLIENT.execute(new HttpGet(googleMapUrl),
                    new BasicResponseHandler()));

            JSONArray results = (JSONArray) googleMapResponse.get("results");

            JSONObject result = results.getJSONObject(0);

            Log.println(Log.INFO, "JSON:",result.toString());

            String address = result.get("formatted_address").toString();

            Log.println(Log.INFO, "JSON:", address);
            return address;
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
        return null;
    }
}