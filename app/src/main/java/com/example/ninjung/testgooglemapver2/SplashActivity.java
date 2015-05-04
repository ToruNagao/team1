package com.example.ninjung.testgooglemapver2;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.view.Window;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends Activity {
    //ProgressBar progressBar;
    //int progress = 100;
    //Handler handler = new Handler();
    //TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        int delay = 1000;
        Handler hdl = new Handler();

        // exits immediately if there is no internet connection
        if ( !(new ConnectionDetector(getApplicationContext())).isConnectingToInternet() ) {
            System.exit(1);
        }

        //x ms delay before runs splashHandler
        hdl.postDelayed(new splashHandler(), delay);
    }

    class splashHandler implements Runnable {
        public void run() {
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
        }

    }
}