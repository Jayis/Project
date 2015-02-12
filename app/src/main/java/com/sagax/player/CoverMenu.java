package com.sagax.player;

import java.util.ArrayList;
import java.util.List;

import com.sagax.player.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class CoverMenu extends Activity {
private Cursor audioCursor;
public String artistInput;
private static final String TAG = "coverMenu";
private List<String> songs = new ArrayList<String>();


/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_cover_menu);

// Set to some artist you know


String[] projection = {
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Albums.ARTIST,
        MediaStore.Audio.Albums.ALBUM_ART,       
};


audioCursor = getContentResolver().query(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection,
        null, null,
        MediaStore.Audio.Albums.ALBUM + " ASC");


while(audioCursor.moveToNext()){
		String gg = audioCursor.getString(0)+"|"+audioCursor.getString(1)+"|"+audioCursor.getString(2)+"|"+audioCursor.getString(3);
		songs.add(gg);
}


String[] from = new String[] { MediaStore.Audio.Albums.ALBUM_ART };

int[] to = new int[] { android.R.id.text1 };

MySimpleListAdapter mAdapter = new MySimpleListAdapter(this,
        android.R.layout.simple_list_item_1, audioCursor, from, to);
GridView gridview = (GridView) findViewById(R.id.gridview);
gridview.setAdapter(mAdapter);

gridview.setOnItemClickListener(new OnItemClickListener() {
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Toast toast =Toast.makeText(getApplicationContext(), "" + position, Toast.LENGTH_SHORT);
        toast.show();
    }
});
}}
