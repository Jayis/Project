package com.sagax.player;


import java.util.ArrayList;

import com.sagax.player.R;


import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;

public class PlaylistView extends LinearLayout
{
	private Context mContext;
	private Playlist currentPlaylist;
	private ArrayList<String> collectionList;
	private DragSortListView singleListview;
	private ArrayAdapter<String> adapter;
	private View headerView,headerViewSimple;
	private String mainTitle,sec;
	private int HEADER_SYTLE = 1; // default style = 1
	public static final int HEADER_STYLE_DEFAULT = 1 , HEADER_STYLE_SIMPLE = 2; 
	

	/*
	 * Inner class for control drag and drop sorting mechanism  
	 */
	private DragSortListView.DropListener onDrop =
        new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                String item=adapter.getItem(from);
                adapter.notifyDataSetChanged();
                adapter.remove(item);
                adapter.insert(item, to);
                
                
                // if it is using a play list as content
                if( currentPlaylist != null){
                	Song song = currentPlaylist.get(from);
    				currentPlaylist.remove(song);
    				currentPlaylist.add(to,song);	
                }
				
            }
        };

    private DragSortListView.RemoveListener onRemove = 
        new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                adapter.remove(adapter.getItem(which));

                // if it is using a play list as content
                if( currentPlaylist != null){
                	currentPlaylist.remove( which );	
                }
				
            }
        };

    private DragSortListView.DragScrollProfile ssProfile =
        new DragSortListView.DragScrollProfile() {
            @Override
            public float getSpeed(float w, long t) {
                if (w > 0.8f) {
                    // Traverse all views in a millisecond
                    return ((float) adapter.getCount()) / 0.001f;
                } else {
                    return 10.0f * w;
                }
            }
       
		};

		
		
	private OnItemClickListener itemClickListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> paren, View view , int position , long id ){
		
		}

	
	};

	/*
	 * Constructors
	 */
	public PlaylistView(Context context){
		super(context);
		this.mContext = context;
		this.currentPlaylist = new Playlist();
		initView();
	}
	
	public PlaylistView(Context context,Playlist playlist,String title){
		super(context);
		this.mContext = context;
		this.currentPlaylist = playlist;
		initView();
	}
	
	public PlaylistView(Context context,ArrayList<String> collection,String title){
		super(context);
		this.mContext = context;
		// setListViewContent() depends on currentPlaylist is null or not   
		// to change list view's content object
		this.currentPlaylist = null;
		this.collectionList = collection;
		initView();
	}

	
	// private method for initialize the view 
	// depends on different configuration
	private void initView(){
		/*
		 * initial the drag & sort list view
		 */
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate( R.layout.listviewer , null );
		singleListview = (DragSortListView) view.findViewById( R.id.sortablelist );
		setOrientation( 1 );	
		// setting the list view listener
		singleListview.setDropListener(onDrop);
		singleListview.setRemoveListener(onRemove);
		singleListview.setDragScrollProfile(ssProfile);

		
		// initialize header view
		
		// get default header
		// get headerview from listheaderview.xml
		headerView = inflater.inflate( R.layout.listheaderview , null );	
		((TextView)headerView.findViewById( R.id.mainTitleText )).setText(mainTitle);
		ImageButton editButton = (ImageButton)headerView.findViewById( R.id.editbutton);
		editButton.setOnClickListener( new OnClickListener(){
			// toggle the edit mode
			public void onClick(View v){
				setDragable( !singleListview.isDragEnabled() );
			}
		});
	
		// get simple header
		// get headerview from headerview.xml
		headerViewSimple = inflater.inflate( R.layout.headerview , null );	
		((TextView)headerViewSimple.findViewById( R.id.mainTitleText )).setText(mainTitle);
		headerViewSimple.findViewById( R.id.editbutton).setOnClickListener( new OnClickListener(){
			// toggle the edit mode
			public void onClick(View v){
				setDragable( !singleListview.isDragEnabled() );
			}
		});
		
		setHeaderStyle(HEADER_STYLE_DEFAULT);
		setListViewContent();
		
		addView( headerView );
		addView( headerViewSimple );
		addView( singleListview );

		setBackgroundResource( R.drawable.back );
		
	}

	// this method is used to setup the list view content with adapter
	// because it would have to check if current content object is Song or String
	private void setListViewContent(){
		ArrayList<String> contentTitles = new ArrayList<String>();
		// test if it is using a play list or a collection list as content
		if( currentPlaylist == null){
			// if it's using collection 
			contentTitles = collectionList;
		}else{
			// if it's using a play list
			for( Song song : currentPlaylist.getSongList() ){
				contentTitles.add( song.title );
			}	
		}

		adapter = new ArrayAdapter<String>( mContext , R.layout.list_item_handle_right_trans , R.id.text , contentTitles ); 
		singleListview.setDragEnabled( false );
		singleListview.setAdapter( adapter );
		//singleListview.addFooterView();
				
	}
	
	/*
	 * Part for getter and setter for this class
	 * */
	
	public void setHeaderTitle(String title){
		((TextView)headerView.findViewById( R.id.mainTitleText )).setText(title);
		((TextView)headerViewSimple.findViewById( R.id.mainTitleText )).setText(title);
			
	}
	
	public void setHeaderSecondTitle(String second){
		if( HEADER_SYTLE == HEADER_STYLE_DEFAULT){
			((TextView)headerView.findViewById( R.id.secondTitleText )).setText(second);
		}
	}
	
	public void setHeaderStyle(int HeaderStyle){
		HEADER_SYTLE = HeaderStyle;
		switch( HEADER_SYTLE ){
			case HEADER_STYLE_DEFAULT:
				headerView.setVisibility(View.VISIBLE);
				headerViewSimple.setVisibility(View.GONE);
				break;
			case HEADER_STYLE_SIMPLE:
				headerView.setVisibility(View.GONE);
				headerViewSimple.setVisibility(View.VISIBLE);
				break;
		}
	}

	public void setDragable(boolean dragable){
		ArrayList<String> songTitles = new ArrayList<String>();
		for(int i=0 ;i<adapter.getCount(); i++){
			songTitles.add( adapter.getItem(i) );
		}
		if( dragable ){
			adapter = new ArrayAdapter<String>( mContext , R.layout.list_item_handle_right , R.id.text , songTitles ); 
		}else{
			adapter = new ArrayAdapter<String>( mContext , R.layout.list_item_handle_right_trans , R.id.text , songTitles ); 
		}
		singleListview.setAdapter( adapter );
		singleListview.setDragEnabled( dragable );
	}

	public void setDeleable(boolean deletable){
		if( deletable ){
			singleListview.setRemoveListener( onRemove );
		}else{
			singleListview.setRemoveListener( null );
		}
	}

	// set the ItemClickListener 
	public void setOnItemClickListener(OnItemClickListener listener){
		singleListview.setOnItemClickListener( listener );
	}
	

	public void setHeaderViewVisible(int visible){
		if( visible != View.VISIBLE || visible != View.INVISIBLE || visible != View.GONE ){
			//throw Exception("Please get the right variable from View.VISIBILITY ");
		}
		switch( HEADER_SYTLE ){
			case HEADER_STYLE_DEFAULT:
				headerView.setVisibility( visible );
				break;
			case HEADER_STYLE_SIMPLE:
				headerViewSimple.setVisibility( visible );
				break;
		}
		
	}

	public Playlist getCurrentPlaylist(){
		return this.currentPlaylist;
	}

	public void setCurrentPlaylist(Playlist playlist){
		this.currentPlaylist = playlist;
		setListViewContent();
		
	}
	
	public void setCollectionList(ArrayList<String> collection){
		// setListViewContent() depends on currentPlaylist is null or not   
		// to change list view's content object
		this.currentPlaylist = null;
		this.collectionList = collection;
		setListViewContent();
	}
	
}
