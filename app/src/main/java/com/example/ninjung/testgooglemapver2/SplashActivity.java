package com.example.ninjung.testgooglemapver2;

/**
 * Created by Toru on 2015/04/26.
 */

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.view.Window;
import android.content.Intent;

public class SplashActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);

        Handler hdl = new Handler();
        hdl.postDelayed(new splashHandler(), 1000);
    }

    class splashHandler implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }

    }

}
