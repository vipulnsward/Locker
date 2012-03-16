package org.sward.maps.locker;

import java.util.*;
import android.location.*;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.*;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.util.Log;
import android.view.*;

public class MyMaps extends MapActivity {

	private MapController mapController;
//	private MapView mapView;
	private LocationManager locationManager;
	private MyCustomMapView mapView;
	static public HelloItemizedOverlay itemizedoverlay;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main); // bind the layout to the activity
		
		// create a map view
		RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.mainlayout);
		mapView = (MyCustomMapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(true);
		
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoom 1 is world view
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());
		
		//Sets the long click listener
		mapView.setOnLongpressListener(new MyCustomMapView.OnLongpressListener() {
	        public void onLongpress(final MapView view, final GeoPoint longpressLocation) {
	            runOnUiThread(new Runnable() {
	            public void run() {
	                // Insert your longpress action here
	            	recieveLongClick(longpressLocation);
	            }
	        });
	        }
	    });
		
		//End Long CLick Listener
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.marker2);
		itemizedoverlay = new HelloItemizedOverlay(drawable,getApplicationContext());
		
		GeoPoint point = new GeoPoint(19240000,-99120000);
		OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
		
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);
		
		GeoPoint point2 = new GeoPoint(35410000, 139460000);
		OverlayItem overlayitem2 = new OverlayItem(point2, "Sekai, konichiwa!", "I'm in Japan!");
		itemizedoverlay.addOverlay(overlayitem2);
		
		//Fetch All entries here from DB and set as GeoPoint
		saver();
		
		
	}
	
	public void saver(){
		  SQLiteDatabase sampleDB = null;
	        
	        try {
	        
	        sampleDB =  this.openOrCreateDatabase("Locker", MODE_PRIVATE, null);
			
	        //filepath, snippet, title, depth, ainfo, lat, long
	        
			Cursor c = sampleDB.rawQuery("SELECT * FROM " +
	    			" imageinfo", null);

			
	    	if (c != null ) {
	    		if  (c.moveToFirst()) {
	    			  			
	    			do {
	    				//String firstName = c.getString(c.getColumnIndex("FirstName"));
	    				//int age = c.getInt(c.getColumnIndex("Age"));
	    				//results.add("" + firstName + ",Age: " + age);
	    				
	    				String title=c.getString(c.getColumnIndex("Title"));
		    			String snippet=c.getString(c.getColumnIndex("Snippet"));
		    			String depth=c.getString(c.getColumnIndex("Depth"));
		    			String ainfo=c.getString(c.getColumnIndex("AInfo"));
		    			String filepath=c.getString(c.getColumnIndex("FilePath"));
		    			String lat=c.getString(c.getColumnIndex("Lat"));
		    			String longitude=c.getString(c.getColumnIndex("Longitude"));
		    			
		    			//Wah!, consume them now
		    			
		    			GeoPoint point2 = new GeoPoint(Integer.parseInt(lat), Integer.parseInt(longitude));
		    			OverlayItem overlayitem2 = new OverlayItem(point2, title, snippet);
		    			itemizedoverlay.addOverlay(overlayitem2, filepath);
		    			
		    			
	    			}while (c.moveToNext());
	    		} 
	    	}
			
	        }catch(Exception e){
	        	e.printStackTrace();
	        }finally{
	        	sampleDB.close();
	        }
		
	}
	
	//save data from adroid app on close

	@Override
	protected void onPause(){
		super.onPause();
		//Save data here for persistance
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Toast.makeText(this, "Just a test", Toast.LENGTH_SHORT).show();
		Intent i=new Intent(this,Places.class);
		startActivity(i);
		return true;
	}
	
	

	//This processes Long Click
	public void recieveLongClick(GeoPoint geoPoint)
	{	    
	    // You can now pull lat/lng from geoPoint
	    int latitude = geoPoint.getLatitudeE6();
	    int longitude = geoPoint.getLongitudeE6();
	    Toast.makeText(this,"Lat:"+latitude+"Lon"+longitude,Toast.LENGTH_LONG).show();
	   
		String addressString="";
		Geocoder gc = new Geocoder(this, Locale.getDefault());

        try {

            List<Address> addresses = gc.getFromLocation(latitude/1E6, longitude/1E6, 1);
            StringBuilder sb = new StringBuilder();

            if (addresses.size() > 0) {
                Address address = addresses.get(0);

                for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                    sb.append(address.getAddressLine(i)).append("\n");

                sb.append(address.getCountryName());
            }
            addressString = sb.toString();
        } catch (java.io.IOException e) {
        	Log.d("GeoCoder", ""+e);
        	}
        
        OverlayItem overlayitem2 = new OverlayItem(geoPoint, "Sekai, konichiwa!", addressString);
		itemizedoverlay.addOverlay(overlayitem2);
		
		
		
		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); //	mapController.setCenter(point);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
}
	