package com.example.ninjung.testgooglemapver2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
    private static ConnectionDetector instance;
    private Context _context;

    private ConnectionDetector(Context context) {
        this._context = context;
    }

    /**
     * @param _context: context of application
     * @return instance of ConnectionDetector
     */
    public static ConnectionDetector getInstance(Context _context) {
        instance = new ConnectionDetector(_context);
        return instance;
    }

    /**
     * @return boolean indicating if the device is connected to the Internet
     */
    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
