package com.bdwater.dispatching;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.xmlrpc.android.XMLRPCException;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.LinearGradientFill;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Shape;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Detail extends Activity {
	DataManager dataManager;
	TextView dateTextView;
	TextView amountTitleTextView;
	TextView titleTextView;
	TextView amountTextView;
	TextView averageTextView;
	TextView minTextView;
	TextView maxTextView;
	TextView countTextView;
	
	ImageView imageView;
	ListView listView;
	
	LinearLayout progressLinearLayout;
	TextView progressMessageTextView;
	
	ArrayList<Button> buttons = new ArrayList<Button>();
	Button todayButton;
	Button yesterdayButton;
	Button choiceDateButton;
	
	Button chartButton;
	Button dataButton;
	
	String dataTagIdString;
	ArrayList<SampleData> data;
	Handler handler;
	Thread thread;
	
	String currentDate = "";
	DisplayMetrics metrics = new DisplayMetrics();

	String today = "";
	String yesterday = "";
	String today_space = "";
	String yesterday_space = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.detail);
    	
    	dataManager = new DataManager();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);

    	today = getResources().getString(R.string.today);
    	yesterday = getResources().getString(R.string.yesterday);
    	today_space = " " + today + " ";
    	yesterday_space = " " + yesterday + " ";

    	initializeControls();
    	initializeHandler();
    	
    	getValuesByThread(0);
    }
    private void initializeControls() {
    	progressLinearLayout = (LinearLayout)findViewById(R.id.progressLinearLayout);
    	progressMessageTextView = (TextView)findViewById(R.id.progress_messageTextView);
    	
    	titleTextView = (TextView)findViewById(R.id.titleTextView);
    	dateTextView = (TextView)findViewById(R.id.dateTextView);
    	
    	amountTitleTextView = (TextView)findViewById(R.id.amountTitleTextView);
    	
    	amountTextView = (TextView)findViewById(R.id.amountTextView);
    	averageTextView = (TextView)findViewById(R.id.averageTextView);
    	minTextView = (TextView)findViewById(R.id.minTextView);
    	maxTextView = (TextView)findViewById(R.id.maxTextView);
    	countTextView = (TextView)findViewById(R.id.countTextView);
    	
    	imageView = (ImageView)findViewById(R.id.imageView);
    	listView = (ListView)findViewById(R.id.listView);
    	
    	todayButton = (Button)findViewById(R.id.todayButton);
    	todayButton.setText(today_space);
    	todayButton.setOnClickListener(OnButtonClick);
    	yesterdayButton = (Button)findViewById(R.id.yesterdayButton);
    	yesterdayButton.setText(yesterday_space);
    	yesterdayButton.setOnClickListener(OnButtonClick);
    	choiceDateButton = (Button)findViewById(R.id.choiceDateButton);
    	choiceDateButton.setText(" " + getResources().getString(R.string.choice_date) + " ");
    	choiceDateButton.setOnClickListener(OnButtonClick);
    	buttons.add(todayButton);
    	buttons.add(yesterdayButton);
    	buttons.add(choiceDateButton);
    	
    	chartButton = (Button)findViewById(R.id.chartButton);
    	chartButton.setText(getResources().getString(R.string.chart));
    	chartButton.setPadding(0, 10, 10, 0);
    	chartButton.setCompoundDrawablePadding(-8);
    	chartButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.building, 0, 0);
    	chartButton.setTextSize(12);
    	chartButton.setOnClickListener(OnButtonClick);
    	
    	dataButton = (Button)findViewById(R.id.dataButton);
    	dataButton.setText(getResources().getString(R.string.data));
    	dataButton.setPadding(0, 10, 10, 0);
    	dataButton.setCompoundDrawablePadding(-8);
    	dataButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.building, 0, 0);
    	dataButton.setTextSize(12);
    	dataButton.setOnClickListener(OnButtonClick);
    	
    	Bundle bundle = this.getIntent().getExtras();
    	titleTextView.setText(bundle.getString("dataTagName"));
    	dataTagIdString = bundle.getString("dataTagId");
    }
    private void initializeHandler() {
    	handler = new Handler() {
    		@SuppressWarnings("unchecked")
			@Override
    		public void handleMessage(Message msg) {
    			switch(msg.what)
    			{
    			case 1:
    				String amountTitle = getResources().getString(R.string.amount);
    				dateTextView.setText(currentDate);
    				
    				amountTitleTextView.setText(amountTitle);
    				data = (ArrayList<SampleData>)msg.obj;
	    			
	    	    	amountTextView.setText(getResources().getString(R.string.total_value) + roundDouble(getAmount()));
	    	    	averageTextView.setText(getResources().getString(R.string.average_value) + roundDouble(getAverage()));
	    	    	minTextView.setText(getResources().getString(R.string.min_value) + roundDouble(getMin()));
	    	    	maxTextView.setText(getResources().getString(R.string.max_value) + roundDouble(getMax()));
	    	    	countTextView.setText(getResources().getString(R.string.sample_value) + getCount());
	    	    	
	    	    	List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
	    	    	for(SampleData d: data) {
	    	    		HashMap<String, String> l = new HashMap<String, String>();
	    	    		l.put("receivedTime", d.receivedTime);
	    	    		l.put("value", d.value.toString());
	    	    		list.add(l);
	    	    	}
	    	    	SimpleAdapter adapter = new SimpleAdapter(Detail.this, list,
	    	    			R.layout.listview_sampledata_item,
	    	    			new String[] { "receivedTime", "value" },
	    	    			new int[] { R.id.receivedTimeTextView, R.id.valueTextView});
	    	    	listView.setAdapter(adapter);
	    	    	
	    	    	progressMessageTextView.setText(getResources().getString(R.string.loading_message));
	    	    	thread = new Thread(new Runnable() {
	    	    		public void run() {
	    	    			updateChart();
	    	    		}
	    	    	});
	    	    	thread.start();
	    	    	break;
    			case 2:
    				if(null != msg.obj) {
    					Bitmap bm = (Bitmap)msg.obj;
    					imageView.setImageBitmap(bm);
//    					imageView.setVisibility(0);
						imageView.setVisibility(View.VISIBLE);
    				}
    				progressLinearLayout.setVisibility(View.GONE);
    	    		break;
    			}
    			for(Button b : buttons) { b.setEnabled(true); }
    		}
    	};
    }
    private void getValuesByThread(final String startTime, final String endTime) {
    	thread = new Thread(new Runnable() {
    		public void run() {
    			updateValues(startTime, endTime);
    		}
    	});
    	for(Button b: buttons) { b.setEnabled(false); }
    	
    	amountTextView.setText("-");
    	averageTextView.setText("-");
    	minTextView.setText("-");
    	maxTextView.setText("-");
    	countTextView.setText("-");
    	
    	imageView.setVisibility(View.INVISIBLE);
    	listView.setVisibility(View.GONE);
    	progressLinearLayout.setVisibility(View.VISIBLE);
    	thread.start();
    }
    private void getValuesByThread(final int mode) {
    	thread = new Thread(new Runnable() {
    		public void run() {
    			if(mode == 0) {
    				currentDate = today;
    				updateTodayValues();
    			}
    			else if(mode == 1) {
    				currentDate = yesterday;
    				updateYesterdayValues();
    			}
    		}
    	});
    	for(Button b: buttons) { b.setEnabled(false); }
    	
    	amountTextView.setText("-");
    	averageTextView.setText("-");
    	minTextView.setText("-");
    	maxTextView.setText("-");
    	countTextView.setText("-");
    	
    	imageView.setVisibility(View.INVISIBLE);
    	listView.setVisibility(View.GONE);
    	progressLinearLayout.setVisibility(View.VISIBLE);
    	thread.start();
    }
    
    private Button.OnClickListener OnButtonClick = new Button.OnClickListener() {
    	public void onClick(View view) {
    		int i = view.getId();
    		switch(i) {
    		case R.id.chartButton:
    			chartButton.setBackgroundResource(R.drawable.bottombar_selected_background);
    			dataButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    			
    			imageView.setVisibility(View.VISIBLE);
    			listView.setVisibility(View.GONE);
    			break;
    		case R.id.dataButton:
    			dataButton.setBackgroundResource(R.drawable.bottombar_selected_background);
    			chartButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    			
    			imageView.setVisibility(View.GONE);
    			listView.setVisibility(View.VISIBLE);
    			break;
    		case R.id.todayButton:
    		case R.id.yesterdayButton:
    			Button button = (Button)view;
    			changeTopButtonState(button);
    			if(i == R.id.todayButton) 
    				getValuesByThread(0);	// get values at today
    			else if(i == R.id.yesterdayButton)
    				getValuesByThread(1);	// get values at yesterday
    			break;
    		case R.id.choiceDateButton:
    			showChoiceDateDialog(); 	// show choice date dialog			
    			break;
    		}
    	}
    };
    private void showChoiceDateDialog() {
    	final DatePicker picker = new DatePicker(this);
    	new AlertDialog.Builder(this)
    		.setTitle(getResources().getString(R.string.search))
    		.setMessage(getResources().getString(R.string.choice_date))
    		.setView(picker)
    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				    Calendar dialogSelectedDate = Calendar.getInstance();
					Date date = new Date(picker.getYear() - 1900, picker.getMonth(), picker.getDayOfMonth());
					dialogSelectedDate.setTime(date);
					
					choosenDate(dialogSelectedDate);
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			})
			.show();
    }
    private void choosenDate(Calendar calendar) {
    	changeTopButtonState(this.choiceDateButton);
    	
    	// validate selected date
		String nowString = DateFormat.getDateInstance().format(new Date().getTime());
		String choosenString = DateFormat.getDateInstance().format(calendar.getTime());
		Date now = new Date();
		Date current = calendar.getTime();
		try {
			now = new SimpleDateFormat("yyyy-MM-dd").parse(nowString);
			current = new SimpleDateFormat("yyyy-MM-dd").parse(choosenString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(current.after(now))	{
			Toast.makeText(Detail.this, getResources().getString(R.string.further), Toast.LENGTH_LONG).show();
			return;
		}

		// get start and end time
		String dateString = (calendar.get(Calendar.YEAR)) + "/" + 
				(calendar.get(Calendar.MONTH) + 1) + "/" +
				calendar.get(Calendar.DAY_OF_MONTH);
		String startTime = dateString + " " + "00:00:00";
		String endTime = dateString + " " + "23:59:59";
		
		currentDate = dateString;
		// get values
		this.getValuesByThread(startTime, endTime);
    }
    private void changeTopButtonState(View v) {
		for(Button b : buttons) {
			if(b.equals(v)) 
				b.setBackgroundResource(R.drawable.detail_green_button_selected_background);
			else
				b.setBackgroundResource(R.drawable.detail_green_button_background);
		}
    }
    private void updateTodayValues() {
    	Calendar c = Calendar.getInstance();
    	String todayString = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH); 
    	String startTimeString = todayString + " 0:00:00";
    	String endTimeString = todayString + " 23:59:59";
    	updateValues(startTimeString, endTimeString);
    }
    private void updateYesterdayValues() {
    	Calendar c = Calendar.getInstance();
    	c.add(Calendar.DAY_OF_MONTH, -1);
    	String todayString = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH); 
    	String startTimeString = todayString + " 0:00:00";
    	String endTimeString = todayString + " 23:59:59";
    	updateValues(startTimeString, endTimeString);
    }
    private void updateValues(String startTimeString, String endTimeString) {
    	ArrayList<SampleData> result = this.getValues(dataTagIdString, startTimeString, endTimeString);

    	Message msg = handler.obtainMessage();
    	msg.what = 1;
    	msg.obj = result;
    	handler.sendMessage(msg);
    }
    
    private void updateChart() {
    	String url = createChart();
    	
    	Message msg = handler.obtainMessage();
    	msg.what = 2;
    	if(!"".equals(url))	msg.obj = getImage(url);
    	handler.sendMessage(msg);
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
    private ArrayList<SampleData> getValues(String dataTagId, String startTimeString, String endTimeString) {
    	ArrayList<SampleData> result = new ArrayList<SampleData>();
    	try {
    		result = dataManager.getSampleData(dataTagId, startTimeString, endTimeString);
    	}
    	catch(XMLRPCException e) {
    		e.printStackTrace();
    	}
    	return result;
    }
    private Double roundDouble(Double value) {
    	BigDecimal v = new BigDecimal(value);
    	BigDecimal rounded = v.setScale(2, BigDecimal.ROUND_HALF_UP);
    	return rounded.doubleValue();
    }
    private Double getMin() {
    	Double min = 0d;
    	if(data.size() > 0) min = data.get(data.size() - 1).value;
    	for(SampleData sd : data) {
    		if(sd.value < min) min = sd.value;
    	}
    	return min;
    }
    private Double getMax() {
    	Double max = 0d;
    	for(SampleData sd : data) {
    		if(sd.value > max) max = sd.value;
    	}
    	return max;
    }
    private Double getAmount() {
    	Double amount = 0d;
    	for(SampleData sd : data) {
    		amount += sd.value;
    	}
    	return amount;
    }
    private Double getAverage() {
    	if(getCount() == 0) 
    		return 0d;
    	else {
    		Double amount = getAmount();
    		if(amount == 0) 
    			return 0d;
    		else
    			return amount / getCount();
    	}
    }
    private int getCount() {
    	return data.size();
    }
    private String createChart() {
        // Defining lines
        final int NUM_POINTS = data.size();
        if(data.size() == 0) return "";
        
        //final double[] competition = new double[NUM_POINTS];
        final double[] values = new double[NUM_POINTS];
        for (int i = 0; i < NUM_POINTS; i++) {
            values[i] = data.get(i).value; 
        }
    	Double min = getMin();
    	Double max = getMax() * 1.1;
    	
    	if(min == 0 && max == 0) return "";
    	if(min == max) return "";
    	
        Line line1 = Plots.newLine(DataUtil.scaleWithinRange(min, max, values), Color.SKYBLUE, "");
        line1.setLineStyle(LineStyle.newLineStyle(3, 1, 0));
        line1.addShapeMarkers(Shape.DIAMOND, Color.SKYBLUE, 8);
        line1.addShapeMarkers(Shape.DIAMOND, Color.WHITE, 5);
        line1.setFillAreaColor(Color.newColor("0f4287"));

        // Defining chart.
        LineChart chart = GCharts.newLineChart(line1);
        chart.setSize(metrics.widthPixels, 320);
        //chart.setTitle("Sample Total", Color.WHITE, 24);
        chart.addHorizontalRangeMarker(40, 60, Color.newColor(Color.RED, 30));
        chart.addVerticalRangeMarker(40, 60, Color.newColor(Color.GREEN, 30));
        chart.setGrid(10, 10, 3, 2);

        // Defining axis info and styles
        AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.WHITE, 20, AxisTextAlignment.RIGHT);
        axisStyle.setDrawTickMarks(true);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ss = "";
        String se = "";
        Date start, end;
        try {
        	ss = data.get(0).receivedTime;
        	se = data.get(data.size() - 1).receivedTime;

        	start = (Date)format.parse(ss);
        	end = (Date)format.parse(se);

        	ss = start.getHours() + ":" + start.getMinutes();
        	se = end.getHours() + ":" + end.getMinutes();
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        AxisLabels xAxis = AxisLabelsFactory.newAxisLabels(ss, se);
        xAxis.setAxisStyle(axisStyle);

        AxisLabels yAxis = AxisLabelsFactory.newNumericRangeAxisLabels(min, max);
        yAxis.setAxisStyle(axisStyle);
        // Adding axis info to chart.
        chart.addXAxisLabels(xAxis);
        chart.addYAxisLabels(yAxis);

        // Defining background and chart fills.
        LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("363433"), 100);
        fill.addColorAndOffset(Color.newColor("2E2B2A"), 0);
        chart.setBackgroundFill(Fills.newSolidFill(Color.newColor(Color.BLACK, 1)));
        chart.setMargins(5, 5, 5, 5);

        return chart.toURLString();
//		return "";
    }
}
