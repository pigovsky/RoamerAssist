package com.pigovsky.roamerassist.model;

import android.widget.ToggleButton;

import com.nutiteq.MapView;
import com.nutiteq.services.routing.RouteActivity;

/**
 * Created by yp on 20.08.2014.
 */
public interface IRouteReceiver extends RouteActivity {
    ToggleButton[] getToggleButtonsMarkingRoute();

    MapView getMapView();
}
