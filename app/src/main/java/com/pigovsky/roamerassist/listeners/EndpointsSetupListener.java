package com.pigovsky.roamerassist.listeners;

import android.view.View;
import android.widget.ToggleButton;

import com.nutiteq.components.MapPos;
import com.nutiteq.geometry.VectorElement;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.projections.Projection;
import com.nutiteq.services.routing.MapQuestDirections;
import com.nutiteq.style.LineStyle;
import com.nutiteq.style.StyleSet;
import com.nutiteq.ui.MapListener;
import com.pigovsky.roamerassist.R;
import com.pigovsky.roamerassist.model.EndpointType;
import com.pigovsky.roamerassist.model.IRouteReceiver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yp on 18.08.2014.
 */
public class EndpointsSetupListener extends MapListener implements View.OnClickListener {
    private static final String MAPQUEST_KEY = "Fmjtd%7Cluub2qu82q%2C70%3Do5-961w1w";
    private final ToggleButton toggleButtonStart;
    private final ToggleButton toggleButtonFinish;
    private IRouteReceiver activity;
    private EndpointType currentRouteEndpointType;
    private ToggleButton[] toggleButtonsMarkingRoute;
    private MapPos startPos;
    private MapPos finishPos;
    private MapQuestDirections directionsService;

    public EndpointsSetupListener(IRouteReceiver activity) {
        this.activity = activity;
        this.toggleButtonsMarkingRoute = activity.getToggleButtonsMarkingRoute();
        this.toggleButtonStart = toggleButtonsMarkingRoute[0];
        this.toggleButtonFinish = toggleButtonsMarkingRoute[1];

        for (View v : toggleButtonsMarkingRoute) {
            v.setOnClickListener(this);
        }
    }

    public EndpointType getCurrentRouteEndpointType() {
        return currentRouteEndpointType;
    }

    public void setCurrentRouteEndpointType(EndpointType currentRouteEndpointType) {
        this.currentRouteEndpointType = currentRouteEndpointType;
    }

    @Override
    public void onMapMoved() {

    }

    @Override
    public void onMapClicked(final double x, final double y,
                             final boolean longClick) {
        if (getCurrentRouteEndpointType() == EndpointType.No) {
            return;
        } else if (getCurrentRouteEndpointType() == EndpointType.Start) {
            this.startPos = (new EPSG3857()).toWgs84(x, y);
            activity.setStartMarker(new MapPos(x, y));
        } else if (getCurrentRouteEndpointType() == EndpointType.Finish) {
            this.finishPos = (new EPSG3857()).toWgs84(x, y);
            activity.setStopMarker(new MapPos(x, y));
        }
        for (ToggleButton v : toggleButtonsMarkingRoute) {
            v.setChecked(false);
        }
        setCurrentRouteEndpointType(EndpointType.No);
    }

    @Override
    public void onVectorElementClicked(VectorElement vectorElement, double v, double v2, boolean b) {

    }

    @Override
    public void onLabelClicked(VectorElement vectorElement, boolean b) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.buttonMarkRouteStart ||
                id == R.id.buttonMarkRouteFinish) {
            setCurrentRouteEndpointType(EndpointType.No);

            if (((ToggleButton) view).isChecked()) {
                switch (id) {
                    case R.id.buttonMarkRouteStart:
                        setCurrentRouteEndpointType(EndpointType.Start);
                        toggleButtonFinish.setChecked(false);
                        break;
                    case R.id.buttonMarkRouteFinish:
                        setCurrentRouteEndpointType(EndpointType.Finish);
                        toggleButtonStart.setChecked(false);
                        break;
                    default:
                        break;
                }
            }
            return;
        }
        switch (id) {
            case R.id.buttonCalculateRoute:
                calculateRoute();
                break;
        }
    }

    private void calculateRoute() {
        if (startPos == null || finishPos == null) {
            return;
        }
        Projection proj = activity.getMapView().getLayers().getBaseLayer().getProjection();

        StyleSet<LineStyle> routeLineStyle = new StyleSet<LineStyle>(LineStyle.builder().setWidth(0.05f).setColor(0xff9d7050).build());
        Map<String, String> routeOptions = new HashMap<String, String>();
        routeOptions.put("unit", "K"); // K - km, M - miles
        routeOptions.put("routeType", "fastest");
        // Add other route options here, see http://open.mapquestapi.com/directions/

        setDirectionsService(new MapQuestDirections(activity, startPos, finishPos, routeOptions, MAPQUEST_KEY, proj, routeLineStyle));
        getDirectionsService().route();
    }

    public MapQuestDirections getDirectionsService() {
        return directionsService;
    }

    public void setDirectionsService(MapQuestDirections directionsService) {
        this.directionsService = directionsService;
    }
}
