package com.bdwater.dispatching;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {
	public static boolean checkNetwork(Activity v) {
		ConnectivityManager manager = (ConnectivityManager)v.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if(info == null || !info.isAvailable()) {
			return false;
		}
		return true;
	}
}
