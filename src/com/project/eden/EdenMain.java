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
 
    // id  , person
    private LruCache<String, ProfileInfo> mMemoryCache;
    private ImageView mImageView;
       
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
           
        final EdenMain oncreate = this;
           
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
           
        mMemoryCache = new LruCache<String, ProfileInfo>(cacheSize) {
            @SuppressLint("NewApi")
            @Override
            protected int sizeOf(String key, ProfileInfo profile) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return profile.profile_picture.getByteCount() / 1024;
            }
        };
           
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
                                        ImageView imageView = new ImageView(oncreate);
                                                       
                                        for(GraphUser user : users) {
                                            String profile_img = "https://graph.facebook.com/" + user.getId() + "/picture?type=large";
                                            String profile_url =
                                                "https://graph.facebook.com/" + user.getId() + "795270135?fields=id,name,education,relationship_status,significant_other,work";
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
                                                               
                                            oncreate.addBitmapToMemoryCache(user.getId(), profile_info);
                                            break;
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
    @SuppressLint("NewApi")
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
       
    //bitmap memory cache handling
    public void addBitmapToMemoryCache(String key, ProfileInfo profile) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, profile);
        }
    }
 
    public ProfileInfo getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
       
    public ProfileInfo removeBitmapFromMemCache(String key) {
        return mMemoryCache.remove(key);
    }
       
}
