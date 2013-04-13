package com.project.eden;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public class ProfileInfo {
	Bitmap profile_picture;
	String id;
	String name;
	String education;
	String work;
	String relationship;
	String significant_other;
	
	public ProfileInfo(Bitmap viewBitmap, JSONObject profile_json) {
		this.profile_picture = viewBitmap;
		formatJSON(profile_json);
	}

	public void formatJSON(JSONObject json) {
		try {
			this.id = json.getString("id");
			this.name = json.getString("name");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
}
