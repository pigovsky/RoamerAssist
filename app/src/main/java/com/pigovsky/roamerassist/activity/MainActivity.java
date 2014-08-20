package com.pigovsky.roamerassist.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pigovsky.roamerassist.R;
import com.pigovsky.roamerassist.helpers.GetAddressTask;
import com.pigovsky.roamerassist.model.Point;
import com.pigovsky.roamerassist.model.Trip;

import java.util.Date;


public class MainActivity extends FragmentActivity
        implements LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static Trip trip;
    private MenuItem buttonRecord;
    private boolean recordingInProgress;
    private LocationManager locationManager;
    private String locationProvider;

    private SharedPreferences sharedPreferences;

    private ProgressBar progressBar;

    public static Trip getTrip() {
        return trip;
    }

    public static void setTrip(Trip trip) {
        MainActivity.trip = trip;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        progressBar = (ProgressBar) findViewById(R.id.progressbar_address);

        setTrip(new Trip(this));

        setRecordingInProgress(false);

        ((ListView) findViewById(R.id.list_trip_points)).setAdapter(getTrip());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        buttonRecord = menu.findItem(R.id.action_record);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record:
                onRecordClick();
                break;
            case R.id.action_addresses:
                onReadAddressesClick();
                break;
            case R.id.action_map:

                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void onReadAddressesClick() {
        Toast.makeText(this, "Try reading addresses", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
        (new GetAddressTask(this)).execute(getTrip());
    }

    private void onRecordClick() {
        if (isRecordingInProgress()) {
            locationManager.removeUpdates(this);
            setRecordingInProgress(false);
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        locationProvider =
                locationManager.getBestProvider(criteria, true);

        if (locationProvider == null) {
            Toast.makeText(this, "Cannot access to GPS. Is it turned on?", Toast.LENGTH_SHORT).show();
        } else {
            trackGPS();
            setRecordingInProgress(true);
        }
    }

    private void trackGPS() {
        float minDistance = Float.parseFloat(sharedPreferences.getString("delta_meters", "5"));
        long minDuration = Long.parseLong(sharedPreferences.getString("sync_frequency", "5000"));
        Toast.makeText(this, String.format("Provider %s, min.duration %d, min.distance %f", locationProvider,
                minDuration, minDistance), Toast.LENGTH_SHORT).show();
        locationManager.requestLocationUpdates(
                locationProvider, minDuration, minDistance, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Point point = new Point(new Date(), location);
        Toast.makeText(this, "Location " + point, Toast.LENGTH_SHORT).show();
        getTrip().addPoint(point);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    protected boolean isRecordingInProgress() {
        return recordingInProgress;
    }

    protected void setRecordingInProgress(boolean recordingInProgress) {
        this.recordingInProgress = recordingInProgress;
        if (buttonRecord != null) {
            buttonRecord.setTitle(
                    getString(recordingInProgress ? R.string.action_pause : R.string.action_record));
        }
    }

    public void addressesWereRead(boolean ok) {
        progressBar.setVisibility(View.GONE);
        if (ok) {
            getTrip().notifyDataSetChanged();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (isRecordingInProgress()) {
            locationManager.removeUpdates(this);
            trackGPS();
        }
    }
}
