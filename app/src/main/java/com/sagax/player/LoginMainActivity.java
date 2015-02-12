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
import org.json.JSONObject;

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
    private static String url_site = "http://106.187.36.145:3000";
    private static String url_list_json = url_site + "/list.json";
    private String url_login = url_site + "/accounts/login/";
    private String url_auth = url_site + "/accounts/auth/";

    //
    private String lastUser;
    private String lastPassword;
    private Cursor cursor_lastUser;
    //
    private String curUser;
    private String curPassword;
    private Cursor cursor_curUser;
    private String jsonSTR;
    //
    private TextView textView_notSuccess;
    //
    private Tools tools = new Tools(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // static variable
        dbHelper = new DBHelper(this);
        sharedPref = this.getSharedPreferences(getString(R.string.preference_lastUser), Context.MODE_PRIVATE);
        database = dbHelper.getWritableDatabase();
        isThereNetwork = tools.isConnected();

        // check network
        if (isThereNetwork) new BG_IfLogin().execute();
        else {
            if (sharedPref.getString("last_user", null) == null) {
                tools.showString("No Network!!");
            }
            else {
                goto_CheckSongList();
            }
        }

    }

    private class BG_IfLogin extends AsyncTask<String, Integer, Integer>
    {
        // network response
        JSONObject jsonObject;
        HttpResponse response;
        HttpEntity resEntity;
        String responseSTR = "";

        @Override
        protected Integer doInBackground (String... params) {
            try {
                // get server response from /list.json
                HttpGet request_IfLogin = new HttpGet(url_list_json);
                response = client.execute(request_IfLogin);
                resEntity = response.getEntity();
                if (resEntity != null) {
                    responseSTR = EntityUtils.toString(resEntity);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute (Integer result) {
            try {
                jsonObject = new JSONObject(responseSTR);
                if ( jsonObject.getInt("login")==1 ) {
                    // if login already
                    // get last user
                    lastUser = sharedPref.getString("last_user", null);
                    cursor_lastUser = database.rawQuery("SELECT * FROM " + DBHelper.TABLE_USERS + " WHERE " + DBHelper.COLUMN_USERNAME + " = '" + lastUser + "'", null);
                    cursor_lastUser.moveToFirst();
                    String tmp_json_str = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(DBHelper.COLUMN_JSONSTR));
                    ContentValues values = new ContentValues();
                    if (tmp_json_str.compareTo(responseSTR) != 0) {
                        values.put(DBHelper.COLUMN_NEEDREFRESH, 1);
                        values.put(DBHelper.COLUMN_JSONSTR, responseSTR);
                        dbHelper.updateUserTableByUsername(database, lastUser, values);
                    }
                    goto_CheckSongList();
                }
                else {
                    // if haven't login
                    setLoginPage();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setLoginPage(){
        String Notify;

        setContentView(R.layout.activity_login_main);

        textView_notSuccess = (TextView) findViewById(R.id.not_success);
        TextView textView_areYou = (TextView) findViewById(R.id.are_you);
        Button button_resumeLogin = (Button) findViewById(R.id.resume_login);

        if ( seekLastUser() ) {
            Notify = "Are you " + lastUser + " ?\nIf YES, plz resume Login\nIf NO, plz Re-Login";
            textView_areYou.setText(Notify);
        }
        else {
            Notify = "Please Login~\n";
            textView_areYou.setText(Notify);
            button_resumeLogin.setEnabled(false);
        }
    }

    private boolean seekLastUser () {
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
        new BG_Login().execute();
    }

    // for button
    public void login (View view) {
        EditText ET_username = (EditText) findViewById(R.id.username);
        curUser = ET_username.getText().toString();
        EditText ET_password = (EditText) findViewById(R.id.password);
        curPassword = ET_password.getText().toString();

        textView_notSuccess.setText("");

        new BG_Login().execute();
    }

    private class BG_Login extends AsyncTask<String, Integer, Integer>
    {
        List<NameValuePair> form_data = new ArrayList<NameValuePair>();
        HttpResponse response;
        HttpEntity resEntity;
        String responseSTR = "";
        JSONObject jsonObject;

        @Override
        protected Integer doInBackground (String... param) {
            try {

                // request url_login to get csrf
                HttpGet http_login_request = new HttpGet(url_login);
                response = client.execute(http_login_request);
                String csrf = tools.getCSRF(client);
                // prepare login parameter
                form_data.add(new BasicNameValuePair("username", curUser));
                form_data.add(new BasicNameValuePair("password", curPassword));
                form_data.add(new BasicNameValuePair("csrftoken", csrf));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(form_data, HTTP.UTF_8);
                // send post to url_auth
                HttpPost http_auth_request = new HttpPost(url_auth);
                http_auth_request.setEntity(ent);
                response = client.execute(http_auth_request);
                resEntity = response.getEntity();
                // send get to url_list_json to check login and json_string
                HttpGet request_IfLogin = new HttpGet(url_list_json);
                response = client.execute(request_IfLogin);
                resEntity = response.getEntity();
                if (resEntity != null) {
                    responseSTR = EntityUtils.toString(resEntity);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute (Integer result) {
            jsonSTR = responseSTR;
            try {
                jsonObject = new JSONObject(jsonSTR);
                if (jsonObject.getInt("login") == 1) {
                    //record_user
                    recordLastUser();
                    goto_CheckSongList();
                }
                else {
                    // fail login
                    textView_notSuccess.setText("login fail..., plz retry");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void recordLastUser () {
        // write to preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("last_user", curUser);
        editor.commit();

        // new values for column
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_PASSWORD, curPassword);

        cursor_curUser = database.rawQuery("SELECT * FROM " + DBHelper.TABLE_USERS + " WHERE " + DBHelper.COLUMN_USERNAME + " = '" + curUser + "'", null);
        cursor_curUser.moveToFirst();
        if ( cursor_curUser.getCount() > 0 ) {
            // find one
            // see if JSON_string changed
            String tmp_json_str = cursor_curUser.getString(cursor_curUser.getColumnIndex(DBHelper.COLUMN_JSONSTR));
            if (tmp_json_str.compareTo(jsonSTR) == 0) {
                // same JSON str, so no need to refresh Database
                values.put(DBHelper.COLUMN_NEEDREFRESH, 0);
            }
            else {
                values.put(DBHelper.COLUMN_NEEDREFRESH, 1);
                values.put(DBHelper.COLUMN_JSONSTR, jsonSTR);
            }
            dbHelper.updateUserTableByUsername(database, curUser, values);

        }
        else {
            // it's a new user
            values.put(DBHelper.COLUMN_JSONSTR, jsonSTR);
            values.put(DBHelper.COLUMN_USERNAME, curUser);
            values.put(DBHelper.COLUMN_NEEDREFRESH, 1);
            values.put(DBHelper.COLUMN_SONGLISTTABLE, "null");
            database.insert(
                    DBHelper.TABLE_USERS,
                    null,
                    values);
        }
    }

    public void goto_CheckSongList(){
        Intent intent = new Intent(this, CheckSongListActivity.class);

        startActivity(intent);

        finish();
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
