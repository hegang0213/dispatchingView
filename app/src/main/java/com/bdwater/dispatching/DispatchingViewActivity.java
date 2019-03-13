package com.bdwater.dispatching;

import java.io.Console;
import java.util.ArrayList;

import com.bdwater.dispatching.R.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DispatchingViewActivity extends Activity {
	private final static String TAG = "Main";

	private DataValueThread dataValueThread;
	private UserDefinedManager userDefinedManager;
	
	private ArrayList<Group> groups = new ArrayList<Group>();
	private ArrayList<Button> buttons = new ArrayList<Button>();
	
	private Handler handler;
	
	private Button produceWaterTotalButton;
	private ListView lv;
	private TextView titleTextView;
	private LinearLayout bottomLinearLayout;
	private LinearLayout progressLinearLayout;

	private ImageView hintImageView;
	private TextView longClickHintTextView;
	private Button detailButton;
	private Button cancelButton;
	
	private String currentGroupId;
	private String currentGroupName;
	
	private Boolean showSite = true;
	private ValueAdapter adapter;
	private ArrayList<DataValue> values;
	
	private boolean isPaused = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        userDefinedManager = new UserDefinedManager(this);
        dataValueThread = new DataValueThread(userDefinedManager);
        
        getBundleForGroups();
        initDataValueThread();
        initControls();
        initializeHandler();
        createBottomButtons();
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	dataValueThread.pause();
    	isPaused = true;
    }
    @Override 
    protected void onResume() {
    	super.onResume();
    	
    	if(null == dataValueThread || isPaused == false) return;
    	dataValueThread.resume();
    	isPaused = false;
    }
    @Override
    protected void onDestroy() {
    	dataValueThread.stop();
    	super.onDestroy();
    	System.exit(0);
    }
    private void initDataValueThread() {
    	dataValueThread.setDataValueThreadEvent(new DataValueThreadEvent() {
			public void onReceived(String groupId, ArrayList<DataValue> result) {
				Log.v(TAG, "onReceived:" + groupId);
				// TODO Auto-generated method stub
				values = result;
				updateValues(result);
				
				//finishLoadingData();
				setTitle();
				//AnimationHelper.fadeIn(DispatchingViewActivity.this, lv);
				
				Toast.makeText(DispatchingViewActivity.this, getResources().getString(string.refresh_time_hint), Toast.LENGTH_LONG).show();
			}

			public void onBeforeLoadData(String groupId) {
				Log.v(TAG, "onBeforeLoadData");
				// TODO Auto-generated method stub
				prepareForLoadingData();
			}

			public void onTimeOut(String groupId, int seconds) {
				// TODO Auto-generated method stub
				Log.v(TAG, "onTimeOut");
				//finishUpdatingData();
				
			}

			public void onUpdated(String groupId, ArrayList<DataValue> result) {
				Log.v(TAG, "onUpdated:" + groupId);

				// TODO Auto-generated method stub
				DataValue[] oldValues = new DataValue[values.size()];
				
				values.toArray(oldValues);
				values.clear();
				
				for(DataValue newValue : result) {
					DataValue found = null;
					boolean isNewValue = false;
					if(newValue.isSite) continue;
					for(DataValue oldValue : oldValues) {
						if(oldValue.isSite) continue;
						if(newValue.dataTagId.equals(oldValue.dataTagId)) {
							found = oldValue;
							if(!newValue.receivedTime.equals(oldValue.receivedTime)) {
								isNewValue = true;
								break;
							}
							break;
						}
					}
					if(null != found && !isNewValue) {
						// gets a old value
						newValue.lostTimes = found.lostTimes + 1;
					}
					if(null != found) {
						newValue.updateTimes = found.updateTimes + 1;
					}
				}
				for(DataValue v : result) {
					if(!showSite && v.isSite)
						continue;
					else
						values.add(v);
				}

				adapter.notifyDataSetChanged();
				//finishUpdatingData();
				setTitle();
				
				Toast.makeText(DispatchingViewActivity.this, getResources().getString(string.refresh_time_hint), Toast.LENGTH_SHORT).show();
			}
			public void onFinish() {
				Log.v(TAG, "onFinish");
				finishUpdatingData();
			}
    	});
    }
    private void initControls() {
        titleTextView = (TextView)findViewById(R.id.titleTextView);
        produceWaterTotalButton = (Button)findViewById(R.id.produceWaterTotalButton);
        lv = (ListView)findViewById(R.id.listView);
        bottomLinearLayout = (LinearLayout)findViewById(R.id.bottomLinearLayout);
        progressLinearLayout = (LinearLayout)findViewById(R.id.progressLinearLayout);
        
        //Animation animation = AnimationUtils.loadAnimation(this, R.anim.list_anim);
        //LayoutAnimationController lac = new LayoutAnimationController(animation);
        //lv.setLayoutAnimation(lac);
        
        hintImageView = (ImageView)findViewById(R.id.hintImageView);
        longClickHintTextView = (TextView)findViewById(R.id.longClickHintTextView);
        detailButton = (Button)findViewById(R.id.detailButton);
        detailButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		Resources r = getResources();
        		String message = r.getString(R.string.detail_message);
        		new AlertDialog.Builder(DispatchingViewActivity.this)
        			.setTitle(getResources().getString(string.indicate_name))
        			.setMessage(message)
        			.setPositiveButton(getResources().getString(string.close), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					}).show();
        	}
        });
        cancelButton = (Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		adapter.setForNomral();
        		adapter.notifyDataSetChanged();
        		
        		cancelButton.setVisibility(View.GONE);
        		
        		showHint();
        	}
        });
        produceWaterTotalButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(DispatchingViewActivity.this, ProduceWaterActivity.class);
        		startActivity(intent);
        	}
        });
        lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				onListViewItemClick(parent, view, position, id);
			}
        });
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        		return onListViewLongItemClick(parent, view, position, id);
        	}
        });
    }
    private void getBundleForGroups() {
        Bundle bundle =  this.getIntent().getExtras();
        String[] ids = bundle.getStringArray("ids");
        String[] names = bundle.getStringArray("names");
        
        groups = new ArrayList<Group>();
        for(int i=0; i < ids.length; i++) {
        	Group g = new Group();
        	g.groupId = ids[i];
        	g.name = names[i];
        	groups.add(g);
        }
        Group g = new Group();
        g.groupId = "";
        g.name = getResources().getString(string.self_choice);
        groups.add(g);
    }
    private void initializeHandler() {
        handler = new Handler() {
     		@Override
        	public void handleMessage(Message msg) {
        		switch(msg.what)
        		{
        		case 5:
        			DataValue value = (DataValue)msg.obj;
    				DataValue found = null;
        			int countInSite = 0;
        			DataValue site = null;
    				for(DataValue v : values) {
    					// for site
    					if(v.isSite && value.siteId.equalsIgnoreCase(v.siteId)) {
    						// is parent site
    						site = v;
    						continue;
    					}
    					
    					// for value
    					if(v.siteId.equalsIgnoreCase(value.siteId)) countInSite++;
    					if(found == null && !v.isSite && v.dataTagId.equalsIgnoreCase(value.dataTagId)) {
    						found = v;
    					}
    				}
    				if(found != null) values.remove(found);
    				if(countInSite <= 1 && site != null) values.remove(site);
    				adapter.notifyDataSetChanged();
    				break;
        		}
        	}
        };
    }
    private void setTitle() {
    	int count = 0;
    	for(DataValue v : values) {
    		if(!v.isSite) count++;
    	}
		titleTextView.setText(currentGroupName + "(" + count + ")");
    }
    
    private void createBottomButtons() {
    	for(Group g : groups) {
    		Button b = new Button(this);
    		b.setTag(g.groupId);
    		b.setText(g.name);
    		b.setPadding(0, 10, 10, 0);
    		b.setCompoundDrawablePadding(-8);
    		b.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.building, 0, 0);
    		b.setTextColor(Color.WHITE);
    		b.setTextSize(12);
    		b.setBackgroundColor(Color.TRANSPARENT);
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT, 1.0f);
    		b.setLayoutParams(params);
    		bottomLinearLayout.addView(b);
    		buttons.add(b);
    		
    		b.setOnClickListener(new Button.OnClickListener() {
    			public void onClick(View v) {
    				onButtonClick(v);
    			}
    		});
    	}
    }
    
    private void onListViewItemClick(AdapterView<?> parent, View view, int position, long id) {
    	Intent intent = new Intent(DispatchingViewActivity.this, Detail.class);
    	
    	ValueAdapter adapter = (ValueAdapter)parent.getAdapter();
    	DataValue value = (DataValue)adapter.getItem(position);
    	if(value.isSite) return;
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("dataTagName", value.dataTagName);
    	bundle.putString("dataTagId", value.dataTagId);
    	
    	intent.putExtras(bundle);
    	startActivity(intent);
    }
    private boolean onListViewLongItemClick(AdapterView<?> parent, View view, int position, long id) {
    	if("".equals(currentGroupId))
    		adapter.setForDelete();
    	else
    		adapter.setForEdit();
    	
    	adapter.notifyDataSetChanged();
    	cancelButton.setVisibility(0);
    	
    	//hintImageView.setVisibility(8);
    	//longClickHintTextView.setVisibility(8);
    	hideHint();
    	return true;
    }
    private void onButtonClick(View v) {
    	Button b = (Button)v;
    	
    	// set current group id by user choice
    	currentGroupId = b.getTag().toString();
    	currentGroupName = b.getText().toString();
    	
		titleTextView.setText(currentGroupName);
		
    	// change state of buttons
		changeButtonsState(b);
		hideHint();
		cancelButton.setVisibility(View.GONE);
		
    	if("".equals(currentGroupId)){
			dataValueThread.getValuesByDataTagIds();
		}
		else {
			// get values by current group id via thread
			dataValueThread.getValuesByGroupId(currentGroupId);
		}
    }
    private void enableButtons() {
    	for(Button button: buttons){
    		button.setEnabled(true);
    	}
    }
    private void disableButtons() {
		for(Button button : buttons){
			button.setEnabled(false);
		}
    }
    private void changeButtonsState(Button current) {
    	current.setBackgroundResource(R.drawable.bottombar_selected_background);
		for(Button button : buttons){
			if(!button.equals(current)){
				button.setBackgroundColor(Color.TRANSPARENT);
			}
		}
    }
    private void showHint() {
    	//cancelButton.setVisibility(8);
    	
    	hintImageView.setVisibility(View.VISIBLE);
    	longClickHintTextView.setVisibility(View.VISIBLE);
    	detailButton.setVisibility(View.VISIBLE);
    }
    private void hideHint() {
    	//cancelButton.setVisibility(0);
    	
    	detailButton.setVisibility(View.GONE);
    	hintImageView.setVisibility(View.GONE);
    	longClickHintTextView.setVisibility(View.GONE);
    }
    private void prepareForLoadingData() {
    	disableButtons();
    	
    	hideHint();
		progressLinearLayout.setVisibility(View.VISIBLE);
		
		//AnimationHelper.fading(this, lv);
    }
    private void finishLoadingData() {
    	enableButtons();
    	
    	if(!adapter.isEdit() && !adapter.isDelete())
    		showHint();
    	else
    		hideHint();
    	
    	progressLinearLayout.setVisibility(View.GONE);
    	
    	if(lv.getVisibility() != View.VISIBLE)
    		lv.setVisibility(View.VISIBLE);
    	
		lv.startLayoutAnimation();
    }
    private void finishUpdatingData() {
    	enableButtons();
    	
    	if(!adapter.isEdit() && !adapter.isDelete())
    		showHint();
    	else
    		hideHint();
    	
    	progressLinearLayout.setVisibility(View.GONE);

		lv.startLayoutAnimation();
    	
    }
    private void updateValues(ArrayList<DataValue> values) {
    	showSite = true;
    	if("2AC3FDA6-FE79-436D-A5F3-159866265222".equalsIgnoreCase(currentGroupId)) showSite = false;
		adapter = new ValueAdapter(this, userDefinedManager, values, showSite);
		adapter.setOnAddFavoriteButtonClick(new OnListItemButtonClick() {
			public void onButtonClick(View v) {
				DataValue value = (DataValue)v.getTag();
				userDefinedManager.add(value.dataTagId, value.dataTagName);
				Toast.makeText(DispatchingViewActivity.this, getResources().getString(string.add_success), Toast.LENGTH_SHORT).show();
			}

		});
		adapter.setOnDeleteButtonClick(new OnListItemButtonClick() {
			public void onButtonClick(View v) {
				DataValue value = (DataValue)v.getTag();
				userDefinedManager.remove(value.dataTagId);
				Message msg = handler.obtainMessage();
				msg.what = 5;
				msg.obj = value;
				handler.sendMessage(msg);
				Toast.makeText(DispatchingViewActivity.this, getResources().getString(string.del_success), Toast.LENGTH_SHORT).show();
			}
		});
		lv.setAdapter(adapter);
    }
}



