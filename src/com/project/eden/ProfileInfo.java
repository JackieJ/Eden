package com.project.eden;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public class ProfileInfo {
    public Bitmap profile_picture;
    public String id;
    public String name;
    public String school;
    public String year;
    public String major;
    public String work;
    public String relationship;
    
    public ProfileInfo(Bitmap viewBitmap, JSONObject profile_json) {
        this.profile_picture = viewBitmap;
        formatJSON(profile_json);
    }

    public void formatJSON(JSONObject json) {
        try {
            this.id = json.getString("id");
            this.name = json.getString("name");
            JSONArray education = json.optJSONArray("education");
            //get the first school info
            if (education != null) {
            	JSONObject schoolObj = education.optJSONObject(0);
            	if (schoolObj != null) {
            		JSONObject schoolb = schoolObj.optJSONObject("school");
            		if (schoolb != null) {
            			this.school = schoolb.optString("name");
            		}
            		JSONObject yearb = schoolObj.optJSONObject("year");
            		if (yearb != null) {
            			this.year = yearb.optString("name");
            		}
            		JSONObject concentb = schoolObj.optJSONObject("concentration");
            		if (concentb != null) {
            			this.major = concentb.optString("name");
            		}
            	}
            }
            //get the relationship status
            this.relationship = json.optString("relationship_status");
            //retrieve work status
            JSONArray workArray = json.optJSONArray("work");
            if (workArray != null) {
            	JSONObject oneWork = workArray.optJSONObject(0);
            	if (oneWork != null) {
            		JSONObject employerb = oneWork.optJSONObject("employer");
            		this.work = employerb.optString("name");
            	            	
            	}
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    }
}
