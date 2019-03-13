package com.bdwater.dispatching;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ValueAdapter extends BaseAdapter {
	private UserDefinedManager userDefinedManager;
	private LayoutInflater mInflater;
	private ArrayList<DataValue> mValues;
	private Boolean showSite = true;
	private OnListItemButtonClick addFavoriteButtonClick;
	private OnListItemButtonClick deleteButtonClick;

	private boolean isEdit = false;
	private boolean isDelete = false;
	public ValueAdapter(Context context, UserDefinedManager udm, ArrayList<DataValue> values, Boolean isShowSite){
		mInflater = LayoutInflater.from(context);
		userDefinedManager = udm;
		showSite = isShowSite;
		if(!isShowSite)
		{
			for(int i = values.size() - 1; i >= 0; i--){
				DataValue v = values.get(i);
				if(v.isSite)
				{
					values.remove(i);
				}
			}
		}
		mValues = values;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return mValues.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mValues.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		try {
			ViewHolder holder;
			DataValue value = mValues.get(position);
			
			if(!showSite && value.isSite == true) return null;
			
			holder = new ViewHolder();
			if(value.isSite) {
				convertView = mInflater.inflate(R.layout.listview_site_item, null);
				convertView.setClickable(false);
				holder.siteName = (TextView)convertView.findViewById(R.id.siteNameTextView);
			}
			else {
				convertView = mInflater.inflate(R.layout.listview_item, null);
				holder.icon = (ImageView)convertView.findViewById(R.id.iconImageView);
				holder.value = (TextView)convertView.findViewById(R.id.valueTextView);
				holder.dataTagName = (TextView)convertView.findViewById(R.id.dataTagNameTextView);
				holder.receivedTime = (TextView)convertView.findViewById(R.id.receivedTimeTextView);
				
				holder.upateTimes = (TextView)convertView.findViewById(R.id.updateTimesTextView);
				holder.lostTimes = (TextView)convertView.findViewById(R.id.lostTimesTextView);
				
				holder.addfavorite = (Button)convertView.findViewById(R.id.button_addfavorite);
				holder.addfavorite.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if(addFavoriteButtonClick != null) {
							addFavoriteButtonClick.onButtonClick(v);
							v.setEnabled(false);
						}
					}
				});
				holder.deletefavorite = (Button)convertView.findViewById(R.id.button_deletefavorite);
				holder.deletefavorite.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if(deleteButtonClick != null) {
							deleteButtonClick.onButtonClick(v);
						}
					}
				});
			}
			
			if(value.isSite) {
				holder.siteName.setText(value.siteName);
			}
			else {
				if(value.conversionType == 2) {
					holder.icon.setImageResource(R.drawable.ic_on_off);
				}
				holder.value.setText(value.value);
				switch(value.lostTimes){
				case 0:
					holder.value.setTextColor(Color.RED);
					break;
				case 1:
					holder.value.setTextColor(Color.rgb(0xbb, 0x00, 0x00));
					break;
				case 2:
					holder.value.setTextColor(Color.rgb(0x99, 0x00, 0x00));
					break;
				case 3:
				default:
					holder.value.setTextColor(Color.rgb(0x80, 0x80, 0x80));
					break;
				}
				holder.dataTagName.setText(value.dataTagName);
				holder.receivedTime.setText(value.receivedTime);
				holder.addfavorite.setTag(value);
				holder.deletefavorite.setTag(value);
				
				holder.upateTimes.setText("" + value.updateTimes);
				holder.lostTimes.setText("" + value.lostTimes);
				
				if(isEdit) 
				{
					holder.addfavorite.setVisibility(View.VISIBLE);
					if(userDefinedManager.contains(value.dataTagId)) {
						holder.addfavorite.setEnabled(false);
					}
					else
						holder.addfavorite.setEnabled(true);
				}
				else 
					holder.addfavorite.setVisibility(View.GONE);
				
				if(isDelete)
					holder.deletefavorite.setVisibility(View.VISIBLE);
				else
					holder.deletefavorite.setVisibility(View.GONE);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return convertView;
	}
	public boolean isEdit() {
		return isEdit;
	}
	public boolean isDelete() {
		return isDelete;
	}
	public void setForEdit() {
		isEdit = true;
		isDelete = false;
	}
	public void setForDelete() {
		isDelete = true;
		isEdit = false;
	}
	public void setForNomral() {
		isEdit = false;
		isDelete = false;
	}
	public void setOnAddFavoriteButtonClick(OnListItemButtonClick click) {
		this.addFavoriteButtonClick = click;
	}
	public void setOnDeleteButtonClick(OnListItemButtonClick click) {
		this.deleteButtonClick = click;
	}
	
	public class ViewHolder {
		public TextView siteName;
		public TextView dataTagName;
		public TextView receivedTime;
		public TextView value;
		public ImageView icon;
		
		public TextView upateTimes;
		public TextView lostTimes;
		
		public Button addfavorite;
		public Button deletefavorite;
	}
}
