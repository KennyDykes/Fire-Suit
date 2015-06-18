package com.example.kenny.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * This Activity simply gets the user's name using an EditText and ImageButton
 */

public class UsernameActivity extends Activity implements View.OnClickListener {
    /**
     * This allows user to enter his name
     */
    EditText receive;
    /**
     * This will hold the MAC Address of Bluetooth device
     */
    String MAC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.username);
        Intent clingon= getIntent();
        receive=(EditText)findViewById(R.id.name_editText);
        ImageButton send=(ImageButton)findViewById(R.id.comeon);
        send.setOnClickListener(this);
        MAC=clingon.getStringExtra("Key");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_username, menu);
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
    /**
     * The OnClick function passes the Bluetooth MAC Address and the user's name to the next activty
     */
    public void onClick(View v)
    {

        switch(v.getId())
        {
            case R.id.comeon:
                Toast.makeText(getBaseContext(), "Page Is Loading", Toast.LENGTH_LONG).show();
                Intent i= new Intent(this, TrackerActivity.class);
                i.putExtra("username",receive.getText().toString());
                i.putExtra("address",MAC);
                startActivity(i);

                break;
        }
    }
}
