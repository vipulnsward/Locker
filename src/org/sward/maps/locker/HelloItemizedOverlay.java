package org.sward.maps.locker;

import android.content.Context;
import android.content.Intent;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.*;
import java.util.*;
import android.graphics.drawable.*;
import android.widget.*;

public class HelloItemizedOverlay extends ItemizedOverlay{
	private int IMG_COUNT=1;	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private ArrayList<Integer> cOverlays = new ArrayList<Integer>();
	public ArrayList<String> pOverlays = new ArrayList<String>();
	private Context mContext;
	
	public HelloItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		}
	
	public HelloItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
		}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    cOverlays.add(new Integer(IMG_COUNT));
	    pOverlays.add("");
	    IMG_COUNT++;
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  String path=pOverlays.get(index);
	  
	  android.util.Log.d("MAP",""+item.getTitle()+":"+item.getSnippet());
	  Toast.makeText(mContext, "You Selected :"+item.getTitle()+":"+item.getSnippet(), Toast.LENGTH_SHORT).show();
	  Intent i = new Intent(mContext, ImageActivity.class);
		i.putExtra("title", item.getTitle());
		i.putExtra("snippet", item.getSnippet());
		i.putExtra("path", path);
		i.putExtra("index", ""+index);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		// Set the request code to any code you like, you can identify the
		// callback via this code
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// To allow running from a non activity
		mContext.startActivity(i);
	
	  return true;
	}
	
}
