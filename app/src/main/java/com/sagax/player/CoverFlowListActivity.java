/*
 * Copyright (C) 2010 Neil Davies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This code is base on the Android Gallery widget and was Created
 * by Neil Davies neild001 'at' gmail dot com to be a Coverflow widget
 *
 * @author Neil Davies
 */
package com.sagax.player;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.sagax.player.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentUris;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.net.Uri;

// widget
import android.widget.BaseAdapter;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.EditText;



public class CoverFlowListActivity extends Activity {
	private MediaManager mediaManager;
    private Context coverflowContext;
	private LinearLayout layout ;
	private LinearLayout listViewLayout;
	private int viewWidth;
	private PlayListAdapter playListAdapter; 
	private PlayListAdapter albumListAdapter;

	private ArrayAdapter<String> playlistNameAdapter;

	private ListView listview;
	private ListView listview2;

	private TextView tv2;
    private TextView currentDisplayList;
	private int MODE = 0;
	private final int LIST_ADD = 0 , LIST_DELETE = 1 , LIST_EDIT = 2;

	
	// variable from mediamanager
	private String[] allAlbumIDs;
	private AlertDialog listnameDialog;	
	
    private String currentEditingListName = "" , currentEditingListID = "";

	private void initlistnameDialog(){
		LayoutInflater li = LayoutInflater.from( this );
		View promptsView = li.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( this );

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);
		final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder.setCancelable(false)
                      .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                //playlist = new Playlist();
                                playListAdapter.resetItems( new ArrayList<Song>() );// = new PlayListAdapter( coverflowContext );
                                listview2.setAdapter( playListAdapter );
                                String listname = userInput.getText().toString();
                                if( listname.isEmpty() ){
                                    listname = "My playlist";
                                }
                                currentEditingListID = String.valueOf( mediaManager.createPlaylist( listname ) );
                                currentEditingListName = listname;
                                currentDisplayList.setText(listname );
                                currentDisplayList.setVisibility( View.VISIBLE );
                                Toast.makeText( coverflowContext ,"Touch to add song.", Toast.LENGTH_SHORT).show();
                                
