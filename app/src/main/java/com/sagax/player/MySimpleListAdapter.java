package com.sagax.player;

import java.io.File;

import com.sagax.player.R;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MySimpleListAdapter extends SimpleCursorAdapter {

private Context mContext;

public MySimpleListAdapter(Context context, int layout, Cursor c,
        String[] from, int[] to) {
    super(context, layout, c, from, to);
    mContext = context;
}

public View getView(int position, View convertView, ViewGroup parent) {
    View v;
    ImageView iv;
    TextView tv;
    if (convertView != null)
        v = convertView;
    else {
        LayoutInflater layout = LayoutInflater.from(mContext);
        v = layout.inflate(R.layout.custom_grid_item, null);
    }

    this.getCursor().moveToPosition(position);

    iv = (ImageView) v.findViewById(R.id.album_image);
    tv = (TextView) v.findViewById(R.id.album_title);
    
    File file=null;
    try{
    	file= new File(this.getCursor().getString(3));
    }catch (Exception e){}
    if(file == null)
    	iv.setImageResource(R.raw.unknown);
    else
    	iv.setImageURI(Uri.fromFile(file));
    
    tv.setText(this.getCursor().getString(1));

    iv.setScaleType(ScaleType.FIT_CENTER);
    return v;

}}
