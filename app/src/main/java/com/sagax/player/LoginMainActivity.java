package com.sagax.player;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class LoginMainActivity extends Activity {

    // static variable
    private static DefaultHttpClient client = new DefaultHttpClient();
    private static Boolean isThereNetwork;
    private static SQLiteDatabase database;
    private static DBHelper dbHelper;
    private static SharedPreferences sharedPref;

    // urls
    public static String url_site = "http://106.187.36.145:3000";
    public static String url_list_json = url_site + "/list.json";
    public static String url_login = url_site + "/accounts/login/";
    public static String url_auth = url_site + "/accounts/auth/";

    //
    public static String lastUser;
    public static String lastPassword;
    private static Cursor cursor_lastUser;
    //
    public static String curUser;
    public static String curPassword;
    //
    private TextView textView_notSuccess;
    //
    private Tools tools = new Tools(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // static variable
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }
        if (sharedPref == null) {
            sharedPref = this.getSharedPreferences(getString(R.string.preference_lastUser), Context.MODE_PRIVATE);
        }
        if (database == null) {
            database = dbHelper.getWritableDatabase();
        }
        isThereNetwork = tools.isConnected();

        // check network
        if (isThereNetwork) new hh().execute();
        else {
            if (sharedPref.getString("last_user", null) == null) {
                tools.showString("No Network!!");
            } else {
                //goto_CheckSongList();
                new gg().execute();
            }
        }

    }

    public void setLoginPage(){
        if ( seekLastUser() ) {
            last_login(null);
        }
        else {
            String Notify;

            setContentView(R.layout.activity_login_main);

            textView_notSuccess = (TextView) findViewById(R.id.not_success);
            TextView textView_areYou = (TextView) findViewById(R.id.are_you);
            //Button button_resumeLogin = (Button) findViewById(R.id.resume_login);

            Notify = "Please Login~";
            textView_areYou.setText(Notify);
            //button_resumeLogin.setEnabled(false);
        }
    }

    public static boolean seekLastUser () {
        lastUser = sharedPref.getString("last_user", null);

        if ( lastUser!= null) {
            cursor_lastUser = database.rawQuery("SELECT * FROM " + DBHelper.TABLE_USERS + " WHERE " + DBHelper.COLUMN_USERNAME + " = '" + lastUser + "'", null);

            if (cursor_lastUser.getCount() > 0) {
                // find one
                cursor_lastUser.moveToFirst();
                // get he's password
                lastPassword = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(DBHelper.COLUMN_PASSWORD));
                return true;
            }
        }
        return false;
    }

    // for button
    public void last_login (View view) {
        curUser = lastUser;
        curPassword = lastPassword;
        new jj().execute();
    }

    // for button
    public void login (View view) {
        EditText ET_username = (EditText) findViewById(R.id.username);
        curUser = ET_username.getText().toString();
        EditText ET_password = (EditText) findViewById(R.id.password);
        curPassword = ET_password.getText().toString();

        textView_notSuccess.setText("");

        new jj().execute();
    }

    private class jj extends BG_Login{
        @Override
        protected void onPostExecute (Integer result) {
            try{
                super.onPostExecute(result);

                if (jsonObject.getInt("login") == 1) {
                    new gg().execute();
                }
                else {
                    textView_notSuccess.setText("login fail..., plz retry");
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class hh extends BG_IfLogin{
        @Override
        protected void onPostExecute (Integer result) {
            try {
                super.onPostExecute(result);

                if (jsonObject.getInt("login") == 1) {
                    new gg().execute();
                }
                else{
                    setLoginPage();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class gg extends BG_CheckSongList{
        @Override
        protected void onPostExecute (Integer result) {
            goto_PlayerMain();
        }
    }

    public void goto_PlayerMain(){
        if (true) {
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);

            finish();
        }
    }

    public static DBHelper shareDBHelper () { return  dbHelper; }
    public static SQLiteDatabase shareDB () { return  database; }
    public static DefaultHttpClient shareClient () {
        return client;
    }
    public static SharedPreferences shareSharePref () {
        return sharedPref;
    }
}
