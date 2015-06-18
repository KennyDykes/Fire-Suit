package com.example.kenny.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Kenith Dykes on 4/27/2015.
 * Splash Screen of the Application uses a Thread Timer
 *
 */
public class SplashScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent mainIntent = new Intent(SplashScreen.this,Rescue_Me.class);
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }

}
