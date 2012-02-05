package org.sward.maps.locker;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageActivity extends Activity {

	private Uri mImageCaptureUri;
	private ImageView mImageView;	
	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;
	
	private EditText text1;
	
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
		if (value1 != null && value2 != null) {
			text1 = (EditText) findViewById(R.id.editText1);
			EditText text2 = (EditText) findViewById(R.id.editText2);
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
		
		mImageView = (ImageView) findViewById(R.id.iv_pic);
		
		((Button) findViewById(R.id.btn_choose)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});
		
		 
		
		
	}

	
	public void onClick(View view) {
		finish();
	}

	  @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    if (resultCode != RESULT_OK) return;
		   
			Bitmap bitmap 	= null;
			String path		= "";
			
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
}