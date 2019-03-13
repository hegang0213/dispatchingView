package com.bdwater.dispatching;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import org.xmlrpc.android.XMLRPCException;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.BarChart;
import com.googlecode.charts4j.BarChartPlot;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.LinearGradientFill;
import com.googlecode.charts4j.Plots;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ProduceWaterActivity extends Activity {
	private DataManager dm = new DataManager();
	private Handler handler;
	private static final int YearDataReceived = 0;
	private static final int MonthDataReceived = 1;
	private static final int UpdatedChart = 2;
	
	private ProduceWaterCategory pwCategory = ProduceWaterCategory.TotalWater;
	private SearchMode searchMode = SearchMode.Single;
	private SearchType searchType = SearchType.Year;
	private int year1 = -1;
	private int year2 = -1;
	private int month1 = -1;
	private int month2 = -1;
	private int chartWidth = 480;
	private boolean scrollable = true;
	
	private ArrayList<Button> buttons = new ArrayList<Button>();
	private TextView titleTextView;
	private View data_progress_float;
	private View chart_progress_float;
	private Button compareButton;
	private Button date1Button;
	private Button date2Button;
	private Button searchButton;
	
	private HorizontalScrollView imageScrollView;
	private ImageView chartImageView;
	
	private TableLayout dataTableLayout;
	private LinearLayout popupLinearLayout;
	private LinearLayout popupContentLinearLayout;
	
	private ArrayList<Button> bottomButtons = new ArrayList<Button>();
	
	DisplayMetrics metrics = new DisplayMetrics();

	String year = "";
	String month = "";
	String day = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.produce_water);
		
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		year = getResources().getString(R.string.year);
		month = getResources().getString(R.string.month);
		day = getResources().getString(R.string.day);

		initControls();
		initHandler();

		getYearDataByThread();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(popupLinearLayout.getVisibility() == View.VISIBLE) {
				popupLinearLayout.setVisibility(View.GONE);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	// initialize controls
	private void initControls() {
		View titlebar = findViewById(R.id.titlebar);
		titleTextView = (TextView)titlebar;
		Resources r = getResources();
		String title = r.getString(R.string.total_water) + " - " + r.getString(R.string.query);
		titleTextView.setText(title);
		
		// top bar
		Button button = (Button)findViewById(R.id.yearButton);
		button.setOnClickListener(onButtonClick);
		buttons.add(button);
		button = (Button)findViewById(R.id.monthButton);
		button.setOnClickListener(onButtonClick);
		buttons.add(button);
		
		// condition bar
		compareButton = (Button)findViewById(R.id.compareButton);
		//compareButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_off, 0, 0, 0);
		//compareButton.setBackgroundResource(R.drawable.blue_button_disable_bg);
		compareButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Button b = (Button)v;
				if(searchMode == SearchMode.Single) {
					searchMode = SearchMode.Multi;
					b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_on, 0, 0, 0);
					v.setBackgroundResource(R.drawable.blue_button_bg);
				}
				else {
					searchMode = SearchMode.Single;
					b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_off, 0, 0, 0);
					v.setBackgroundResource(R.drawable.blue_button_disable_bg);
				}
				onSearchModeChanged();
			}
		});
		
		date1Button = (Button)findViewById(R.id.date1Button);
		date1Button.setOnClickListener(onDate1ButtonClick);
		date2Button = (Button)findViewById(R.id.date2Button);
		date2Button.setOnClickListener(onDate2ButtonClick);
		searchButton = (Button)findViewById(R.id.searchButton);
		searchButton.setOnClickListener(onSearchButtonClick);
		
		// content
		chart_progress_float = (View)findViewById(R.id.chart_progress_float);
		data_progress_float = (View)findViewById(R.id.data_progress_float);
		dataTableLayout = (TableLayout)findViewById(R.id.dataTableLayout);
		
		imageScrollView = (HorizontalScrollView)findViewById(R.id.imageScrollView);
		imageScrollView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(!scrollable)	{
					return true;
				}
				else {
					v.onTouchEvent(event);
					return false;
				}
			}
		});
		chartImageView = (ImageView)findViewById(R.id.imageView);
		
		// pop up
		popupLinearLayout = (LinearLayout)findViewById(R.id.popupLinearLayout);
		popupContentLinearLayout = (LinearLayout)findViewById(R.id.popupContentLinearLayout);
		popupLinearLayout.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				popupLinearLayout.setVisibility(View.GONE);
				return false;
			}
		});
	
		// bottom buttons
		button = (Button)findViewById(R.id.underWaterButton);
		button.setOnClickListener(onBottomButtonClick);
		bottomButtons.add(button);
		button = (Button)findViewById(R.id.surfaceWaterButton);
		button.setOnClickListener(onBottomButtonClick);
		bottomButtons.add(button);
		button = (Button)findViewById(R.id.totalWaterButton);
		button.setOnClickListener(onBottomButtonClick);
		bottomButtons.add(button);
	}
	// initializes handler
	private void initHandler() {
		handler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case YearDataReceived:
				case MonthDataReceived:
					ArrayList<ProduceWaterData> values = (ArrayList<ProduceWaterData>)msg.obj;
					if(searchMode == SearchMode.Single)
						displayDataForSingle(values);
					else
						displayDataForMulti(values);
					
					// update chart 
					updateChartByThread(values);
					endGetData();
					break;
				case UpdatedChart:
					if(null != msg.obj) {
						chartImageView.setImageBitmap((Bitmap)msg.obj);
						imageScrollView.scrollTo(0, 0);
						if(searchType == SearchType.Year) {
							imageScrollView.setHorizontalScrollBarEnabled(false);
							scrollable = false;
						}
						else {
							imageScrollView.setEnabled(true);
							scrollable = true;
						}
					}
					endGetChart();
					break;
				}
			}
		};
	}
	
	private void setCategory(ProduceWaterCategory value) {
		if(!pwCategory.equals(value)) {
			pwCategory = value;
			onCategoryChanged();
		}
	}
	private void setSearchType(SearchType value) {
		if(!searchType.equals(value)) {
			searchType = value;
			onSearchTypeChanged();
		}
	}

	private void setYear1(int year) {
		if(year != year1) {
			year1 = year;
			onYear1Changed();
		}
	}
	private void setYear2(int year) {
		if(year != year2) {
			year2 = year;
			onYear2Changed();
		}
	}
	private void setMonth1(int year, int month) {
		if(year != year1 || month != month1) {
			year1 = year;
			month1 = month;
			onMonth1Changed();
		}
	}
	private void setMonth2(int year, int month) {
		if(year != year2 || month != month2) {
			year2 = year;
			month2 = month;
			onMonth2Changed();
		}
	}
	private void onCategoryChanged() {
		String value = "";
		Resources r = getResources();
		if(pwCategory == ProduceWaterCategory.UnderWater) value = r.getString(R.string.under_water);
		if(pwCategory == ProduceWaterCategory.SurfaceWater) value = r.getString(R.string.surface_water);
		if(pwCategory == ProduceWaterCategory.TotalWater) value = r.getString(R.string.total_water);
		value = value + " - " + r.getString(R.string.query);
		titleTextView.setText(value);
		
		getData();
	}
	// on searchMode was changed
	private void onSearchModeChanged() {
		if(searchMode == SearchMode.Single) {
			date2Button.setVisibility(View.GONE);
			searchButton.setVisibility(View.GONE);
		}
		else {
			date2Button.setVisibility(View.VISIBLE);
			searchButton.setVisibility(View.VISIBLE);
		}
	}
	private void onSearchTypeChanged() {
		if(searchType == SearchType.Year) {
			onYear1Changed();
			onYear2Changed();
		}
		else {
			onMonth1Changed();
			onMonth2Changed();
		}
		getData();
	}
	
	private void onYear1Changed() {
		date1Button.setText(year1 + year);
	}
	private void onYear2Changed() {
		if(year2 == -1) 
			date2Button.setText(getResources().getString(R.string.choice_date));
		else
			date2Button.setText(year2 + year);
	}
	private void onMonth1Changed() {
		if(year1 != -1 && month1 != -1)
			date1Button.setText(year1 + year + month1 + month);
		else
			date1Button.setText(getResources().getString(R.string.choice_date));
	}
	private void onMonth2Changed() {
		if(year2 != -1 && month2 != -1)
			date2Button.setText(year2 + year + month2 + month);
		else
			date2Button.setText(getResources().getString(R.string.choice_date));
	}
	private void getData() {
		if(searchMode == SearchMode.Single) {
			if(searchType == SearchType.Year)
				getYearDataByThread();
			else
				getMonthDataByThread();
		}
		else {
			if(searchType == SearchType.Year) {
				if(year1 != -1 && year2 != -1) 
					getYearDataByThread();
			}
			else {
				if(year1 != -1 && month1 != -1
						&& year2 != -1 && month2 != -1) {
					getMonthDataByThread();
				}
			}
		}
	}
	
	Button.OnClickListener onBottomButtonClick = new Button.OnClickListener() {
		public void onClick(View v) {
			for(Button b : bottomButtons) {
				if(v.equals(b))
					b.setBackgroundResource(R.drawable.bottombar_selected_background);
				else
					b.setBackgroundResource(android.graphics.Color.TRANSPARENT);
			}
			switch(v.getId()) {
			case R.id.underWaterButton: setCategory(ProduceWaterCategory.UnderWater); break;
			case R.id.surfaceWaterButton: setCategory(ProduceWaterCategory.SurfaceWater); break;
			case R.id.totalWaterButton: setCategory(ProduceWaterCategory.TotalWater); break;
			}
		}
	};
	// compare button click listener
	Button.OnClickListener onButtonClick = new Button.OnClickListener() {
		public void onClick(View v) {
			changeButtonState(v);
			if(v.getId() == R.id.yearButton) {
				setSearchType(SearchType.Year);
			}
			else if(v.getId() == R.id.monthButton) {
				setSearchType(SearchType.Month);
			}

		}
	};
	// date1 button click listener
	Button.OnClickListener onDate1ButtonClick = new Button.OnClickListener() {
		public void onClick(View v) {
			if(searchType == SearchType.Year)
				showYearSelectedPopup("year1");
			else 
				showMonthSelectedPopup("month1");
		}
	};
	// date2 button click listener
	Button.OnClickListener onDate2ButtonClick = new Button.OnClickListener() {
		public void onClick(View v) {
			if(searchType == SearchType.Year)
				showYearSelectedPopup("year2");
			else 
				showMonthSelectedPopup("Month2");
		}
	};
	// search button click listener
	Button.OnClickListener onSearchButtonClick = new Button.OnClickListener() {
		public void onClick(View v) {
			if(searchType == SearchType.Year) {
				getYearDataByThread(year1, year2);
			}
			else {
				getMonthDataByThread(year1, month1, year2, month2);
			}
		}
	};	
	// date was selected
	Button.OnClickListener onDateButtonSelected = new Button.OnClickListener() {
		public void onClick(View v) {
			Button b = (Button)v;
			int year = Integer.parseInt(b.getText().toString());
			if("year1".equals(b.getTag().toString())) { 
				if(searchMode == SearchMode.Single) {
					getYearDataByThread(year, -1);
				}
				else {
					setYear1(year);
				}
			}
			else {
				// year2 was selected
				setYear2(year);
			}
			hidePopup();
		}
	};
	
	ArrayList<Button> yearButtons = new ArrayList<Button>();
	ArrayList<Button> monthButtons = new ArrayList<Button>();
	Button.OnClickListener onYearDateButtonSelected = new Button.OnClickListener() {
		public void onClick(View v) {
			for(Button b : yearButtons) {
				if(b.equals(v)) {
					b.setBackgroundColor(android.graphics.Color.GRAY);
					b.setTag(true);
				}
				else {
					b.setBackgroundColor(android.graphics.Color.DKGRAY);
					b.setTag(null);
				}
			}
		}
	};
    Button.OnClickListener onMonthDateButtonSelected = new Button.OnClickListener() {
		public void onClick(View v) {
			for(Button b : monthButtons) {
				if(b.equals(v)) {
					b.setBackgroundColor(android.graphics.Color.GRAY);
					b.setTag(true);
				}
				else {
					b.setBackgroundColor(android.graphics.Color.DKGRAY);
					b.setTag(null);
				}
			}
		}
	};
	
	
	private void showPopup() {
		popupLinearLayout.setVisibility(View.VISIBLE);
		AnimationHelper.fadeIn(this, popupLinearLayout);
	}
	private void hidePopup() {
		AnimationHelper.fadeOut(this, popupLinearLayout);
		popupLinearLayout.setVisibility(View.GONE);
	}
	// show year choice pop up
	private void showYearSelectedPopup(String yearString) {
		Date now = new Date();
		int currentYear = now.getYear() + 1900;
		
		popupContentLinearLayout.removeAllViews();
		for(int i = currentYear; i >= 2008; i--) {
			Button b = new Button(ProduceWaterActivity.this);
			b.setText("" + i);
			b.setTextSize(20);
			b.setTextColor(android.graphics.Color.WHITE);
			b.setPadding(10, 10, 10, 10);
			b.setBackgroundColor(android.graphics.Color.DKGRAY);
			b.setTag(yearString);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			b.setLayoutParams(params);
			if(i != 2008) params.bottomMargin = 1;
			popupContentLinearLayout.addView(b);
			
			b.setOnClickListener(onDateButtonSelected);
		}
		showPopup();
	}
	private void showMonthSelectedPopup(final String monthString) {
		
		Date now = new Date();
		int currentYear = now.getYear() + 1900;
		TableRow row = null;
		TableLayout.LayoutParams params = null;
		TableRow.LayoutParams rparams = null;
		
		popupContentLinearLayout.removeAllViews();
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.month_choice, null);
		TableLayout yearTL = (TableLayout)view.findViewById(R.id.yearTL);
		TableLayout monthTL = (TableLayout)view.findViewById(R.id.monthTL);
		Button okButton = (Button)view.findViewById(R.id.okButton);
		popupContentLinearLayout.addView(view);
		
		// year content
		yearButtons.clear();
		int count = currentYear - 2008;
		for(int i = count; i >= 0; i--) {
			if((i - count) % 3 == 0) {
				row = new TableRow(this);
				params = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				params.bottomMargin = 1;
				row.setLayoutParams(params);
				yearTL.addView(row);
			}
			Button b = new Button(this);
			b.setText("" + (2008 + i));
			b.setTextSize(20);
			b.setTextColor(android.graphics.Color.WHITE);
			b.setPadding(10, 10, 10, 10);
			b.setBackgroundColor(android.graphics.Color.DKGRAY);
			rparams = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			rparams.weight = 1;
			rparams.rightMargin = 1;
			b.setLayoutParams(rparams);
			b.setOnClickListener(onYearDateButtonSelected);
			
			yearButtons.add(b);
			row.addView(b);
		}
		
		// month content
		monthButtons.clear();
		for(int i = 0; i < 12; i++) {
			if(i % 4 == 0) {
				row = new TableRow(this);
				params = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				params.bottomMargin = 1;
				row.setLayoutParams(params);
				monthTL.addView(row);
			}
			Button b = new Button(this);
			b.setText("" + (i + 1));
			b.setTextSize(20);
			b.setTextColor(android.graphics.Color.WHITE);
			b.setPadding(10, 10, 10, 10);
			b.setBackgroundColor(android.graphics.Color.DKGRAY);
			rparams = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			rparams.weight = 1;
			rparams.rightMargin = 1;
			b.setLayoutParams(rparams);
			b.setOnClickListener(onMonthDateButtonSelected);
			
			monthButtons.add(b);
			row.addView(b);
		}
		
		
		okButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				int year = -1, month = -1;
				for(Button b : yearButtons) {
					if(b.getTag() != null) {
						year = Integer.parseInt(b.getText().toString());
					}
				}
				for(Button b : monthButtons) {
					if(b.getTag() != null) {
						month = Integer.parseInt(b.getText().toString());
					}
				}
				if(year != -1 && month != -1) {
					if("month1".equals(monthString)) {
						if(searchMode == SearchMode.Single) 
							getMonthDataByThread(year, month, -1, -1);
						else
							setMonth1(year, month);
					}
					else {
						setMonth2(year, month);
					}
					hidePopup();
				}
			}
		});
		showPopup();
	}
	
	// change year, month buttons' state
	private void changeButtonState(View v) {
		for(Button b : buttons) {
			if(v.equals(b))
				b.setBackgroundResource(R.drawable.pw_topbar_selected_background);
			else
				b.setBackgroundColor(android.graphics.Color.TRANSPARENT);
		}
	}
	
	private void beginGetData() {
		chart_progress_float.setVisibility(View.VISIBLE);
		data_progress_float.setVisibility(View.VISIBLE);
		
		AnimationHelper.fading(this, imageScrollView);
		AnimationHelper.fading(this, dataTableLayout);
	}
	private void endGetData() {
		data_progress_float.setVisibility(View.GONE);
		AnimationHelper.flyIn(this, dataTableLayout);
	}
	private void endGetChart() {
		chart_progress_float.setVisibility(View.GONE);
		AnimationHelper.flyIn(this, imageScrollView);
	}
	
	private int getProduceWaterCategoryValue() {
		if(pwCategory == ProduceWaterCategory.UnderWater) return 0;
		if(pwCategory == ProduceWaterCategory.SurfaceWater) return 1;
		if(pwCategory == ProduceWaterCategory.TotalWater) return 2;
		return 2;
	}
	// get data of year for single
	private void getYearDataByThread() {
		Date d = new Date();
		int year = d.getYear() + 1900;
		if(year1 != -1) year = year1;
		getYearDataByThread(year, year2);
	}
	// get data of year for multiply
	private void getYearDataByThread(final int year, final int compareYear) {
		beginGetData();
		setYear1(year);
		setYear2(compareYear);
		new Thread() {
			public void run() {
				ArrayList<ProduceWaterData> result = new ArrayList<ProduceWaterData>();
				try {
					int category = getProduceWaterCategoryValue(); 
					
					result = dm.getProduceWaterByYear(category, year, compareYear);
					Message msg = handler.obtainMessage();
					msg.what = YearDataReceived;
					msg.obj = result;
					handler.sendMessage(msg);
				}
				catch(XMLRPCException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	// get data of month for single
	private void getMonthDataByThread() {
		Date d = new Date();
		int year = d.getYear() + 1900;
		int month = d.getMonth() + 1;
		if(year1 != -1) year = year1;
		if(month1 != -1) month = month1;
		getMonthDataByThread(year, month, year2, month2);
	}
	private void getMonthDataByThread(final int year, final int month, final int compareYear, final int compareMonth) {
		beginGetData();
		setMonth1(year, month);
		setMonth2(compareYear, compareMonth);
		new Thread() {
			public void run() {
				ArrayList<ProduceWaterData> result = new ArrayList<ProduceWaterData>();
				try {
					int category = getProduceWaterCategoryValue(); 
					
					result = dm.getProduceWaterByMonth(category, year, month, compareYear, compareMonth);
					Message msg = handler.obtainMessage();
					msg.what = MonthDataReceived;
					msg.obj = result;
					handler.sendMessage(msg);
				}
				catch(XMLRPCException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	// clear rows of table
	private void clearTableRows() {
		dataTableLayout.removeAllViews();
	}
	// display data of year for single
	private void displayDataForSingle(ArrayList<ProduceWaterData> data) {
		clearTableRows();
		
		TableRow row = null;
		int columnCount = searchType == SearchType.Year? 2: 3;
		String dateString = searchType == SearchType.Year ? month: day;
		
		// create content
		for(int i = 0; i < data.size(); i++) {
			ProduceWaterData d = data.get(i);
			if(i % columnCount == 0) {
				 row = new TableRow(this);
				 row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				 row.setPadding(0, 0, 0, 3);
				 dataTableLayout.addView(row);
			}
			
			TextView tv = new TextView(this);
			String text = d.time + dateString + ":" + ((d.value == -1) ? "-": d.value);
			if(d.value == -1)
				tv = createRedContent(text);
			else
				tv = createGreenContent(text);
			row.addView(tv);
		}
	}
	// display data of year for multiply
	private void displayDataForMulti(ArrayList<ProduceWaterData> data) {
		clearTableRows();
		
		String dateHeader = getResources().getString(R.string.month_name);
		String date1Header = year1 + year;
		String date2Header = year2 + year;
		
		if(searchType == SearchType.Month) {
			dateHeader = day;
			date1Header = year1 + year + month1 + month;
			date2Header = year2 + year + month2 + month;
		}
		
		TableRow row = null;
		TextView tv = null;
		TableRow.LayoutParams params = null;
		
		// create header
		row = new TableRow(this);
		row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		row.setPadding(0, 0, 0, 3);
		
		// space
		tv = new TextView(this);
		tv.setText(dateHeader);
		params = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		params.weight = 1;
		tv.setLayoutParams(params);
		row.addView(tv);
		
		// year1
		tv = new TextView(this);
		tv.setText(date1Header);
		tv.setTextColor(android.graphics.Color.WHITE);
		params = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		params.weight = 1;
		tv.setLayoutParams(params);
		row.addView(tv);
		
		// year2
		tv = new TextView(this);
		tv.setText(date2Header);
		tv.setTextColor(android.graphics.Color.WHITE);
		params = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		params.weight = 1;
		tv.setLayoutParams(params);
		row.addView(tv);
		
		// space
		tv = new TextView(this);
		tv.setText(getResources().getString(R.string.diff_value));
		params = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		params.weight = 1;
		tv.setLayoutParams(params);
		row.addView(tv);
		dataTableLayout.addView(row);

		// create content
		for(int i = 0; i < data.size(); i++) {
			ProduceWaterData d = data.get(i);

			row = new TableRow(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 3);
			dataTableLayout.addView(row);
			
			// month
			tv = createBlueContent("" + d.time);
			row.addView(tv);
			
			// value
			tv = createGreenContent(d.value == -1 ? "-": "" + d.value);
			row.addView(tv);

			// value1
			tv = createRedContent(d.value1 == -1 ? "-": "" + d.value1);
			row.addView(tv);

			// different
			int diff = -1;
			if(d.value != -1 && d.value1 != -1) {
				diff = d.value1 - d.value;
				if(diff < 0)
					tv = createRedContent("" + diff);
				else
					tv = createBlueContent("" + diff);
			}
			else {
				tv = createBlueContent("-");
			}
			row.addView(tv);
		}
	}
	// generates green content
	private TextView createGreenContent(String text) {
		return createContent(text, R.drawable.detail_content_background);
	}
	// generates blue content
	private TextView createBlueContent(String text) {
		return createContent(text, R.drawable.detail_blue_content_background);
	}
	// generates red content
	private TextView createRedContent(String text) {
		return createContent(text, R.drawable.detail_red_content_background);
	}
	// generates a content for text view
	private TextView createContent(String text, int backgroundResId) {
		TextView tv = new TextView(this);
		tv.setText(text);
		TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = 3;
		params.weight = 1;
		params.gravity = Gravity.CENTER_VERTICAL;
		tv.setLayoutParams(params);
		tv.setBackgroundResource(backgroundResId);
		return tv;
	}
	
    private void updateChartByThread(final ArrayList<ProduceWaterData> data) {
    	new Thread(new Thread() {
    		public void run() {
		    	String url = createChart(data);
		    	if(!"".equals(url))	{
			    	Message msg = handler.obtainMessage();
			    	msg.what = UpdatedChart;
		    		msg.obj = getImage(url);
		    		handler.sendMessage(msg);
		    	}
    		}
    	}).start();
    }
    private Bitmap getImage(String urlString) {
    	Bitmap bm = null;
    	try {
    		URL url = new URL(urlString);
    		URLConnection conn = url.openConnection();
    		conn.connect();
    		InputStream is = conn.getInputStream();
    		BufferedInputStream bis = new BufferedInputStream(is);
    		bm = BitmapFactory.decodeStream(bis);
    		bis.close();
    		is.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return bm;
    }
    /*private int getMinValue(ArrayList<ProduceWaterData> data) {
    	int result  = 0;
    	for(ProduceWaterData d : data) {
    		int min = getMin(d.value, d.value1);
    		if(result == 0) {
    			result = min;
    		} 
    		else {
        		if(min != -1) {
        			result = getMin(result, min);
        		}    			
    		}
    	}
    	return result;
    }
    private int getMin(int value1, int value2) {
    	if(value1 == -1) {
    		return value2;
    	}
    	else if(value2 == -1) {
    		return value1;
    	}
    	else {
    		return value1 < value2 ? value1 : value2;
    	}
    }*/
    private int getMaxValue(ArrayList<ProduceWaterData> data) {
    	int result = 0;
    	for(ProduceWaterData d : data) {
    		int max = getMax(d.value, d.value1);
    		result = getMax(result, max);
    	}
    	return result;
    }
    private int getMax(int value1, int value2) {
    	if(value1 == -1) {
    		return value2;
    	}
    	else if(value2 == -1) {
    		return value1;
    	}
    	else {
    		return value1 > value2? value1 : value2;
    	}
    		
    }
    // create year chart
    private String createChart(ArrayList<ProduceWaterData> data) {
        // Defining lines
        final int NUM_POINTS = data.size();
        if(data.size() == 0) return "";
        
        //final double[] competition = new double[NUM_POINTS];
        final double[] values = new double[NUM_POINTS];
        final double[] values2 = new double[NUM_POINTS];
        for (int i = 0; i < NUM_POINTS; i++) {
        	int value = data.get(i).value;
        	int value1 = data.get(i).value1;
            values[i] = value == -1? 0: value;
            values2[i] = value1 == -1? 0: value1;
        }
    	
        //int min = getMinValue(data);
        int max = getMaxValue(data);

        if(max == 0) max = 1;
        BarChartPlot bar1 = Plots.newBarChartPlot(DataUtil.scaleWithinRange(0, max, values), Color.GREEN, "");
        BarChartPlot bar2 = null;
        if(searchMode == SearchMode.Multi)
        	bar2 = Plots.newBarChartPlot(DataUtil.scaleWithinRange(0, max, values2), Color.RED, "");
        //bar1.setFillAreaColor(Color.newColor("0f4287"));

        // Defining chart.
        BarChart chart = null;
        if(searchMode == SearchMode.Single)
        	chart = GCharts.newBarChart(bar1);
        else
        	chart = GCharts.newBarChart(bar1, bar2);
        //chart.setGrid(10, 10, 3, 2);

        // Defining axis info and styles
        AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.WHITE, 20, AxisTextAlignment.RIGHT);

        AxisLabels xAxis = null;
        if(searchType == SearchType.Year) {
        	String[] yearLabels = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
        	xAxis = AxisLabelsFactory.newAxisLabels(yearLabels);
        }
        else {
        	String[] monthLabels = new String[NUM_POINTS];
        	for(int i = 0; i < NUM_POINTS; i++) {
        		monthLabels[i] = "" + data.get(i).time;
        	}
        	xAxis = AxisLabelsFactory.newAxisLabels(monthLabels);
        }
        int fontSize = searchType == SearchType.Year? 20: 16;
        xAxis.setAxisStyle(AxisStyle.newAxisStyle(Color.WHITE, fontSize, AxisTextAlignment.CENTER));

        AxisLabels yAxis = AxisLabelsFactory.newNumericRangeAxisLabels(0, max);
        yAxis.setAxisStyle(axisStyle);
        // Adding axis info to chart.
        chart.addXAxisLabels(xAxis);
        chart.addYAxisLabels(yAxis);

        //if(searchMode == SearchMode.Multi && searchType == SearchType.Year)
        	//chart.setBarWidth(metrics.widthPixels / 50);
        chart.setBarWidth(BarChart.AUTO_RESIZE);
        //chart.setSpaceBetweenGroupsOfBars(15);
        chartWidth = searchType == SearchType.Month ? metrics.widthPixels * 3 / 2 : metrics.widthPixels;
        chartWidth = chartWidth - 10;
        chart.setSize(chartWidth, 320);

        // Defining background and chart fills.
        LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("363433"), 100);
        fill.addColorAndOffset(Color.newColor("2E2B2A"), 0);
        chart.setBackgroundFill(Fills.newSolidFill(Color.newColor(Color.BLACK, 1)));
        chart.setMargins(5, 5, 5, 5);

        return chart.toURLString();
//		return "";
    }
	
    public enum ProduceWaterCategory {
    	UnderWater,
    	SurfaceWater,
    	TotalWater
    }
	public enum SearchMode {
		Single,
		Multi
	}
	public enum SearchType {
		Year,
		Month
	}
}
