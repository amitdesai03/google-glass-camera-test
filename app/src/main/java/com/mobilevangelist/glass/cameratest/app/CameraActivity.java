/*
 * Copyright (C) 2013 Mobilevangelist.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.mobilevangelist.glass.cameratest.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.TimelineManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.itraff.TestApi.ItraffApi.ItraffApi;

/**
 * Camera preview
 */
public class CameraActivity extends Activity {
  private CameraPreview _cameraPreview;
  private Camera _camera;

  private Context _context = this;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    _cameraPreview = new CameraPreview(this);
    setContentView(_cameraPreview);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      // Handle tap events.
      case KeyEvent.KEYCODE_CAMERA:
        android.util.Log.d("CameraActivity", "Camera button pressed.");
        _cameraPreview.getCamera().takePicture(null, null, new SavePicture());
        android.util.Log.d("CameraActivity", "Picture taken.");

        return true;
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
        android.util.Log.d("CameraActivity", "Tap.");
        _cameraPreview.getCamera().takePicture(null, null, new SavePicture());

        return true;
      default:
        return super.onKeyDown(keyCode, event);
    }
  }



  class SavePicture implements Camera.PictureCallback {
    

		public  final Integer CLIENT_API_ID = 42865;
		public  final String CLIENT_API_KEY = "502cbb30e3";

		private static final String TAG = "TestApi";
		
		// handler that receives response from api
		@SuppressLint("HandlerLeak")
		private Handler itraffApiHandler = new Handler() {
			// callback from api
			@Override
			public void handleMessage(Message msg) {
				Log.d("MSG","Yippie");
//				dismissWaitDialog();
				Bundle data = msg.getData();
				if (data != null) {
					Integer status = data.getInt(ItraffApi.STATUS, -1);
					String response = data.getString(ItraffApi.RESPONSE);
					Log.d("status",status+"");
					Log.d("respoonse",response);
					Card photoCard = new Card(_context);
			        if(response.contains("i_533af5bc21df9"))
			        {	
			        	photoCard.setText("Price for red santa on eBay is 3$");
			        }else if(response.contains("i_533af5b648802"))
			        {	
			        	photoCard.setText("Price for grey horse on eBay is 4$");
			        }
			        else
			        {
			        	photoCard.setText("Item not found on eBay");
			        }
			        TimelineManager.from(_context).insert(photoCard);
				}
			}
		};
		
	@Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
      android.util.Log.d("CameraActivity", "In onPictureTaken().");
      Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
      android.util.Log.d("Image:",image.toString());
      
		if (image != null) {
			android.util.Log.d("","image != null");

			// chceck internet connection
			if (ItraffApi.isOnline(getApplicationContext())) {
//				showWaitDialog();
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				// send photo
				ItraffApi api = new ItraffApi(CLIENT_API_ID,
						CLIENT_API_KEY, TAG, true);
				Log.d("KEY", CLIENT_API_ID.toString());
				
				api.setMode(ItraffApi.MODE_SINGLE);
				

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compress(Bitmap.CompressFormat.JPEG, 100,
						stream);
				byte[] pictureData = stream.toByteArray();
				Log.d("about to send photo","Yay");
				api.sendPhoto(pictureData, itraffApiHandler,
						prefs.getBoolean("allResults", true));
			} 
		}
		
        

    }
  }
}
