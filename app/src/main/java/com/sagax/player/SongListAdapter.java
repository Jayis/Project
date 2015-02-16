package com.sagax.player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sagax.player.R;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SongListAdapter extends BaseAdapter{
	private ArrayList<Song> songs;
	private Context mContext;
	private String currentIdString;

    private DownloadManager dm;
    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private SharedPreferences sharedPref;
    private String songTable;
    private List<Map<String,Object>> QedSongList;
	
	public SongListAdapter(Context context,ArrayList<Song> playlist, Song current) {
		mContext = context;
		songs = playlist;
		if(current != null)
			currentIdString = current.id;
        else
        currentIdString = "null";

        dbHelper = LoginMainActivity.shareDBHelper();
        database = LoginMainActivity.shareDB();
        sharedPref = LoginMainActivity.shareSharePref();
        dm = MainActivity.shareDM();
        QedSongList = MainActivity.shareDLQ();
        songTable = sharedPref.getString("songTable", null);
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View v = convertView;
		// init the view
		if( v == null){
			LayoutInflater vi;
	        vi = LayoutInflater.from( mContext);
	        v = vi.inflate(R.layout.songlist, parent, false);
		}  
	        // set song title to list content
		TextView tmp = ((TextView)v.findViewById(R.id.songtitle));
		//TextView songLength = ((TextView)v.findViewById(R.id.songlength));
		tmp.setText(songs.get(position).title);
		tmp.setTextColor(Color.rgb(255, 235, 0));
		//songLength.setText(songs.get(position).gtDuration());
	    //songLength.setTextColor(Color.rgb(255, 235, 0));
		if(!currentIdString.equals(songs.get(position).id)){
			((ImageView)v.findViewById(R.id.current)).setVisibility(ViewGroup.INVISIBLE);
			tmp.setTextColor(Color.rgb(255, 255, 255));
			//songLength.setTextColor(Color.rgb(255, 255, 255));
		}

        // Harry

        Button DL = (Button) v.findViewById(R.id.button_DL);
        if (songs.get(position).status == 2) {
            DL.setText("in local");
            DL.setEnabled(false);
        }
        else if (songs.get(position).status == 1) {
            DL.setText("Downloading");
            DL.setEnabled(false);
        }
        else{
            DL.setText("Download");
            DL.setEnabled(true);
            DL.setOnClickListener(new ItemButton_Click(mContext, position));
        }


		return v;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return songs.size();
	}

	@Override
	public Song getItem(int position) {
		// TODO Auto-generated method stub
		return songs.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

    class ItemButton_Click implements View.OnClickListener {
        private int position;
        private Button this_button;
        private Context mContext;

        ItemButton_Click(Context context, int pos) {
            this.position = pos;
            this.mContext = context;
        }

        public void onClick(View v) {
            //-------FUTURE WORK-------

            songs.get(position).status = 1;

            this_button = (Button) v.findViewById(R.id.button_DL);
            this_button.setEnabled(false);
            this_button.setText("DownLoading...");

            String DownloadURL = "http://106.187.36.145:3000" + songs.get(position).url;

            if (dm == null) Log.d("dm", "nulllll");
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadURL));
            if (request == null) Log.d("reqeust", "nulllll");
            File destination = mContext.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (destination == null) Log.d("destination", "nulllll");
            File file = new File(destination, songs.get(position).filename);
            Log.d("DLurl", songs.get(position).url);
            Log.d("filename", songs.get(position).filename);
            request.setDestinationUri(Uri.parse(file.toURI().toString()));
            if (Uri.parse(file.toURI().toString()) == null) Log.d("uri", "nulllll");
            long QedSong = dm.enqueue(request);

            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_LOCALURI, file.getAbsolutePath());
            dbHelper.updateSongTableByServerID(database, songTable, Integer.valueOf(songs.get(position).id), values );



            Map<String, Object> item = new HashMap<String, Object>();
            item.put("queryID", QedSong);
            item.put("songID", songs.get(position).id);
            item.put("DL_butt", this_button);
            item.put("localurl", file.getAbsolutePath());

            QedSongList.add(item);

        }
    }


}