                                MODE = LIST_ADD;

                            }
                       })
                      .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                        }
                        });
        listnameDialog = alertDialogBuilder.create();

	}


	// handler for transition between coverflow and textview of album title and song list.
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int position = Integer.parseInt( (String)msg.obj );
			String albumKey = allAlbumIDs[ position ];
			tv2.setText( mediaManager.getAlbumNameByID( albumKey ) );
			albumListAdapter.resetItems( mediaManager.getSongsByAlbumID(albumKey) );
       	}
    };

	private OnItemClickListener albumListListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent,View view,int position,long id){
			if( MODE == LIST_ADD ){
                playListAdapter.addItem( albumListAdapter.getItem( position ) );
                mediaManager.setPlaylist( playListAdapter.getList() , currentEditingListID );
                /*
				if( playlist != null ){
					playlist.add( albumListAdapter.getItem( position ) );

				}
                 */
			}
		}
	
	};

	private OnItemClickListener playListListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent,View view,int position,long id){
			if( MODE == LIST_DELETE ){
				String[] playlistIDs = mediaManager.getAllPlaylistID();
                String listname = mediaManager.getPlaylistNameByID( playlistIDs[position]);
				mediaManager.removePlaylistById( playlistIDs[ position ] );
				playlistNameAdapter.remove( playlistNameAdapter.getItem( position ));
                Toast.makeText( coverflowContext , "List:"+listname+" has been removed." , Toast.LENGTH_SHORT).show();

			}else if( MODE == LIST_ADD ){
				//playlist.remove( position );
				playListAdapter.removeItemAt( position );
                mediaManager.setPlaylist( playListAdapter.getList() , currentEditingListID );

            }else if( MODE == LIST_EDIT){

                String[] playlistIDs = mediaManager.getAllPlaylistID();
                currentEditingListID = playlistIDs[position] ;
                ArrayList<Song> list = mediaManager.getPlaylistByID( playlistIDs[ position ] );

                playListAdapter.resetItems( list );

                currentDisplayList.setText( mediaManager.getPlaylistNameByID( playlistIDs[position] ));
                currentDisplayList.setVisibility( View.VISIBLE );
                listview2.setAdapter( playListAdapter );

                MODE = LIST_ADD;


            }
		}
	
	};



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mediaManager = new MediaManager( this );
        coverflowContext = this;
		allAlbumIDs = mediaManager.getAllAlbumID();
		int DEFAULT_ALBUM_POSITION = allAlbumIDs.length/2;

		layout = new LinearLayout( this );
		listViewLayout = new LinearLayout( this );
		//listViewLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		//layout = (LinearLayout) findViewById( R.id.coverflowlayout );
		layout.setOrientation( 1 );
		layout.setBackgroundResource( R.drawable.back );


		// layout animation
		

		tv2 = new TextView(this);
		tv2.setGravity( Gravity.CENTER ) ;
		tv2.setTextColor( getResources().getColor( android.R.color.white ) );
		tv2.setText( mediaManager.getAlbumNameByID( allAlbumIDs[DEFAULT_ALBUM_POSITION] ) );

        currentDisplayList = new TextView( this );
        currentDisplayList.setGravity( Gravity.CENTER );
        currentDisplayList.setTextColor( getResources().getColor( android.R.color.white));
        currentDisplayList.setTextSize( 25.0f );
        currentDisplayList.setVisibility( View.GONE );

        CoverFlow coverFlow = new CoverFlow(this);
		coverFlow.setAdapter(new ImageAdapter(this,mHandler));



        coverFlow.setSpacing(-25);
        coverFlow.setSelection(DEFAULT_ALBUM_POSITION, true);
        coverFlow.setAnimationDuration(1000);


		coverFlow.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
				mHandler.obtainMessage(0, String.valueOf(position)).sendToTarget();
			}

			public void onNothingSelected(AdapterView<?> parent){}
		
		});

		listview = new ListView(this);
		listview2 = new ListView(this);

			
		albumListAdapter = new PlayListAdapter(this,mediaManager.getSongsByAlbumID( allAlbumIDs[ DEFAULT_ALBUM_POSITION] ));
		listview.setAdapter( albumListAdapter );



        // init playlistadapter , all other places use reserItems instead of new one
        playListAdapter = new PlayListAdapter(this);
		//listview2.setAdapter( playListAdapter );



		initPlaylistNameAdapter();
		listview2.setAdapter( playlistNameAdapter );
		MODE = LIST_EDIT;

		RayMenu rmenu = new RayMenu( getApplicationContext() );
		initRayMenu( rmenu );


		layout.addView( coverFlow );
		layout.addView( tv2 );
		layout.addView( rmenu );
        layout.addView( currentDisplayList );
		layout.addView( listViewLayout );
	
		
		
 		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		int width= wm.getDefaultDisplay().getWidth();

		listview.setLayoutParams(new LayoutParams(width/2,LayoutParams.MATCH_PARENT));
		listview2.setLayoutParams(new LayoutParams(width/2,LayoutParams.MATCH_PARENT));
		
		listview.setOnItemClickListener( albumListListener );
		listview2.setOnItemClickListener( playListListener );

		listview.setDividerHeight( 2 );

		listViewLayout.addView( listview );
		listViewLayout.addView( listview2 );

		
		
		// listview
		
		setContentView( layout );
		
        initlistnameDialog();

		
    }
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}

	private void initPlaylistNameAdapter(){
		String[] playlistID = mediaManager.getAllPlaylistID();

		ArrayList<String> playlistName = new ArrayList<String>();
		for( String id : playlistID ){
			playlistName.add( mediaManager.getPlaylistNameByID( id ) ); 
		}
		playlistNameAdapter = new ArrayAdapter<String>( this, R.layout.regular_list2 , playlistName );
	}


	private void initRayMenu(RayMenu menu){
		int [] DrawableRes = { R.drawable.composer_music , R.drawable.composer_thought , R.drawable.composer_with };
		for ( int i = 0 ; i< DrawableRes.length ; i++ ) {
			ImageView item = new ImageView(this);
			item.setImageResource( DrawableRes[i] );
			final int position = i ;	
			menu.addItem( item , new OnClickListener(){
				@Override
				public void onClick(View v){
                    currentDisplayList.setVisibility( View.GONE );
					switch(position){
						case 0:
                            listnameDialog.show();
							MODE = LIST_ADD;
							break;
						case 1:
                            Toast.makeText( getApplicationContext() , "Touch to edit a list" , Toast.LENGTH_SHORT).show();
                            initPlaylistNameAdapter();
                            listview2.setAdapter( playlistNameAdapter );
                            MODE = LIST_EDIT;
							break;
						case 2:
                            Toast.makeText( getApplicationContext() , "Touch to delete a list" , Toast.LENGTH_SHORT).show();
                            initPlaylistNameAdapter();
                            listview2.setAdapter( playlistNameAdapter );
                            MODE = LIST_DELETE;

							//playlistView.setPlaylist(playlist);
							//playlistView.setVisibility(View.VISIBLE);
							break;
					
					}
				}
			
			});

		}//end for
	}


    /*
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // Show home screen when pressing "back" button,
            //  so that this app won't be closed accidentally
        	if(playlistView.getVisibility() == View.VISIBLE){
        		playlistView.setVisibility(View.GONE);
        		
        		// restore playlist if uses had any changes
        		playlist = playlistView.getCurrentPlaylist();
        		playListAdapter.resetItems(playlist.getSongList());
        	}else{
        		finish();
        	}
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
     */

	public class PlayListAdapter extends BaseAdapter{
		private ArrayList<Song> playList;
		private Context mContext;

		public PlayListAdapter(Context c){
			mContext = c;
			playList = new ArrayList<Song>();
		}

		public PlayListAdapter(Context c,ArrayList<Song> playList){
			mContext = c;
			this.playList = playList;
		}

		public PlayListAdapter(Context c,Song[] playList){
			this(c);
			for( Song song : playList ){
				this.playList.add( song );
			}
		}

		public void addItem(Song song){
			playList.add(song);
			this.notifyDataSetChanged();
		}
		
		public void addItemAt(Song song,int position){
			playList.add(position,song);
			this.notifyDataSetChanged();
		}

		public void removeItem(Song song){
			playList.remove(song);
			this.notifyDataSetChanged();
		}

		public void removeItemAt(int position){
			playList.remove(position);
			this.notifyDataSetChanged();
		}

		public Song getItem(int position){
			return playList.get(position);
		}

		public int getCount(){
			return playList.size();
		}

		public long getItemId(int position){
			return position;
		}
        
        public ArrayList<Song> getList(){
            return this.playList;
        }
		
		public void resetItems(Song[] songs){
			playList.clear();
			for( int i=0; i< songs.length ; i++){
				playList.add(songs[i]);
			}
			this.notifyDataSetChanged();
		}

		public void resetItems(ArrayList<Song> playList){
			this.playList = playList;
			this.notifyDataSetChanged();
		}

		public View getView(int position,View view, ViewGroup parent){

			LayoutInflater layoutInflater = ( LayoutInflater )mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = layoutInflater.inflate( R.layout.regular_list , null );
			TextView textview = ( TextView ) view.findViewById( R.id.songtitle );
			
			textview.setText( playList.get(position).title );
			textview.setTextColor( mContext.getResources().getColor( android.R.color.white) );


			return view;	
		}

	}


    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;
        private FileInputStream fis;
		private Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		private Handler mHandler;
		private String[] albumList;


	
        private ImageView[] mImages;
        
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public ImageAdapter(Context c,Handler handler) {
            mContext = c;
			mHandler = handler;
			albumList = mediaManager.getAllAlbumID();
        }

        public boolean createReflectedImages() {
			
			/*

            //The gap we want between the reflection and the original image
            final int reflectionGap = 4;
            
            
            int index = 0;
            for (int imageId : mImageIds) {
                Bitmap originalImage = BitmapFactory.decodeResource(getResources(),
                                                                    imageId);
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                
                
                //This will not scale but will flip on the Y axis
                Matrix matrix = new Matrix();
                matrix.preScale(1, -1);
                
                //Create a Bitmap with the flip matrix applied to it.
                //We only want the bottom half of the image
                Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);
                
                
                //Create a new bitmap with same width but taller to fit reflection
                Bitmap bitmapWithReflection = Bitmap.createBitmap(width
                                                                  , (height + height/2), Config.ARGB_8888);
                
                //Create a new Canvas with the bitmap that's big enough for
                //the image plus gap plus reflection
                Canvas canvas = new Canvas(bitmapWithReflection);
                //Draw in the original image
                canvas.drawBitmap(originalImage, 0, 0, null);
                //Draw in the gap
                Paint deafaultPaint = new Paint();
                canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
                //Draw in the reflection
                canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);
                
                //Create a shader that is a linear gradient that covers the reflection
                Paint paint = new Paint();
                LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0,
                                                           bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
                                                           TileMode.CLAMP);
                //Set the paint to use this shader (linear gradient)
                paint.setShader(shader);
                //Set the Transfer mode to be porter duff and destination in
                paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
                //Draw a rectangle using the paint with our linear gradient
                canvas.drawRect(0, height, width,
                                bitmapWithReflection.getHeight() + reflectionGap, paint);
                
                ImageView imageView = new ImageView(mContext);
                imageView.setImageBitmap(bitmapWithReflection);
                imageView.setLayoutParams(new CoverFlow.LayoutParams(120, 180));
                imageView.setScaleType(ScaleType.MATRIX);
                mImages[index++] = imageView;
                
            }
			*/
            return true;
        }
        
        public int getCount() {
            return albumList.length;
        }
        
        public Object getItem(int position) {
            return position;
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        
			//Use this code if you want to load from resources
            ImageView i = new ImageView(mContext);
			ParcelFileDescriptor fd = null;
			try{
				// get the album cover from media resources
				Uri albumPath = mediaManager.getAlbumArtByID( albumList[ position ] );
				fd = mContext.getContentResolver().openFileDescriptor(albumPath, "r");
				i.setImageURI( albumPath );		


			}catch(FileNotFoundException e){
            	i.setImageResource(R.raw.unknown);
			}
			try{
				if( fd!= null)
					fd.close();
			}catch(IOException e){
			
			}


            i.setLayoutParams(new CoverFlow.LayoutParams(200, 200));
            i.setScaleType(ImageView.ScaleType.CENTER_INSIDE); 
            
            //Make sure we set anti-aliasing otherwise we get jaggies
            BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
            drawable.setAntiAlias(true);


            return i;
        }
        /** Returns the size (0.0f to 1.0f) of the views 
         * depending on the 'offset' to the center. */ 
        public float getScale(boolean focused, int offset) { 
            /* Formula: 1 / (2 ^ offset) */ 
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
        } 
        
    }
}
