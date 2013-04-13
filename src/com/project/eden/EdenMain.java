//Author: Jackie Jin
//License: BSD

package com.project.eden;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.*;
//import com.project.eden.ImageAdapter.DownloadImageTask;

public class EdenMain extends Activity {

    private static final String DISK_CACHE_SUBDIR = "peoplepictures";
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10;
    		
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    final EdenMain oncreate = this;	// used for contexts
	    final DiskLruImageCache mDiskLruCache = new DiskLruImageCache(oncreate, DISK_CACHE_SUBDIR,DISK_CACHE_SIZE);

		Log.d("SHULIN", "starting cache thing");
	    
	    Session.openActiveSession(this, true, new Session.StatusCallback() {
	    
	      @Override
	      public void call(Session session, SessionState state, Exception exception) {
	    	  Log.d("SHULIN", "call function");
	        if (session.isOpened()) {
	        	Log.d("SHULIN", "session is open");
	        	Request.executeMyFriendsRequestAsync(session, new Request.GraphUserListCallback() {
	        		
					@Override
					public void onCompleted(List<GraphUser> users, Response response) {
						Log.d("SHULIN", "starting to look for friends1");
						if(users != null) {
							ImageView imageView = new ImageView(oncreate);
							Log.d("SHULIN", "starting to look for friends2");
							for(GraphUser user : users) {
								Log.d("SHULIN", "in loop");
								String profile_img = "https://graph.facebook.com/" + user.getId() + "/picture?type=large";
								String profile_url = 
										"https://graph.facebook.com/" + user.getId() + "?fields=id,name,education,relationship_status,significant_other,work";
								Bitmap mIcon11 = null;
								JSONObject profile_json = null;
								
					            try {
					                InputStream in = new java.net.URL(profile_url).openStream();
					                profile_json = new JSONObject(convertStreamToString(in));
					                in.close();
					            } catch (Exception e) {
					                Log.e("Error", e.getMessage());
					                e.printStackTrace();
					            }
					            
								new DownloadImageTask(imageView).execute(profile_img);
								ImageView image=new ImageView(oncreate);
								Bitmap viewBitmap = Bitmap.createBitmap(image.getWidth(),image.getHeight(),Bitmap.Config.ARGB_8888);
								
								ProfileInfo profile_info = new ProfileInfo(viewBitmap, profile_json);
								mDiskLruCache.put(user.getId(), profile_info);
								Log.d("SHULIN", "added user " + user.getId());
							}
						}
					}
	          });
	          
	        }      
	      }
	    });
	}
	 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void enterNext (View view) {
		//Go to the camera activity
		Intent intent = new Intent(this, EdenCamActivity.class);
		startActivity(intent);
	}
	
	/* tools */
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
	
	public static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
}
