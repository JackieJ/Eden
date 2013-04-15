//Author: Jackie Jin
//License: BSD
package com.project.eden;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.facebook.*;
import com.facebook.model.*;

public class EdenMain extends Activity {
	private LruCache<String, Bitmap> mMemoryCache;
	private Context mainContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mainContext = this;
		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() 	{
			// callback when session changes state
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				 if (session.isOpened()) {

			          // make request to the /me API
			          Request.executeMyFriendsRequestAsync(session, new Request.GraphUserListCallback() {

			            // callback after Graph API response with user object 
			            public void onCompleted(List<GraphUser> users, Response response) {
			            	if(users != null && !users.isEmpty()) {
								LinkedList<String> friendlist = new LinkedList<String>();
								for(GraphUser user : users) {
									String u= "https://graph.facebook.com/" + user.getId() + "/picture?type=large"; 
									friendlist.add(u);
								}
								String[] listofurls = friendlist.toArray(new String[friendlist.size()]);

								// friendlist now is a string list of all of your friend's profile pic url 
								GridView gridview = (GridView) findViewById(R.id.gridview);
								gridview.setAdapter(new ImageAdapter(mainContext, listofurls));;
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
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.button_enter) {
			Intent intent = new Intent(this, EdenCamActivity.class);
			startActivity(intent);
		}
		
		return true;
	}
}