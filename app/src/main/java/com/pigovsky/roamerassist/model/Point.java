package com.pigovsky.roamerassist.model;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.pigovsky.roamerassist.helpers.GeocoderHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by yp on 17.07.2014.
 */
public class Point {
    private Date date;
    private Location location;
    private String address;
    private int color = Color.GREEN;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public long getDateInMilliseconds(){
        return date.getTime();
    }

    public Point(Date date, Location location){
        this.date=date;
        this.location=location;
        address=null;
    }

    public String[] asStringRow()
    {
        return new String[]{simpleDateFormat.format(date), this.toString()};
    }

    public void readAddress(Geocoder geocoder){
        if (getAddress()!=null)
            return;

        if (geocoder==null) {
            setAddress(GeocoderHelper.fetchCityNameUsingGoogleMap(location));
            return;
        }

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
        }
        catch (Exception e) {
            Log.e("readAddress", "Arguments " +
                    getLongLatString() +
                    " passed to address service");
            e.printStackTrace();
            setAddress(GeocoderHelper.fetchCityNameUsingGoogleMap(location));
            return;
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            setAddress(String.format(
                    "%s, %s, %s",
                    address.getMaxAddressLineIndex() > 0 ?
                            address.getAddressLine(0) : "",
                    address.getLocality(),
                    address.getCountryName()
            ));
        }
    }

    @Override
    public String toString() {
        return getAddress() ==null?
                getLongLatString() :
                getAddress();
    }

    private String getLongLatString() {
        return String.format("Longitude %.1f, latitude %.1f",
                location.getLongitude(),
                location.getLatitude());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
