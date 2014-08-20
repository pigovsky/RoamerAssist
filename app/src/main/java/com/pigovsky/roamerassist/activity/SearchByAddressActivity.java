package com.pigovsky.roamerassist.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.pigovsky.roamerassist.R;
import com.pigovsky.roamerassist.application.App;
import com.pigovsky.roamerassist.helpers.IPointFound;
import com.pigovsky.roamerassist.helpers.RoutePointsHelper;
import com.pigovsky.roamerassist.helpers.SearchTask;


public class SearchByAddressActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);
        findViewById(R.id.buttonSearchByAddress).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        findViewById(R.id.progressbarSearchByAddress).setVisibility(View.VISIBLE);

        String addressToSearch =
                ((EditText) findViewById(R.id.edittextSearchAddress)).getText().toString();

        new SearchTask(new IPointFound() {
            @Override
            public void pointFound(RoutePointsHelper.Point location) {
                if (location == null) {
                    Toast.makeText(SearchByAddressActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    return;
                }
                findViewById(R.id.progressbarSearchByAddress).setVisibility(View.GONE);
                App.getInstance().setLocation(location);
                finish();

            }
        }).execute(addressToSearch);

    }


}
