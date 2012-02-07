package org.sward.maps.locker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

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
			text1.setText(value1);
			text2.setText(value2);
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

		
		public void doUpload(String filepath,String filename) { 
            HttpClient httpClient = new DefaultHttpClient(); 
            try { 
                    httpClient.getParams().setParameter("http.socket.timeout", new Integer(90000)); // 90 second 
                    post = new HttpPost(new URI("http://219.91.152.192:8080/Locker_WMS/recieve.jsp")); 
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
                    
                    OverlayItem item= MyMaps.itemizedoverlay.mOverlays.get(index);
                    post.addHeader("LAT",item.getPoint().getLatitudeE6()+"");
                    post.addHeader("LONG",item.getPoint().getLongitudeE6()+"");
                    
                    HttpResponse response = httpClient.execute(post); 
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) { 
                            Log.e(TAG,"--------Error--------Response Status line code:"+response.getStatusLine()); 
                    }else { 
                            // Here every thing is fine. 
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