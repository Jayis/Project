package com.sagax.player;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

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

/**
 * Created by user_2 on 2015/2/22.
 */
public class BG_Login extends AsyncTask<String, Integer, Integer>
{
    private DefaultHttpClient client;
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private SharedPreferences sharedPref;

    List<NameValuePair> form_data = new ArrayList<NameValuePair>();
    HttpResponse response;
    HttpEntity resEntity;
    String responseSTR = "";
    JSONObject jsonObject;

    public Cursor cursor_curUser;
    public String jsonSTR;

    @Override
    protected Integer doInBackground (String... param) {
        // static initialization
        client = LoginMainActivity.shareClient();
        database = LoginMainActivity.shareDB();
        dbHelper = LoginMainActivity.shareDBHelper();
        sharedPref = LoginMainActivity.shareSharePref();

        try {

            // request url_login to get csrf
            HttpGet http_login_request = new HttpGet(LoginMainActivity.url_login);
            response = client.execute(http_login_request);
            String csrf = Tools.getCSRF(client);
            // prepare login parameter
            form_data.add(new BasicNameValuePair("username", LoginMainActivity.curUser));
            form_data.add(new BasicNameValuePair("password", LoginMainActivity.curPassword));
            form_data.add(new BasicNameValuePair("csrftoken", csrf));
            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(form_data, HTTP.UTF_8);
            // send post to url_auth
            HttpPost http_auth_request = new HttpPost(LoginMainActivity.url_auth);
            http_auth_request.setEntity(ent);
            response = client.execute(http_auth_request);
            resEntity = response.getEntity();
            // send get to url_list_json to check login and json_string
            HttpGet request_IfLogin = new HttpGet(LoginMainActivity.url_list_json);
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
                recordLastUser();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void recordLastUser () {
        // write to preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("last_user",LoginMainActivity.curUser);
        editor.commit();

        // new values for column
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_PASSWORD, LoginMainActivity.curPassword);

        cursor_curUser = database.rawQuery("SELECT * FROM " + DBHelper.TABLE_USERS + " WHERE " + DBHelper.COLUMN_USERNAME + " = '" + LoginMainActivity.curUser + "'", null);
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
            dbHelper.updateUserTableByUsername(database, LoginMainActivity.curUser, values);

        }
        else {
            // it's a new user
            values.put(DBHelper.COLUMN_JSONSTR, jsonSTR);
            values.put(DBHelper.COLUMN_USERNAME, LoginMainActivity.curUser);
            values.put(DBHelper.COLUMN_NEEDREFRESH, 1);
            values.put(DBHelper.COLUMN_SONGLISTTABLE, "null");
            database.insert(
                    DBHelper.TABLE_USERS,
                    null,
                    values);
        }
    }
}
