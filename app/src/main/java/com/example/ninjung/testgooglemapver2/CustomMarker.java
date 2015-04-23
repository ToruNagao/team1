package com.example.ninjung.testgooglemapver2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ContextThemeWrapper;

/**
 * Created by ninjung on 4/20/15.
 */
public class CustomMarker extends ContextThemeWrapper {
    private Bitmap bmp;
    public void CustomMarker(){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bmp = Bitmap.createBitmap(100, 100, conf);
        Canvas canvas1 = new Canvas(bmp);

// paint defines the text color,
// stroke width, size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

//modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.darkgreen_parking), 0,0, color);
        canvas1.drawText("Ninjung Parked!", 30, 40, color);
    }

    public Bitmap getBmp() {
        return bmp;
    }
}
