package org.sward.maps.locker;


import org.sward.maps.locker.MyMaps;

import com.google.android.maps.OverlayItem;

import android.os.Bundle;
import android.app.ListActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Places extends ListActivity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
				"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
				"Linux", "OS/2" };
		values=this.getLayers(); //Real Magic
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	}
	
	//Fetch Layers info
	String [] getLayers(){
		String[] ret= new String [MyMaps.itemizedoverlay.mOverlays.size()];
		OverlayItem item=null;
		
		
		for (int i=0;i<MyMaps.itemizedoverlay.mOverlays.size();i++)
		{
			item=MyMaps.itemizedoverlay.mOverlays.get(i);
			MyMaps.itemizedoverlay.pOverlays.get(i);
			ret[i]= "Place : "+item.getSnippet() +" : "+item.getTitle()+ " : Lat -"+item.getPoint().getLatitudeE6()+" : Long -"+item.getPoint().getLongitudeE6();
			
		}
		
		return ret;
	}
}
