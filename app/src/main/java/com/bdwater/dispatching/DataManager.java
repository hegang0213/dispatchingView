package com.bdwater.dispatching;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

public final class DataManager {
	private static XMLRPCClient client;
	private final String rpcUriString = "http://121.18.65.239:50080/DataMethods.ashx";
	public DataManager() {
		URI uri = URI.create(rpcUriString);
		client = new XMLRPCClient(uri);
	}
	@SuppressWarnings("unchecked")
	public ArrayList<Group> getGroups() throws XMLRPCException {
		ArrayList<Group> groups = new ArrayList<Group>();
    	try
    	{
    		Object[] values = (Object[])client.call("getGroups");
	    	for(int i = 0; i < values.length; i++) {
	    		Group g = new Group();
	    		HashMap<String, Object> value = (HashMap<String, Object>)values[i];
	    		Iterator<Entry<String, Object>> it = value.entrySet().iterator();
	    		while(it.hasNext()) {
	    			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
	    			String key = entry.getKey();
	    			if("groupId".equals(key)) {
	    				g.groupId = entry.getValue().toString();
	    			}
	    			else if("name".equals(key)) {
	    				g.name = entry.getValue().toString();
	    			}
	    			else if("key".equals(key)) {
	    				g.key = entry.getValue().toString();
	    			}
	    		}
	    		groups.add(g);
	    	}
	    	return groups;
    	}
    	catch(XMLRPCException e)
    	{
    		throw e;
    	}
	}
	@SuppressWarnings("unchecked")
	public ArrayList<DataValue> getValuesByGroupId(String groupId) throws XMLRPCException {

		ArrayList<DataValue> result = new ArrayList<DataValue>();
		try {
			Object[] values = (Object[])client.call("getValuesByGroupId", groupId);
			for(int i = 0; i < values.length; i++){
				DataValue dv = new DataValue();
				HashMap<String, Object> value = (HashMap<String, Object>)values[i];
				Iterator<Entry<String, Object>> it = value.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
					String key = entry.getKey();
					Object v = entry.getValue();
					if("isSite".equals(key)) {
						dv.isSite = Boolean.parseBoolean(v.toString());
					}
					else if("siteName".equals(key)) {
						dv.siteName = v.toString();
					}
					else if("siteId".equals(key)) {
						dv.siteId = v.toString();
					}
					else if("dataTagId".equals(key)) {
						dv.dataTagId = v.toString();
					}
					else if("dataTagName".equals(key)) {
						dv.dataTagName = v.toString();
					}
					else if("conversionType".equals(key)){
						dv.conversionType = (Integer)v;
					}
					else if("receivedTime".equals(key)){
						dv.receivedTime = v.toString();
					}
					else if("value".equals(key)){
						dv.value = v.toString();
					}
				}
				if(!dv.siteName.startsWith("容城"))
					result.add(dv);
			}
			return result;
		}
		catch(XMLRPCException e){
			throw e;
		}
	}
	@SuppressWarnings("unchecked")
	public ArrayList<DataValue> getValuesByDataTagIds(String dataTagIds) throws XMLRPCException {
		ArrayList<DataValue> result = new ArrayList<DataValue>();
		try {
			Object[] values = (Object[])client.call("getValuesByDataTagIds", dataTagIds);
			for(int i = 0; i < values.length; i++){
				DataValue dv = new DataValue();
				HashMap<String, Object> value = (HashMap<String, Object>)values[i];
				Iterator<Entry<String, Object>> it = value.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
					String key = entry.getKey();
					Object v = entry.getValue();
					if("isSite".equals(key)) {
						dv.isSite = Boolean.parseBoolean(v.toString());
					}
					else if("siteName".equals(key)) {
						dv.siteName = v.toString();
					}
					else if("siteId".equals(key)) {
						dv.siteId = v.toString();
					}
					else if("dataTagId".equals(key)) {
						dv.dataTagId = v.toString();
					}
					else if("dataTagName".equals(key)) {
						dv.dataTagName = v.toString();
					}
					else if("conversionType".equals(key)){
						dv.conversionType = (Integer)v;
					}
					else if("receivedTime".equals(key)){
						dv.receivedTime = v.toString();
					}
					else if("value".equals(key)){
						dv.value = v.toString();
					}
				}
				result.add(dv);
			}
			return result;
		}
		catch(XMLRPCException e){
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<SampleData> getSampleData(String dataTagIdString, String startTimeString, String endTimeString) throws XMLRPCException {
		ArrayList<SampleData> result = new ArrayList<SampleData>();
		try {
			Object[] values = (Object[])client.call("getSampleData", dataTagIdString, startTimeString, endTimeString);
			for(int i = 0; i < values.length; i++){
				SampleData dv = new SampleData();
				HashMap<String, Object> value = (HashMap<String, Object>)values[i];
				Iterator<Entry<String, Object>> it = value.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
					String key = entry.getKey();
					Object v = entry.getValue();
					if("receivedTime".equals(key)) {
						dv.receivedTime = v.toString();
					}
					else if("value".equals(key)) {
						dv.value = Double.parseDouble(v.toString());
					}
				}
				result.add(dv);
			}
			return result;
		}
		catch(XMLRPCException e){
			throw e;
		}
	}
	
	public final static int UNDER_WATER = 0;
	public final static int SURFACE_WATER = 1;
	public final static int TOTAL_WATER = 2;
	@SuppressWarnings("unchecked")
	public ArrayList<ProduceWaterData> getProduceWaterByYear(int category, int year, int compareYear) throws XMLRPCException {
		ArrayList<ProduceWaterData> result = new ArrayList<ProduceWaterData>();
		try {
			Object[] values = (Object[])client.call("getProduceWaterByYear", category, year, compareYear);
			for(int i = 0; i < values.length; i++){
				ProduceWaterData pw = new ProduceWaterData();
				HashMap<String, Object> value = (HashMap<String, Object>)values[i];
				Iterator<Entry<String, Object>> it = value.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
					String key = entry.getKey();
					Object v = entry.getValue();
					if("month".equals(key)) {
						pw.time = Integer.parseInt(v.toString());
					}
					else if("value".equals(key)) {
						if("".equals(v.toString()))
							pw.value = -1;
						else
							pw.value = Integer.parseInt(v.toString());
					}
					else if("value1".equals(key)) {
						if("".equals(v.toString()))
							pw.value1 = -1;
						else
							pw.value1 = Integer.parseInt(v.toString());
					}
				}
				result.add(pw);
			}
			return result;
		}
		catch(XMLRPCException e) {
			throw e;
		}
	}
	@SuppressWarnings("unchecked")
	public ArrayList<ProduceWaterData> getProduceWaterByMonth(int category, int year, int month, int compareYear, int compareMonth) throws XMLRPCException {
		ArrayList<ProduceWaterData> result = new ArrayList<ProduceWaterData>();
		try {
			Object[] values = (Object[])client.call("getProduceWaterByMonth", category, year, month, compareYear, compareMonth);
			for(int i = 0; i < values.length; i++){
				ProduceWaterData pw = new ProduceWaterData();
				HashMap<String, Object> value = (HashMap<String, Object>)values[i];
				Iterator<Entry<String, Object>> it = value.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
					String key = entry.getKey();
					Object v = entry.getValue();
					if("day".equals(key)) {
						pw.time = Integer.parseInt(v.toString());
					}
					else if("value".equals(key)) {
						if("".equals(v.toString()))
							pw.value = -1;
						else
							pw.value = Integer.parseInt(v.toString());
					}
					else if("value1".equals(key)) {
						if("".equals(v.toString()))
							pw.value1 = -1;
						else
							pw.value1 = Integer.parseInt(v.toString());
					}
				}
				result.add(pw);
			}
			return result;
		}
		catch(XMLRPCException e) {
			throw e;
		}
	}
}
