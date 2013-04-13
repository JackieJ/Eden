//Author: Jackie Jin
//License: BSD

package com.project.eden;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.*;

public class EdenMain extends Activity {

	@Override
	 public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    final EdenMain oncreate = this;

	    Session.openActiveSession(this, true, new Session.StatusCallback() {

	      @Override
	      public void call(Session session, SessionState state, Exception exception) {
	        if (session.isOpened()) {

	          Request.executeMyFriendsRequestAsync(session, new Request.GraphUserListCallback() {
				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					if(users != null && !users.isEmpty()) {
						LinkedList<String> friendlist = new LinkedList<String>();
						int limit = 5; // test
						for(GraphUser user : users) {
							String u= "https://graph.facebook.com/" + user.getId() + "/picture?type=large"; 
							friendlist.add(u);
							limit--;
							if ( limit == 0 ) {
								break;
							}
						}
						String[] listofurls = friendlist.toArray(new String[friendlist.size()]);

						// friendlist now is a string list of all of your friend's profile pic url 
						GridView gridview = (GridView) findViewById(R.id.gridview);
					    gridview.setAdapter(new ImageAdapter(oncreate, listofurls));
					}
				}
	          });

	        }      
	      }
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	public void enterNext (View view) {
		//Go to the camera activity
		Intent intent = new Intent(this, EdenCamActivity.class);
		startActivity(intent);
	}
}
