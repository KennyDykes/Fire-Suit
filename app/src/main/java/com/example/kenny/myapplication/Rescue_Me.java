package com.example.kenny.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * This is the Menu Page after the SplashScreen has gone away
 */

public class Rescue_Me extends ActionBarActivity implements View.OnClickListener
    {
        /**
         * Declares fields for the ImageButtons
         */
        ImageButton track, map,exit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_acttivity);
        track=(ImageButton)findViewById(R.id.track);
        map=(ImageButton) findViewById(R.id.mapping);
        exit=(ImageButton)findViewById(R.id.exit);
        track.setOnClickListener(this);
        map.setOnClickListener(this);
        exit.setOnClickListener(this);
    }

        /**
         * Based on the user's button selection determines the navigation of the next activity
         * The first case is intended for a firefighter who wants to be tracked
         * The second case is intended for a firechief who wants watch the movements on a firefighter using Google Maps
         * The third case exits the application
         */
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.track:
                Toast.makeText(getBaseContext(), "Starting Track Activity", Toast.LENGTH_SHORT).show();
                Intent i= new Intent(this, DeviceListActivity.class);
                startActivity(i);
                break;

            case R.id.mapping:
                Toast.makeText(getBaseContext(), "Starting Maps", Toast.LENGTH_SHORT).show();
                Intent j= new Intent(this, MapsActivity.class);
                startActivity(j);
                break;
            case R.id.exit:
                Toast.makeText(getBaseContext(), "Exiting", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_title_acttivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
