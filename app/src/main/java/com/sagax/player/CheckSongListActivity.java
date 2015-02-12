package com.sagax.player;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;


public class CheckSongListActivity extends Activity {

    // share static
    private DefaultHttpClient client;
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private SharedPreferences sharedPref;

    //
    private String lastUser;
    private String private_SongList;
    private Cursor cursor_lastUser;
    private JSONArray jsonArray;

    //
    public final static String EXTRA_SONGTABLENAME = "com.jayis4176.mobile_client.songTableName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // static initialization
        client = LoginMainActivity.shareClient();
        database = LoginMainActivity.shareDB();
        dbHelper = LoginMainActivity.shareDBHelper();
        sharedPref = LoginMainActivity.shareSharePref();

        // initialization
        lastUser = sharedPref.getString("last_user", null);

        //
        new BG_CheckSongList().execute();
    }

    private class BG_CheckSongList extends AsyncTask<String, Integer, Integer>
    {
        int needRefresh;
        JSONObject jsonObject;

        @Override
        protected Integer doInBackground (String... params) {
            cursor_lastUser = database.rawQuery("SELECT * FROM " + DBHelper.TABLE_USERS + " WHERE " + DBHelper.COLUMN_USERNAME + " = '" + lastUser + "'", null);
            cursor_lastUser.moveToFirst();
            needRefresh = cursor_lastUser.getInt(cursor_lastUser.getColumnIndex(DBHelper.COLUMN_NEEDREFRESH));
            if (needRefresh == 1)  {
                // need to refresh
                ContentValues values = new ContentValues();
                if (cursor_lastUser.getString(cursor_lastUser.getColumnIndex(DBHelper.COLUMN_SONGLISTTABLE)).compareTo("null") == 0) {
                    // also need to create table
                    private_SongList = lastUser + "_SongList";
                    dbHelper.createPrivateTable(database, private_SongList);
                    // update table name (SongList)
                    values.put(DBHelper.COLUMN_SONGLISTTABLE, private_SongList);
                }
                else {
                    // use old table name
                    private_SongList = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(DBHelper.COLUMN_SONGLISTTABLE));
                }

                // parse in JSONObject
                try {
                    jsonObject = new JSONObject(cursor_lastUser.getString(cursor_lastUser.getColumnIndex(DBHelper.COLUMN_JSONSTR)));
                    jsonArray = jsonObject.getJSONArray("file_list");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                // start refreshing SongList
                refreshSongList();

                values.put(DBHelper.COLUMN_NEEDREFRESH, 0);
                dbHelper.updateUserTableByUsername(database, lastUser, values);
            }
            else {
                private_SongList = cursor_lastUser.getString(cursor_lastUser.getColumnIndex(DBHelper.COLUMN_SONGLISTTABLE));
            }

            return null;
        }

        @Override
        protected void onPostExecute (Integer result) {
            goto_PlayerMain();
        }
    }

    private void refreshSongList () {
        Cursor cursor_curSong;
        JSONObject curSong;
        Cursor cursor_offListSongs;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                curSong = jsonArray.getJSONObject(i);
                cursor_curSong = database.rawQuery("SELECT * FROM " + private_SongList + " WHERE " + DBHelper.COLUMN_SERVERID + " = " + curSong.getInt("server_id"), null);
                cursor_curSong.moveToFirst();
                ContentValues values = new ContentValues();
                // -- attribute on json.list
                values.put(DBHelper.COLUMN_ALBUM, dbHelper.emptyStringChecker(curSong.getString("album")));
                values.put(DBHelper.COLUMN_GENRE, curSong.getString("genre"));
                values.put(DBHelper.COLUMN_SERVERID, curSong.getInt("server_id"));
                values.put(DBHelper.COLUMN_TITLE, dbHelper.emptyStringChecker(curSong.getString("title")));
                values.put(DBHelper.COLUMN_URL, curSong.getString("url"));
                values.put(DBHelper.COLUMN_ARTIST, dbHelper.emptyStringChecker(curSong.getString("artist")));
                values.put(DBHelper.COLUMN_UPLOADDATE, curSong.getString("upload_date"));
                values.put(DBHelper.COLUMN_FILENAME, curSong.getString("filename"));
                // -- attribute add by myself
                values.put(DBHelper.COLUMN_ONLIST, 1);            // to see if this song is still on the list this time

                if (cursor_curSong.getCount() <= 0) {
                    values.put(DBHelper.COLUMN_LOCALURI, "null");    // "null" means haven't download yet
                    values.put(DBHelper.COLUMN_EQON, 0);
                    // not found on local SongList
                    database.insert(
                            private_SongList,
                            null,
                            values);
                }
                else {
                    //else if (curSong.getString("upload_date").compareTo(cursor_curSong.getString(cursor_lastUser.getColumnIndex(MySQLiteHelper.COLUMN_UPLOADDATE))) != 0) {
                    // the song is not the same
                    // update
                    dbHelper.updateSongTableByServerID(database, private_SongList, curSong.getInt("server_id"), values);
                    // ---------FUTURE WORK----------
                    // delete the old file, if old localURI is not null
                }
                /**/
            }
            // delete files of songs not on list
            cursor_offListSongs = database.rawQuery("SELECT * FROM " + private_SongList + " WHERE " + DBHelper.COLUMN_ONLIST  + " = 0", null);
            if (cursor_offListSongs.getCount() > 0) {
                cursor_offListSongs.moveToFirst();

                for (int i = 0; i < cursor_offListSongs.getCount(); i++) {
                    File curFile = new File(cursor_offListSongs.getString(cursor_offListSongs.getColumnIndex(DBHelper.COLUMN_LOCALURI)));
                    curFile.delete();
                    cursor_offListSongs.moveToNext();
                }
            }
            // delete those are not on the list
            String selection = DBHelper.COLUMN_ONLIST + " LIKE ?";
            String[] selectionArgs = { String.valueOf(0) };
            database.delete(private_SongList, selection, selectionArgs);

            // re-assign 0 to ONLIST of songs in list
            database.execSQL("UPDATE " + private_SongList + " SET " + DBHelper.COLUMN_ONLIST + " = 0");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goto_PlayerMain(){
        Intent intent = new Intent(this, MainActivity.class);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("songTable", private_SongList);
        editor.commit();

        startActivity(intent);

        finish();
    }
}
