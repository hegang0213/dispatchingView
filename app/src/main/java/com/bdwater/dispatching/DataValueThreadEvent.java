package com.bdwater.dispatching;

import java.util.ArrayList;

public interface DataValueThreadEvent {
	void onReceived(String groupId, ArrayList<DataValue> result);
	void onBeforeLoadData(String groupId);
	void onTimeOut(String groupId, int seconds);
	void onUpdated(String groupId, ArrayList<DataValue> result);
	void onFinish();
}
