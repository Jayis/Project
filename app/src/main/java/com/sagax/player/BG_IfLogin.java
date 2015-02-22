package com.sagax.player;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by user_2 on 2015/2/22.
 */
public class BG_IfLogin extends AsyncTask<String, Integer, Integer>
{
    private DefaultHttpClient client;
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private SharedPreferences sharedPref;

    private String lastUser;
    private String lastPassword;
    private Cursor cursor_lastUser;

    // network response
    JSONObject jsonObject;
    HttpResponse response;
    HttpEntity resEntity;
    String responseSTR = "";

    @Override
    protected Integer doInBackground (String... params) {
        // static initialization
        client = LoginMainActivity.shareClient();
        database = LoginMainActivity.shareDB();
        dbHelper = LoginMainActivity.shareDBHelper();
        sharedPref = LoginMainActivity.shareSharePref();

        try {
            // get server response from /list.json
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
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
