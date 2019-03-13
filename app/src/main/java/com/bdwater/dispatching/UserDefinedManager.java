package com.bdwater.dispatching;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;

public class UserDefinedManager {
	private final String rootTag = "definedData";
	
	private Activity activity;
	private ArrayList<DataTag> dataTags = new ArrayList<DataTag>();
	public UserDefinedManager(Activity act) {
		
		activity = act;
		SharedPreferences settings = activity.getSharedPreferences(rootTag, 0);
		String dataTagIds = settings.getString("dataTagIds", "");
		String dataTagNames = settings.getString("dataTagNames", "");
		String[] ids = dataTagIds.split(",");
		String[] names = dataTagNames.split(",");
		
		for(int i = 0; i < ids.length; i++) {
			if(!"".equals(ids[i])) {
				DataTag dt = new DataTag();
				dt.id = ids[i];
				dt.name = names[i];
				dataTags.add(dt);
			}
		}
	}
	public void save() {
		String dataTagIds = "", dataTagNames = "";
		for(int i = 0; i < dataTags.size(); i++) {
			dataTagIds += dataTags.get(i).id + ",";
			dataTagNames += dataTags.get(i).name + ",";
		}
		
		if(dataTagIds.length() > 0) dataTagIds = dataTagIds.substring(0, dataTagIds.length() - 1);
		if(dataTagNames.length() > 0) dataTagNames = dataTagNames.substring(0, dataTagNames.length() - 1);
		
		SharedPreferences settings = activity.getSharedPreferences(rootTag, 0);
		settings.edit()
		.putString("dataTagIds", dataTagIds)
		.putString("dataTagNames", dataTagNames)
		.commit();
	}
	public String getDataTagIds() {
		String dataTagIds = "";
		for(int i = 0; i < dataTags.size(); i++) {
			dataTagIds += dataTags.get(i).id + ",";
		}
		if(dataTagIds.length() > 0) dataTagIds = dataTagIds.substring(0, dataTagIds.length() - 1);
		return dataTagIds;
	}
	public ArrayList<DataTag> getDataTags() {
		return dataTags;
	}
	public void add(String dataTagId, String dataTagName) {
		if(null == get(dataTagId)) {
			dataTags.add(new DataTag(dataTagId, dataTagName));
			save();
		}
	}
	public void remove(String dataTagId) {
		DataTag found = get(dataTagId);
		if(null != found) {
			dataTags.remove(found);
			save();
		}
	}
	public DataTag get(String dataTagId) {
		DataTag found = null;
		for(DataTag dt : dataTags) {
			if(dt.id.equalsIgnoreCase(dataTagId)) {
				found = dt;
				break;
			}
		}
		return found;
	}
	public boolean contains(String dataTagId) {
		boolean result = false;
		for(DataTag dt : dataTags) {
			if(dt.id.equalsIgnoreCase(dataTagId)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
