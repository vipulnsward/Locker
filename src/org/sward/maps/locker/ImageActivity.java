package org.sward.maps.locker;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.*;

import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageActivity extends Activity {
	
	private HttpPost post; 
	private Uri mImageCaptureUri;
	private ImageView mImageView;	
	public String path		= "";
	public int index=0;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;
	private static final String TAG = "IMAGE";
	private static final String FILENAME_STR = "FILE";
	private Bitmap bitmap 	= null;
	
	private EditText text1;
	private EditText text2;
	private EditText text3;
	private EditText text4;
/** Called when the activity is first created. */

	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.image);
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String value1 = extras.getString("title");
		String value2 = extras.getString("snippet");
		String indexS = extras.getString("index");
		mImageView = (ImageView) findViewById(R.id.iv_pic);//To initialize ImageView
		//Free up space we need to load the Bitmap ahead
		Runtime.getRuntime().gc();
		
		if(indexS!=null)
			index=Integer.parseInt(indexS);
		
		String pathE = extras.getString("path");
		if(pathE!=null&&pathE.equals(""))
			path=pathE;
		OverlayItem item=MyMaps.itemizedoverlay.mOverlays.get(index);
		try{
		path=MyMaps.itemizedoverlay.pOverlays.get(index);
		
		Log.d(TAG, ""+path+":"+index);
		
		if(!path.equals(""))
		{			
			//We have a previous image, load it
			bitmap 	= BitmapFactory.decodeFile(path);
			mImageView.setImageBitmap(bitmap);	
		}
		//End here
		}
		catch(Exception e)
		{
			Log.d(TAG, "E"+e);
			Log.d(TAG, ""+path);
		}
		
		if (value1 != null && value2 != null) {
			text1 = (EditText) findViewById(R.id.editText1);
			text2 = (EditText) findViewById(R.id.editText2);
			text3 = (EditText) findViewById(R.id.editText3);
			text4 = (EditText) findViewById(R.id.editText4);
			
			text1.setText(value1);
			text2.setText(value2);
		}
		
		//Check it data in DataBase

        SQLiteDatabase sampleDB = null;
        
        try {
        
        sampleDB =  this.openOrCreateDatabase("Locker", MODE_PRIVATE, null);
		
        //filepath, snippet, title, depth, ainfo, lat, long
        
		Cursor c = sampleDB.rawQuery("SELECT * FROM " +
    			" imageinfo" +
    			" where Lat = '"+item.getPoint().getLatitudeE6()+"' and Longitude = '"+item.getPoint().getLongitudeE6()+"'", null);

		//where Lat='"+lat+"' and Longitude='"+longitude+"'");
		
		
    	if (c != null ) {
    		if  (c.moveToFirst()) {
    			String title=c.getString(c.getColumnIndex("Title"));
    			String snippet=c.getString(c.getColumnIndex("Snippet"));
    			String depth=c.getString(c.getColumnIndex("Depth"));
    			String ainfo=c.getString(c.getColumnIndex("AInfo"));
    			
    			text1.setText(title);
    			text2.setText(snippet);
    			text3.setText(depth);
    			text4.setText(ainfo);
    			
    			//do {
    				//String firstName = c.getString(c.getColumnIndex("FirstName"));
    				//int age = c.getInt(c.getColumnIndex("Age"));
    				//results.add("" + firstName + ",Age: " + age);
    				
    			//}while (c.moveToNext());
    		} 
    	}
		
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
        	sampleDB.close();
        }
        
        final String [] items			= new String [] {"From Camera", "From SD Card"};				
		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder		= new AlertDialog.Builder(this);
		
		builder.setTitle("Select Image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) {
				if (item == 0) {
					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File file		 = new File(Environment.getExternalStorageDirectory(),
							   			"tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
					mImageCaptureUri = Uri.fromFile(file);

					try {			
						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
						intent.putExtra("return-data", true);
						
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (Exception e) {
						e.printStackTrace();
					}			
					
					dialog.cancel();
				} else {
					Intent intent = new Intent();
					
	                intent.setType("image/*");
	                intent.setAction(Intent.ACTION_GET_CONTENT);
	                
	                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
				}
			}
		} );
		
		final AlertDialog dialog = builder.create();
		
		
		
		((Button) findViewById(R.id.btn_choose)).setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				dialog.show();
			}
		});
		
		 
		
		
	}
	
	public void amClicked(View view) {
		
		this.doUpload(path, "IMAGE"+path.hashCode()/100000+".jpg");
	}

	
	public void myFinish(View view) {
		//Reduce the Bitmap load thingy
		bitmap.recycle();
		Runtime.getRuntime().gc();
		finish();
	}

	  @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    if (resultCode != RESULT_OK) return;
		   		
			
			
			if (requestCode == PICK_FROM_FILE) {
				mImageCaptureUri = data.getData(); 
				
				path = getRealPathFromURI(mImageCaptureUri); //from Gallery 
			
				if (path == null)
					path = mImageCaptureUri.getPath(); //from File Manager
				
				if (path != null) 
					bitmap 	= BitmapFactory.decodeFile(path);
			} else {
				
				path	= mImageCaptureUri.getPath();
				bitmap  = BitmapFactory.decodeFile(path);
			}
			
			MyMaps.itemizedoverlay.pOverlays.set(index, path);
		    String temp=MyMaps.itemizedoverlay.pOverlays.get(index);
			
			Log.d(TAG, "Added"+temp);
			mImageView.setImageBitmap(bitmap);		
		}
		
		public String getRealPathFromURI(Uri contentUri) {
	        String [] proj 		= {MediaStore.Images.Media.DATA};
	        Cursor cursor 		= managedQuery( contentUri, proj, null, null,null);
	        
	        if (cursor == null) return null;
	        
	        int column_index 	= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        
	        cursor.moveToFirst();

	        return cursor.getString(column_index);
		}
		
		@Override
		public void finish() {
			// Prepare data intent 
			Intent data = new Intent();
			data.putExtra("returnKey1", "Swinging on a star. ");
			data.putExtra("returnKey2", "You could be better then you are. ");
			// Activity finished ok, return the data
			setResult(RESULT_OK, data);
			super.finish();
		}
		
		// filepath, snippet, title, depth, ainfo, lat, long
		public void save(String filepath, String snippet, String title, String depth, String ainfo, String lat, String longitude){
			
	        SQLiteDatabase sampleDB = null;
	        try {
	        	sampleDB =  this.openOrCreateDatabase("Locker", MODE_PRIVATE, null);
	        	
	        	
	        	//Table banava
	        	sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " +
	        			"imageinfo" +
	        			" (FilePath VARCHAR, Snippet VARCHAR," +
	        			" Title VARCHAR, Depth VARCHAR, AInfo VARCHAR, Lat VARCHAR, Longitude VARCHAR);");
	        	sampleDB.execSQL("DELETE FROM imageinfo where Lat='"+lat+"' and Longitude='"+longitude+"'");
	        	
	        	//Ithe Insert karaycha ahe
	        	
	        	sampleDB.execSQL("INSERT INTO " +
	        			"imageinfo" +
	        			" Values ('"+filepath+"','"+snippet+"','"+title+"','"+depth+"','"+ainfo+"','"+lat+"','"+longitude+"');");
	        	
	        }catch(Exception e){
	        	Log.d("ERR", ""+e.getMessage());
	        }
	        
		}
		
		public void doUpload(String filepath,String filename) { 
            HttpClient httpClient = new DefaultHttpClient(); 
            try { 
                    httpClient.getParams().setParameter("http.socket.timeout", new Integer(90000)); // 90 second 
                    post = new HttpPost(new URI("http://219.91.152.130:8080/Locker_WMS/recieve.jsp")); 
                    File file = new File(filepath); 
                    Log.d(TAG,file.getName());
                    FileEntity entity; 
                    if (filepath.substring(filepath.length()-3, filepath.length 
()).equalsIgnoreCase("txt") || 
                            filepath.substring(filepath.length()-3, filepath.length 
()).equalsIgnoreCase("log")) { 
                            entity = new FileEntity(file,"text/plain; charset=\"UTF-8\""); 
                            entity.setChunked(true); 
                    }else { 
                            entity = new FileEntity(file,"binary/octet-stream"); 
                            entity.setChunked(true); 
                    } 
                    post.setEntity(entity); 
                    post.addHeader(FILENAME_STR, filename); 
                    post.addHeader("SNIPPET", text2.getText().toString() );
                    post.addHeader("TITLE", text1.getText().toString());
                    
                    post.addHeader("DEPTH", text3.getText().toString() );
                    post.addHeader("AINFO", text4.getText().toString());
                    
                    OverlayItem item= MyMaps.itemizedoverlay.mOverlays.get(index);
                    post.addHeader("LAT",item.getPoint().getLatitudeE6()+"");
                    post.addHeader("LONG",item.getPoint().getLongitudeE6()+"");
                    
                    HttpResponse response = httpClient.execute(post); 
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) { 
                            Log.e(TAG,"--------Error--------Response Status line code:"+response.getStatusLine()); 
                    }else { 
                            // Here every thing is fine. 
                    		//Mag save kara localstore la
                    		// filepath, snippet, title, depth, ainfo, lat, long
                    		save(filepath,text2.getText().toString(),text1.getText().toString(),text3.getText().toString(), text4.getText().toString(),item.getPoint().getLatitudeE6()+"",item.getPoint().getLongitudeE6()+"");
                    		//Save jhala ki nai?
                    } 
                    HttpEntity resEntity = response.getEntity(); 
                    if (resEntity == null) { 
                            Log.e(TAG,"---------Error No Response !!!-----"); 
                    }else{
                    	Log.e("RESP",""+response.getStatusLine());
                    } 
                    
            } catch (Exception ex) { 
                    Log.e(TAG,"---------Error-----"+ex.getMessage()); 
                    ex.printStackTrace(); 
            } finally { 
                      httpClient.getConnectionManager().shutdown(); 
            } 
            
            Log.d(TAG, "File Uploaded");
    }
		
		
}