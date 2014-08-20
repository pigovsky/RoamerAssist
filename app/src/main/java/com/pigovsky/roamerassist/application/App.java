package com.pigovsky.roamerassist.application;

import com.pigovsky.roamerassist.helpers.RoutePointsHelper;

/**
 * Created by yp on 18.08.2014.
 */
public class App {
    /**
     * A static field holding instance of this application object
     */
    private static App instance;
    private RoutePointsHelper.Point location;

    /**
     * A private constructor producing this application singleton.
     * By default it specifies railway station, Kyiv as the starting point, and airport Boryspil as
     * the finish point to calculate route. And 1000 meter distance as default unit
     * to put marks on the route.
     */
    private App() {
    }

    /**
     * Gets this unique application object. It also creates this object If it was accessed
     * for the first time,
     *
     * @return this unique application object
     */
    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public RoutePointsHelper.Point getLocation() {
        return location;
    }

    public void setLocation(RoutePointsHelper.Point location) {
        this.location = location;
    }
}
