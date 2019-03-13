package com.bdwater.dispatching;

import java.util.ArrayList;
import java.util.Date;

import org.xmlrpc.android.XMLRPCException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DataValueThread {
	private final static String TAG = "DataValueThread";
	DataValueThreadEvent dataValueThreadEvent;
	
	DataManager dataManager;
	UserDefinedManager userDefinedManager;
	
	Handler handler;
	Thread thread;
	Thread loopThread;
	
	Boolean isDataLoaded = false;
	Date startTime = new Date();
	Boolean canLoop = false;
	int loopInterval = 20 * 1000;
	Boolean isStop = false;

	String currentGroupId = "";

	private final int DATA_RECEIVED = 1;
	private final int TIME_OUT = 2;
	private final int BEFORE_LOADING_DATA = 3;
	private final int DATA_UPDATED = 4;
	private final int ON_FINISH = 5;
	private boolean isCallPreparing = true;
	
	public DataValueThread(UserDefinedManager udm) {
		dataManager = new DataManager();
		userDefinedManager = udm;
		
		initHandler();
		initLoopThread();
	}
	public void pause() {
		canLoop = false;
	}
	public void resume() {
		startTime = new Date();
		if(isDataLoaded) 
			canLoop = true;
	}
	public void stop() {
		isStop = true;
	}
	private void initHandler() {
		handler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
		    public void handleMessage(Message msg) {
		    	switch(msg.what)
		    	{
		    	case DATA_RECEIVED:
					Log.v(TAG, "handler: DATA_RECEIVED");
		    		// get values
		    		isDataLoaded = true;
			   		//startTime = new Date();
			   		//canLoop = true;
			   		
			   		raiseOnReceived((ArrayList<DataValue>)msg.obj);
		        	break;
		        case TIME_OUT:
					Log.v(TAG, "handler: TIME_OUT");
		        	// notify time
		        	//int time = Integer.parseInt(msg.obj.toString());
		        	//int remain = getDataInterval - time;
		        	//remain = remain / 1000;

		        	break;
		        case BEFORE_LOADING_DATA:
					Log.v(TAG, "handler: BEFORE_LOADING_DATA");
		        	startTime = new Date();
		        	canLoop = false;

		        	try {
						raiseOnBeforeLoadData();
					}
					catch(Exception e) {}
		        	isCallPreparing = false;
		        	Log.v(TAG, "handler: BEFORE_LOADING_DATA == End");
		        	break;
		        case DATA_UPDATED:
					Log.v(TAG, "handler: DATA_UPDATED");
					//startTime = new Date();
					//canLoop = true;
		        	
		        	raiseOnUpdated((ArrayList<DataValue>)msg.obj);
		        	break;
		        case ON_FINISH:
					startTime = new Date();
					canLoop = true;
		        	raiseOnFinish();
					break;
		        }
		    }
		};
	}
    private void initLoopThread() {
    	loopThread = new Thread() {
    		@Override
    		public void run() {
    			Thread current = Thread.currentThread();
    			while(current == loopThread && isStop == false) {
    				try {
    					Thread.sleep(100);
    					if(canLoop == true) 
    					{
    						Date now = new Date();
    						long millions = now.getTime() - startTime.getTime();
    						if(millions % 1000 == 0) {
		    					Message msg = handler.obtainMessage();
		    					msg.what = TIME_OUT;
		    					msg.obj = canLoop == true ? millions: 0;
		    					handler.sendMessage(msg);
    						}
    						if(millions >= loopInterval){
    							startTime = new Date();
    							canLoop = false;
       							if(!"".equals(currentGroupId))
       								getValuesByGroupIdInThread(currentGroupId, true);
       							else 
       								getValuesByDataTagIdsInThread(userDefinedManager.getDataTagIds(), true);
        					}
    					}
    				}
    				catch(InterruptedException e) {
    					e.printStackTrace();
    				}
    			}
    		}
    	};
    	loopThread.start();
    }
    
    public void setDataValueThreadEvent(DataValueThreadEvent event) {
    	this.dataValueThreadEvent = event;
    }
    private void raiseOnReceived(ArrayList<DataValue> result) {
    	if(null != this.dataValueThreadEvent)
    		this.dataValueThreadEvent.onReceived(currentGroupId, result);
    }
	private void raiseOnBeforeLoadData() {
		if(null != this.dataValueThreadEvent)
			this.dataValueThreadEvent.onBeforeLoadData(currentGroupId);
	}
	private void raiseOnUpdated(ArrayList<DataValue> result) {
		if(null != this.dataValueThreadEvent)
			this.dataValueThreadEvent.onUpdated(currentGroupId, result);
	}
	private void raiseOnFinish() {
		if(null != this.dataValueThreadEvent)
			this.dataValueThreadEvent.onFinish();
	}
    private void getValuesByGroupIdInThread(String groupId, Boolean forUpdated){
    	try
    	{
			Log.v(TAG, "getValuesByGroupIdInThread:" + groupId);
			Message msg = handler.obtainMessage();
			msg.what = BEFORE_LOADING_DATA;
			handler.sendMessage(msg);
			//isCallPreparing = true;
    		
			//while(isCallPreparing);
    		msg = handler.obtainMessage();
    		Log.v(TAG, "call dataManager.getValuesByGroupId");
    		msg.obj = dataManager.getValuesByGroupId(groupId);
    		Log.v(TAG, "call dataManager.getValauesByGroupId == End");
    		msg.what = (forUpdated == true) ? DATA_UPDATED: DATA_RECEIVED;
    		handler.sendMessage(msg);
    	}
    	catch(XMLRPCException e)
    	{
    		Log.v(TAG, "getValuesByGroupIdInThread: error");
    		e.printStackTrace();
    	}
    	finally {
    		handler.sendEmptyMessage(ON_FINISH);
		}
	}
    private void getValuesByDataTagIdsInThread(String dataTagIds, Boolean forUpdated){
    	try
    	{
			Message msg = handler.obtainMessage();
			msg.what = BEFORE_LOADING_DATA;
			handler.sendMessage(msg);
    		
			isCallPreparing = true;
    		
			while(isCallPreparing);
			
    		msg = handler.obtainMessage();
    		msg.what = (forUpdated == true) ? DATA_UPDATED: DATA_RECEIVED;
    		
    		ArrayList<DataValue> valueList = new ArrayList<DataValue>();
    		if(!"".equalsIgnoreCase(dataTagIds))
    			valueList = dataManager.getValuesByDataTagIds(dataTagIds);
    			
    		msg.obj = valueList;    		
    		handler.sendMessage(msg);
			
    	}
    	catch(XMLRPCException e)
    	{
    		e.printStackTrace();
    	}
    }
    public void getValuesByGroupId(final String groupId) {
    	getValuesByGroupId(groupId, false);
    }
    public void getValuesByGroupId(final String groupId, final Boolean forUpdated) {
    	currentGroupId = groupId;

    	thread = new Thread() {
    		public void run() {
    			getValuesByGroupIdInThread(groupId, forUpdated);
    		}
    	};
    	thread.start();
    }
    public void getValuesByDataTagIds() {
    	getValuesByDataTagIds(false);
    }
    public void getValuesByDataTagIds(final Boolean forUpdated) {
    	currentGroupId = "";

    	thread = new Thread() {
    		public void run() {
    			getValuesByDataTagIdsInThread(userDefinedManager.getDataTagIds(), forUpdated);
    		}
    	};
    	thread.start();    
    }
}
