package com.pigovsky.roamerassist;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pigovsky.roamerassist.model.GetAddressTask;
import com.pigovsky.roamerassist.model.Point;
import com.pigovsky.roamerassist.model.Trip;

import java.util.Date;


public class MainActivity extends FragmentActivity
    implements View.OnClickListener, LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private Trip trip;
    private Button buttonRecord;
    private boolean recordingInProgress;
    private LocationManager locationManager;
    private String locationProvider;

    private SharedPreferences sharedPreferences;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonRecord = (Button)findViewById(R.id.button_record);

        progressBar = (ProgressBar)findViewById(R.id.progressbar_address);

        trip = new Trip(this);

        setRecordingInProgress(false);

        for(int id : new int[] {
                R.id.button_settings,
                R.id.button_read_addresses,
                R.id.button_record,
                R.id.button_map
        } )
            ((Button)findViewById(id)).setOnClickListener(this);

        ((ListView)findViewById(R.id.list_trip_points)).setAdapter(trip);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onReadAddressesClick()
    {
        Toast.makeText(this,"Try reading addresses",Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
        (new GetAddressTask(this)).execute(trip);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_record:
                onRecordClick();
                break;
            case R.id.button_read_addresses:
                onReadAddressesClick();
                break;
            case R.id.button_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.button_map:
                startActivity(new Intent(this, MapActivity.class));
                break;
        }
    }

    private void onRecordClick() {
        if (isRecordingInProgress()) {
            locationManager.removeUpdates(this);
            setRecordingInProgress(false);
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        locationProvider =
                locationManager.getBestProvider(criteria,true);

        if (locationProvider==null) {
            Toast.makeText(this, "Cannot access to GPS. Is it turned on?", Toast.LENGTH_SHORT).show();
        }
        else {
            trackGPS();
            setRecordingInProgress(true);
        }
    }

    private void trackGPS() {
        float minDistance = Float.parseFloat(sharedPreferences.getString("delta_meters", "5"));
        long minDuration = Long.parseLong(sharedPreferences.getString("sync_frequency","5000"));
        Toast.makeText(this, String.format("Provider %s, min.duration %d, min.distance %f", locationProvider,
                minDuration, minDistance), Toast.LENGTH_SHORT).show();
        locationManager.requestLocationUpdates(
                locationProvider, minDuration, minDistance, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Point point = new Point(new Date(), location);
        Toast.makeText(this,"Location "+point, Toast.LENGTH_SHORT).show();
        trip.addPoint(point);
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
        buttonRecord.setText(recordingInProgress?"Pause":"Record");
    }

    public void addressesWereRead(boolean ok)
    {
        progressBar.setVisibility(View.GONE);
        if (ok)
            trip.notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (isRecordingInProgress()){
            locationManager.removeUpdates(this);
            trackGPS();
        }
    }
}
