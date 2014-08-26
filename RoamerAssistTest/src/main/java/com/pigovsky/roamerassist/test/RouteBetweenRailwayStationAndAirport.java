package com.pigovsky.roamerassist.test;

import android.widget.Button;
import android.widget.EditText;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

import java.io.IOException;

public class RouteBetweenRailwayStationAndAirport extends UiAutomatorTestCase {
    /**
     * Tries to build route between railway station and Boryspil airport
     *
     * @throws UiObjectNotFoundException
     * @throws IOException
     */
    public void testRoutingBetweenRailwayStationAndAirport() throws UiObjectNotFoundException, IOException {

        /*
         * Stop previous instance of app under test
         */
        Runtime.getRuntime().exec("am force-stop com.pigovsky.roamerassist");

        // Simulate a short press on the HOME button.
        getUiDevice().pressHome();

        // We’re now in the home screen. Next, we want to simulate
        // a user bringing up the All Apps screen.
        // If you use the uiautomatorviewer tool to capture a snapshot
        // of the Home screen, notice that the All Apps button’s
        // content-description property has the value “Apps”.  We can
        // use this property to create a UiSelector to find the button.
        UiObject allAppsButton = new UiObject(new UiSelector()
                .description("Apps"));

        // Simulate a click to bring up the All Apps screen.
        allAppsButton.clickAndWaitForNewWindow();

        // In the All Apps screen, the Settings app is located in
        // the Apps tab. To simulate the user bringing up the Apps tab,
        // we create a UiSelector to find a tab with the text
        // label “Apps”.
        UiObject appsTab = new UiObject(new UiSelector()
                .text("Apps"));

        // Simulate a click to enter the Apps tab.
        appsTab.click();

        // Next, in the apps tabs, we can simulate a user swiping until
        // they come to the Settings app icon.  Since the container view
        // is scrollable, we can use a UiScrollable object.
        UiScrollable appViews = new UiScrollable(new UiSelector()
                .scrollable(true));

        // Set the swiping mode to horizontal (the default is vertical)
        appViews.setAsHorizontalList();

        // Create a UiSelector to find the Settings app and simulate
        // a user click to launch the app.
        UiObject settingsApp = appViews.getChildByText(new UiSelector()
                        .className(android.widget.TextView.class.getName()),
                "Roamer assistant"
        );
        settingsApp.clickAndWaitForNewWindow();

        // Validate that the package name is the expected one
        UiObject appExistanceValidation = new UiObject(new UiSelector()
                .packageName("com.pigovsky.roamerassist"));
        assertTrue("Unable to detect RoamerAssist",
                appExistanceValidation.exists());

        new UiObject(new UiSelector().text("S")).click();
        startSearchAddressActivity();
        typeTextIntoEditText("Vokzalna, Kyiv");
        clickTheButtonAndWaitForNewWindow();
        new UiObject(new UiSelector().text("F")).click();
        startSearchAddressActivity();
        typeTextIntoEditText("Boryspil airport");
        clickTheButtonAndWaitForNewWindow();
        new UiObject(new UiSelector().text("Calculate route")).click();
    }

    private void clickTheButtonAndWaitForNewWindow() throws UiObjectNotFoundException {
        new UiObject(new UiSelector().className(Button.class)).clickAndWaitForNewWindow();
    }

    private void typeTextIntoEditText(String address) throws UiObjectNotFoundException {
        new UiObject(new UiSelector().className(EditText.class)).setText(address);
    }

    private void startSearchAddressActivity() throws UiObjectNotFoundException {
        clickButtonAndWaitForNewWindow("Search address");
    }

    private void clickButtonAndWaitForNewWindow(String buttonText) throws UiObjectNotFoundException {
        new UiObject(new UiSelector().text(buttonText)).clickAndWaitForNewWindow();
    }
}
