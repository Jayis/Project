package com.sagax.player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.sagax.player.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class MusicListActivityView extends ActivityView {
	private Context mContext;
	private ListView listview;
	private TextView titleTextView;
	private MediaManager mediaManager;
	private MusicManager musicManager;
	private ImageListAdapter imageAdapter;
	private TypeListAdapter typeAdapter;
	private OnItemClickListener itemClickListener;
	private AlertDialog typeDialog;
	private View textlistView,imagelistView;
	
	private final String[] titleStrings = new String[]{ "All" , "Album" , "Singer", "Type" };
	private final int TITLE_ALL = 0 , TITLE_ALBUM = 1 ,TITLE_SINGER = 2 , TITLE_TYPE = 3;
	
	public MusicListActivityView(Activity activity){
		super(activity);
		this.mContext = activity;
	}
	
	

	@Override
	public void display(){
		// setting up the view
		mediaManager = MainActivity.getMediaManagerInstance();
		musicManager = MainActivity.getMusicManagerInstance();
		//activity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		//activity.setContentView(R.layout.activityalbumlist);
		
		
		
		titleTextView = ((TextView)activity.findViewById(R.id.text));
		((TextView)activity.findViewById(R.id.text)).setText(titleStrings[0]);
		
		((TextView)activity.findViewById(R.id.text)).setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				typeDialog.show();
			}
			
		});
	
		setTextView("");
		initlistnameDialog();
		
	}
	
	// set play list dialog view

	private void initlistnameDialog(){
	
		/*
		LayoutInflater li = LayoutInflater.from( mContext );
		View promptsView = li.inflate(android.R.layout., null);
		 */
		
		ListView promptsView = new ListView(mContext);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_expandable_list_item_1,titleStrings);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( mContext );


		// set prompts.xml to alertdialog builder
		promptsView.setAdapter(adapter);
		promptsView.setOnItemClickListener( new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				
				
				titleTextView.setText(titleStrings[position]);
				switch(position){
					case TITLE_ALL:
						setTextView("");
						break;
					case TITLE_SINGER:
						setTextView("Singer");
						break;
					case TITLE_ALBUM:
						setImageView(TITLE_ALBUM);
						break;
					case TITLE_TYPE:
						setImageView(TITLE_TYPE);
						break;
				}
				typeDialog.cancel();
			}
			
		});
		alertDialogBuilder.setView(promptsView);
		//final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

	    
		alertDialogBuilder.setCancelable(false)
	                      .setPositiveButton("OK",new DialogInterface.OnClickListener() {
	                            public void onClick(DialogInterface dialog,int id) {
	                            	dialog.cancel();
	                            }
	                       });
		typeDialog = alertDialogBuilder.create();

	}
	
	private void setImageView(final int TYPE){
		((MainActivity)(activity)).init();
		LayoutInflater inflater = activity.getLayoutInflater();
		imagelistView = inflater.inflate(R.layout.activityalbumlist, null);
		activity.addContentView(imagelistView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		
		
		
		//activity.setContentView(R.layout.activityalbumlist);
		
		listview = (ListView)imagelistView.findViewById(R.id.listview);
		
		
		
		if( TYPE == TITLE_TYPE ){
			typeAdapter = new TypeListAdapter(mContext);
			listview.setAdapter(typeAdapter);
			itemClickListener = new OnItemClickListener(){
				public void onItemClick(AdapterView<?> parent, View view , int position , long id ){
					
					Intent intent = new Intent(mContext, ImagePlaylistActivity.class);
					intent.putExtra("ID",String.valueOf( typeAdapter.getItemId(position) ) );
					intent.putExtra("TYPE", TYPE );
					intent.putExtra("TITLE",typeAdapter.getItemTitle(position));
					intent.putExtra("SUBTITLE",typeAdapter.getItemSubTitle(position));
					mContext.startActivity(intent);
					
				}
			};
		}
		
		if( TYPE == TITLE_ALBUM ){
			imageAdapter = new ImageListAdapter(mContext);
			listview.setAdapter(imageAdapter);
			itemClickListener = new OnItemClickListener(){
				public void onItemClick(AdapterView<?> parent, View view , int position , long id ){
					/*
					Playlist playlist = adapter.getItem(position);
					playlistView.setImagePlaylistView( adapter.getItemTitle(position), adapter.getItemSubTitle(position), playlist);
					playlistView.setVisibility(View.VISIBLE);
					listview.setVisibility(View.GONE);
					*/
					Intent intent = new Intent(mContext, ImagePlaylistActivity.class);
					intent.putExtra("ID",String.valueOf( imageAdapter.getItemId(position) ) );
					intent.putExtra("TYPE", TYPE );
					intent.putExtra("TITLE",imageAdapter.getItemTitle(position));
					intent.putExtra("SUBTITLE",imageAdapter.getItemSubTitle(position));
					mContext.startActivity(intent);
				}
			};
		}
		
		
		
		
		listview.setOnItemClickListener(itemClickListener);
		
	}
	
	
	// setup text list view
	private void setTextView(String catType){
		//activity.setContentView(R.layout.text_list);
		
		((MainActivity)(activity)).init();
		LayoutInflater inflater = activity.getLayoutInflater();
		textlistView = inflater.inflate(R.layout.text_list, null);
		activity.addContentView(textlistView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		
		
		
		ListView lv = (ListView) textlistView.findViewById(R.id.textlistview);
		EditText etSearchbox=(EditText)textlistView.findViewById(R.id.etSearchbox);
		// hide virtual keyboard if we touch other places of the screen
		
		lv.setOnItemClickListener( new OnItemClickListener(){
			@SuppressLint("ShowToast")
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				System.out.println("position:"+position);
				Toast.makeText(mContext, String.valueOf(position), Toast.LENGTH_LONG);
				//musicManager.setCurrentPlaylist(playlist);
			}	
		});
		
		
		etSearchbox.addTextChangedListener(new TextWatcher() {
		    @Override
		    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		        // TODO Auto-generated method stub
		    }
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		            int arg3) {
		        // TODO Auto-generated method stub
		    }
		    @Override
		    public void afterTextChanged(Editable arg0) {
		        // TODO Auto-generated method stub
		    }
		});
	}
	
	private Bitmap getCover(Uri uri){
		ContentResolver res = activity.getContentResolver();
		Bitmap bitmap = null;
		if (uri != null) {
			ParcelFileDescriptor fd = null;
	        try {
	        	fd = res.openFileDescriptor(uri, "r");
	        	bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
	        } catch (FileNotFoundException e) {
	        	return null;
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
	        finally {
	        	try {
	        		if (fd != null)
	        			fd.close();
				} catch (IOException e) {
				}
			}
		}
		return bitmap;
		
	}
	
	
	private class ImageListAdapter extends BaseAdapter{
		
		//private Context mContext;
		private String[] albumID ;
		
		public ImageListAdapter(Context context){
			albumID = mediaManager.getAllAlbumID();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return albumID.length;
		}

		@Override
		public Playlist getItem(int position) {
			ArrayList<Song> songs = mediaManager.getSongsByAlbumID(albumID[position]);
			Playlist playlist = new Playlist(songs);
			return playlist;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.valueOf(albumID[position]);
		}
		
		public String getItemTitle(int position){
			String id = albumID[position];
			return mediaManager.getAlbumNameByID(id);
		}
		
		public String getItemSubTitle(int position){
			String id = albumID[position];
			return mediaManager.getAlbumArtistByID(id);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutInflater vi;
		    vi = LayoutInflater.from( mContext );
		    v = vi.inflate(R.layout.imagelistcell, null);
    
		    final String id = albumID[position];
		    // set song title to list content
			((TextView)v.findViewById(R.id.title)).setText(mediaManager.getAlbumNameByID(id));
			
			if( mediaManager.getAlbumArtistByID(id) == null ){
				((TextView)v.findViewById(R.id.subtitle)).setText("unknown");	
			}else{
				((TextView)v.findViewById(R.id.subtitle)).setText(mediaManager.getAlbumArtistByID(id));
			}
			
			
			// get cover from album
			// if it's bitmap is null 
			// don't change default cover 
			Bitmap cover = getCover( mediaManager.getAlbumArtByID(id) );
			if( cover != null){
				((ImageView)v.findViewById(R.id.cover)).setImageBitmap(cover);
				((ImageView)v.findViewById(R.id.cover)).setScaleType(ImageView.ScaleType.FIT_XY);
				((ImageView)v.findViewById(R.id.cover)).setAdjustViewBounds(true);
				((ImageView)v.findViewById(R.id.cover)).setMaxHeight(190);
				((ImageView)v.findViewById(R.id.cover)).setMaxWidth(185);
			}
			
			((ImageButton)v.findViewById(R.id.play)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					Playlist playlist = new Playlist(mediaManager.getSongsByAlbumID(id));
					musicManager.setCurrentPlaylist(playlist);
					musicManager.playIndex(0);
					MainActivity ma = (MainActivity) mContext;
					ActivityView av = ma.act.get(ma.statusList[1]);
					av.finish();
					av = ma.act.get(ma.statusList[0]);
					ma.init();
					av.display();
					av.setAnimation();

				}
				
			});
			
			return v;
		}
	
	}
	
	
	private class TypeListAdapter extends BaseAdapter{
		
		//private Context mContext;
		private String[] typeID ;
		
		public TypeListAdapter(Context context){
			typeID = mediaManager.getAllTypeID();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return typeID.length;
		}

		@Override
		public Playlist getItem(int position) {
			ArrayList<Song> songs = mediaManager.getSongsByTypeID(typeID[position]);
			Playlist playlist = new Playlist(songs);
			return playlist;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.valueOf(typeID[position]);
		}
		
		public String getItemTitle(int position){
			return mediaManager.getTypeNameByID(typeID[position]);
		}
		
		public String getItemSubTitle(int position){
			return getItem(position).getSongList().size()+"songs";
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutInflater vi;
		    vi = LayoutInflater.from( mContext );
		    v = vi.inflate(R.layout.imagelistcell, null);
    
		    // id of this songlist
		    final String id = typeID[position];
		    // set song title to list content
			((TextView)v.findViewById(R.id.title)).setText(mediaManager.getTypeNameByID(id));
			
			if( mediaManager.getSongsByTypeID(id) == null ){
				((TextView)v.findViewById(R.id.subtitle)).setText("unknown");	
			}else{
				((TextView)v.findViewById(R.id.subtitle)).setText(getItem(position).getSongList().size()+"songs");
			}
			
			((ImageButton)v.findViewById(R.id.play)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					Playlist playlist = new Playlist(mediaManager.getSongsByTypeID(id));
					musicManager.setCurrentPlaylist(playlist);
					musicManager.playIndex(0);
					MainActivity ma = (MainActivity) mContext;
					ActivityView av = ma.act.get(ma.statusList[1]);
					av.finish();
					av = ma.act.get(ma.statusList[0]);
					ma.init();
					av.display();
					av.setAnimation();
				}
				
			});
			
			return v;
		}
	
	}





	@Override
	public void finish() {
		// TODO Auto-generated method stub
		((TextView)activity.findViewById(R.id.text)).setOnClickListener(null);
		((TextView)activity.findViewById(R.id.text)).setText("ArcPlayer");
	}


	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}
