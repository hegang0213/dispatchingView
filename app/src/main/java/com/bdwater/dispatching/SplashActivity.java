package com.bdwater.dispatching;

import java.util.ArrayList;

import org.xmlrpc.android.XMLRPCException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	Handler handler;
	Bundle bundle;
	View view;
	TextView messageTextView;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.splash, null);
        setContentView(view);
        
        messageTextView = (TextView)findViewById(R.id.messageTextView);
        
        handler = new Handler() {
        	@SuppressWarnings("unchecked")
        	@Override
        	public void handleMessage(Message msg) {
        		switch(msg.what)
        		{
        		case 1:
    				ArrayList<Group> groups = (ArrayList<Group>)msg.obj;
    				String[] groupsString = new String[groups.size()];
    				String[] groupIdsString = new String[groups.size()];
    				for(int i = 0; i < groups.size(); i++) {
    					groupIdsString[i] = groups.get(i).groupId;
    					groupsString[i] = groups.get(i).name;
    				}
            		bundle = new Bundle();
            		bundle.putStringArray("ids", groupIdsString);
            		bundle.putStringArray("names", groupsString);
            		
            		Message msg1 = handler.obtainMessage();
   					msg1.what = 12;
   					handler.sendMessage(msg1);
            		break;
        		case 2:
	        		Intent intent = new Intent(SplashActivity.this, DispatchingViewActivity.class);
	        		intent.putExtras(bundle);
	    			startActivity(intent);
	    			finish();
        			break;
        			
        		case 10:
        			messageTextView.setText(getResources().getString(R.string.check_network));
        			break;
        		case 11:
        			messageTextView.setText(getResources().getString(R.string.loading_message));
        			break;
        		case 12:
        			messageTextView.setText(getResources().getString(R.string.starting));
            		new Thread() {
            			public void run() {
                   			int interval = 2500;
                   			int time = 0;
                   			try {
            	       			while(time < interval) {
            	       				sleep(500);
            	       				time += 500;
            	       			}
            	       			Message msg = handler.obtainMessage();
            	       			msg.what = 2;
            	       			handler.sendMessage(msg);

                   			} catch (InterruptedException e) {
                   				e.printStackTrace();
                   			}
            			}
            		}.start();
        			break;
        		case -1:
        			String message = msg.obj.toString();
//        			Toast.makeText(SplashActivity.this, message, Toast.LENGTH_LONG);
//        			finish();
					showErrorDelay(message);
        			break;
        		case -2:
        			new AlertDialog.Builder(SplashActivity.this)
        			.setMessage(getResources().getString(R.string.no_network))
        			.setPositiveButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
							finish();
						}
					})
        			.show();
        			break;
        		}

        	}
        };
        
        
        Thread splashThread = new Thread(){
        	public void run() {
        		Message msg = handler.obtainMessage();
        		msg.what = 10;
        		handler.sendMessage(msg);
        		boolean isAvailable = NetworkHelper.checkNetwork(SplashActivity.this);
        		msg = handler.obtainMessage();
        		if(isAvailable) {
        			msg.what = 11;
        			handler.sendMessage(msg);
        			loadGroups();
        		}
        		else {
        			msg.what = -2;
        			handler.sendMessage(msg);
        		}
       		}
        };
        splashThread.start();
        
	}
	private void loadGroups() {
		DataManager dm = new DataManager();
		Message msg = handler.obtainMessage();
		try {
			ArrayList<Group> groups = dm.getGroups();
			msg.what = 1;
			msg.obj = groups;
			handler.sendMessage(msg);
		}
		catch(XMLRPCException e) {
			//e.printStackTrace();
			msg.what = -1;
			msg.obj = e.getMessage();
			handler.sendMessage(msg);
		}
	}
	void showErrorDelay(final String message) {
		messageTextView.setText(message);
		view.postDelayed(new Runnable() {
			public void run() {
				finish();
			}
		}, 5000);
	}
	
}
