package com.pigovsky.roamerassist.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nutiteq.MapView;
import com.nutiteq.components.Color;
import com.nutiteq.components.Components;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.Options;
import com.nutiteq.geometry.Marker;
import com.nutiteq.log.Log;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.projections.Projection;
import com.nutiteq.rasterdatasources.HTTPRasterDataSource;
import com.nutiteq.rasterdatasources.RasterDataSource;
import com.nutiteq.rasterlayers.RasterLayer;
import com.nutiteq.services.routing.Route;
import com.nutiteq.style.MarkerStyle;
import com.nutiteq.style.StyleSet;
import com.nutiteq.ui.DefaultLabel;
import com.nutiteq.ui.Label;
import com.nutiteq.utils.UnscaledBitmapLoader;
import com.nutiteq.vectorlayers.GeometryLayer;
import com.nutiteq.vectorlayers.MarkerLayer;
import com.pigovsky.roamerassist.R;
import com.pigovsky.roamerassist.application.App;
import com.pigovsky.roamerassist.helpers.RoutePointsHelper;
import com.pigovsky.roamerassist.listeners.EndpointsSetupListener;
import com.pigovsky.roamerassist.model.IRouteReceiver;
import com.pigovsky.roamerassist.model.MyLocationCircle;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends Activity implements IRouteReceiver, View.OnClickListener {
    private static final float MARKER_SIZE = 0.4f;
    private MapView mapView;
    private Marker startMarker;
    private Marker stopMarker;
    private MarkerLayer markerLayer;

    private LocationListener locationListener;
    private GeometryLayer locationLayer;
    private Timer locationTimer;
    private GeometryLayer routeLayer;
    private EndpointsSetupListener mapListener;
    private StyleSet<MarkerStyle> distanceMarkerStyleSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // enable logging for troubleshooting - optional
        Log.enableAll();
        Log.setTag("hellomap");

        // 1. Get the MapView from the Layout xml - mandatory
        mapView = (MapView) findViewById(R.id.mapView);

        // Optional, but very useful: restore map state during device rotation,
        // it is saved in onRetainNonConfigurationInstance() below
        Components retainObject = (Components) getLastNonConfigurationInstance();
        if (retainObject != null) {
            // just restore configuration and update listener, skip other initializations
            mapView.setComponents(retainObject);
            return;
        } else {
            // 2. create and set MapView components - mandatory
            mapView.setComponents(new Components());
        }

        // 3. Define map layer for basemap - mandatory.
        // Here we use MapQuest open tiles.
        // We use online data source for the tiles and the URL is given as template. Almost all online tiled maps use EPSG3857 projection.
        RasterDataSource dataSource = new HTTPRasterDataSource(new EPSG3857(), 0, 18, "http://otile1.mqcdn.com/tiles/1.0.0/osm/{zoom}/{x}/{y}.png");

        RasterLayer mapLayer = new RasterLayer(dataSource, 0);

        mapView.getLayers().setBaseLayer(mapLayer);

        adjustMapDpi();

        // Show performance indicator
        //mapView.getOptions().setFPSIndicator(true);

        // Increase raster tile download speed by doing 4 downloads in parallel
        //mapView.getOptions().setRasterTaskPoolSize(4);

        // set initial map view camera - optional. "World view" is default
        // Location: San Francisco
        // NB! it must be in base layer projection (EPSG3857), so we convert it from lat and long
        mapView.setFocusPoint(mapView.getLayers().getBaseLayer().getProjection().fromWgs84(-122.41666666667f, 37.76666666666f));
        // rotation - 0 = north-up
        mapView.setMapRotation(0f);
        // zoom - 0 = world, like on most web maps
        mapView.setZoom(16.0f);
        // tilt means perspective view. Default is 90 degrees for "normal" 2D map view, minimum allowed is 30 degrees.
        //mapView.setTilt(65.0f);

        // Activate some mapview options to make it smoother - optional
        mapView.getOptions().setPreloading(true);
        mapView.getOptions().setSeamlessHorizontalPan(true);
        mapView.getOptions().setTileFading(true);
        mapView.getOptions().setKineticPanning(true);
        mapView.getOptions().setDoubleClickZoomIn(true);
        mapView.getOptions().setDualClickZoomOut(true);

        // set sky bitmap - optional, default - white
        mapView.getOptions().setSkyDrawMode(Options.DRAW_BITMAP);
        mapView.getOptions().setSkyOffset(4.86f);
        mapView.getOptions().setSkyBitmap(
                UnscaledBitmapLoader.decodeResource(getResources(),
                        R.drawable.sky_small)
        );

        // Map background, visible if no map tiles loaded - optional, default - white
        mapView.getOptions().setBackgroundPlaneDrawMode(Options.DRAW_BITMAP);
        mapView.getOptions().setBackgroundPlaneBitmap(
                UnscaledBitmapLoader.decodeResource(getResources(),
                        R.drawable.background_plane)
        );
        mapView.getOptions().setClearColor(Color.WHITE);

        // configure texture caching - optional, suggested
        mapView.getOptions().setTextureMemoryCacheSize(40 * 1024 * 1024);
        mapView.getOptions().setCompressedMemoryCacheSize(8 * 1024 * 1024);

        // define online map persistent caching - optional, suggested. Default - no caching
        //mapView.getOptions().setPersistentCachePath(this.getDatabasePath("mapcache").getPath());
        // set persistent raster cache limit to 100MB
        //mapView.getOptions().setPersistentCacheSize(100 * 1024 * 1024);

        // 5. Add simple marker to map.
        // define marker style (image, size, color)
        Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.olmarker);
        MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(0.5f).setColor(Color.WHITE).build();

        // define label what is shown when you click on marker
        Label markerLabel = new DefaultLabel("San Francisco", "Here is a marker");

        // define location of the marker, it must be converted to base map coordinate system
        MapPos markerLocation = mapLayer.getProjection().fromWgs84(-122.416667f, 37.766667f);

        Bitmap distanceMarker = UnscaledBitmapLoader.decodeResource(getResources(),
                R.drawable.point);

        distanceMarkerStyleSet = new StyleSet<MarkerStyle>(
                MarkerStyle.builder().setBitmap(distanceMarker).setColor(Color.BLUE)
                        .setSize(.2f).build()
        );

        Bitmap olMarker = UnscaledBitmapLoader.decodeResource(getResources(),
                R.drawable.olmarker);
        StyleSet<MarkerStyle> startMarkerStyleSet = new StyleSet<MarkerStyle>(
                MarkerStyle.builder().setBitmap(olMarker).setColor(Color.GREEN)
                        .setSize(MARKER_SIZE).build()
        );
        startMarker = new Marker(new MapPos(0, 0), new DefaultLabel("Start"),
                startMarkerStyleSet, null);

        StyleSet<MarkerStyle> stopMarkerStyleSet = new StyleSet<MarkerStyle>(
                MarkerStyle.builder().setBitmap(olMarker).setColor(Color.RED)
                        .setSize(MARKER_SIZE).build()
        );
        stopMarker = new Marker(new MapPos(0, 0), new DefaultLabel("Stop"),
                stopMarkerStyleSet, null);

        // create layer and add object to the layer, finally add layer to the map.
        // All overlay layers must be same projection as base layer, so we reuse it
        markerLayer = new MarkerLayer(mapLayer.getProjection());
        markerLayer.add(new Marker(markerLocation, markerLabel, markerStyle, null));
        mapView.getLayers().addLayer(markerLayer);

        this.routeLayer = new GeometryLayer(new EPSG3857());
        mapView.getLayers().addLayer(routeLayer);

        // add event listeners
        this.mapListener = new EndpointsSetupListener(this);
        mapView.getOptions().setMapListener(mapListener);
        findViewById(R.id.buttonCalculateRoute).setOnClickListener(mapListener);
        for (int id : new int[]{
                R.id.buttonZoomOut,
                R.id.buttonZoomIn,
                R.id.buttonSearchByAddressActivity
        }) {
            findViewById(id).setOnClickListener(this);
        }

        // make markers invisible until we need them
        startMarker.setVisible(false);
        stopMarker.setVisible(false);
        markerLayer.add(startMarker);
        markerLayer.add(stopMarker);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        Log.debug("onRetainNonConfigurationInstance");
        return this.mapView.getComponents();
    }


    @Override
    protected void onStart() {
        super.onStart();

        // 4. Start the map - mandatory.
        mapView.startMapping();

        // Create layer for location circle
        locationLayer = new GeometryLayer(mapView.getLayers().getBaseProjection());
        mapView.getComponents().layers.addLayer(locationLayer);

        // add GPS My Location functionality
        final MyLocationCircle locationCircle = new MyLocationCircle(locationLayer);
        initGps(locationCircle);

        // Run animation
        locationTimer = new Timer();
        locationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                locationCircle.update(mapView.getZoom());
            }
        }, 0, 50);

        RoutePointsHelper.Point location = App.getInstance().getLocation();
        if (location != null) {
            MapPos focusPoint = mapView.getLayers().getBaseProjection().fromWgs84(location.getLongitude(), location.getLatitude());
            mapListener.onMapClicked(focusPoint.x, focusPoint.y, false);
            mapView.setFocusPoint(focusPoint);
        }
    }

    @Override
    protected void onStop() {
        // Stop animation
        locationTimer.cancel();

        // Remove created layer
        mapView.getComponents().layers.removeLayer(locationLayer);

        // remove GPS support, otherwise we will leak memory
        deinitGps();

        // Note: it is recommended to move startMapping() call to onStart method and implement onStop method (call MapView.stopMapping() from onStop).
        mapView.stopMapping();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void initGps(final MyLocationCircle locationCircle) {
        final Projection proj = mapView.getLayers().getBaseLayer().getProjection();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationCircle.setLocation(proj, location);
                locationCircle.setVisible(true);

                // recenter automatically to GPS point
                // TODO in real app it can be annoying this way, add extra control that it is done only once
                //mapView.setFocusPoint(mapView.getLayers().getBaseProjection().fromWgs84(location.getLongitude(), location.getLatitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.debug("GPS onStatusChanged " + provider + " to " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.debug("GPS onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.debug("GPS onProviderDisabled");
            }
        };

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

    }

    protected void deinitGps() {
        // remove listeners from location manager - otherwise we will leak memory
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    // adjust zooming to DPI, so texts on rasters will be not too small
    // useful for non-retina rasters, they would look like "digitally zoomed"
    private void adjustMapDpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float dpi = metrics.densityDpi;
        // following is equal to  -log2(dpi / DEFAULT_DPI)
        float adjustment = (float) -(Math.log(dpi / DisplayMetrics.DENSITY_HIGH) / Math.log(2));
        Log.debug("adjust DPI = " + dpi + " as zoom adjustment = " + adjustment);
        mapView.getOptions().setTileZoomLevelBias(adjustment / 2.0f);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonZoomIn:
                mapView.zoomIn();
                break;
            case R.id.buttonZoomOut:
                mapView.zoomOut();
                break;
            case R.id.buttonSearchByAddressActivity:
                startActivity(new Intent(this, SearchByAddressActivity.class));
                break;
            default:
                break;
        }
    }


    @Override
    public ToggleButton[] getToggleButtonsMarkingRoute() {
        return new ToggleButton[]{
                (ToggleButton) findViewById(R.id.buttonMarkRouteStart),
                (ToggleButton) findViewById(R.id.buttonMarkRouteFinish)
        };
    }

    @Override
    public void showRoute(double v, double v2, double v3, double v4) {

    }

    @Override
    public void setStartMarker(MapPos mapPos) {
        startMarker.setMapPos(mapPos);
        startMarker.setVisible(true);
    }

    @Override
    public void setStopMarker(MapPos mapPos) {
        stopMarker.setMapPos(mapPos);
        stopMarker.setVisible(true);
    }

    @Override
    public void routeResult(Route route) {
        if (route.getRouteResult() != Route.ROUTE_RESULT_OK) {
            Toast.makeText(this, "Route error", Toast.LENGTH_LONG).show();
            return;
        }

        routeLayer.clear();

        routeLayer.add(route.getRouteLine());
        Log.debug("route line points: " + route.getRouteLine().getVertexList().size());

        mapView.requestRender();
        Toast.makeText(this, "Route " + route.getRouteSummary(), Toast.LENGTH_LONG).show();
        mapListener.getDirectionsService().startRoutePointMarkerLoading(markerLayer, MARKER_SIZE);

        markerLayer.clear();
        markerLayer.add(startMarker);
        markerLayer.add(stopMarker);

        double[] distanceToPoints = RoutePointsHelper.computeDistanceToPoints(
                RoutePointsHelper.toPoints(mapView.getLayers().getBaseLayer().getProjection(), route.getRouteLine().getVertexList())
        );

        for (Marker marker : RoutePointsHelper.calculateMarksOnRegularDistances(
                route.getRouteLine().getVertexList(),
                distanceToPoints,
                1000d, distanceMarkerStyleSet)) {
            markerLayer.add(marker);
        }
    }

    @Override
    public MapView getMapView() {
        return mapView;
    }
}