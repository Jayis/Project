package com.sagax.player;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.TextView;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.List;

/**
 * Created by JAYIS4176 on 2015/2/7.
 */
public class Tools {

    private Activity activity;

    public Tools (Activity act) {
        activity = act;
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void showString (String string) {
        TextView textView = new TextView(activity);
        textView.setTextSize(20);
        textView.setText(string);
        activity.setContentView(textView);
    }

    public String getCSRF(DefaultHttpClient httpClient) {
        String csrf = "";

        List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookie = cookies.get(i);
            if (cookie.getName() == "csrftoken") {
                csrf = cookie.getValue();
            }
        }

        return csrf;
    }
}
