package com.example.ninjung.testgooglemapver2;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.view.Window;
import android.content.Intent;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends Activity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        int delay = 1000;
        Handler hdl = new Handler();
        ConnectionDetector connect = ConnectionDetector.getInstance(getApplicationContext());

        // exits immediately if there is no internet connection
        if (!(connect.isConnectingToInternet())) {
            System.exit(1);
        }

        imageView = (ImageView) findViewById(R.id.tireImage);
        final Animation animRotate = AnimationUtils.loadAnimation(this,R.anim.rotate);
        imageView.startAnimation(animRotate);

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
