package com.spreadit.radar;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.spreadit.R;
import com.spreadit.network.ComManager;
import com.spreadit.network.MessageReceiver;
import com.spreadit.radar.CirclesCanvasAnimation.Circle;
import com.spreadit.utils.ActionItem;
import com.spreadit.utils.ClearCacheDataUtils;
import com.spreadit.utils.QuickAction;

public class RadarActivity extends Activity 
{
	private SlidingUpPanelLayout 	mHistoryLayout;

	private RelativeLayout 			mainContent;

	private EditText 				mNewMsg;

	private ListView 				mHistoryList;

	private ListView				mFilteredHistoryList;
	
	private ArrayAdapter<String>	mHistoryAdapter;

	private boolean 				bDisplayMsg;

	private Map<String, Button>		mCloseUsers;

	private ComManager				mComManager;
	
	private CanvasAnimationView		mAnimationView;
	
	private CirclesCanvasAnimation	mCirclesAnimation;
	
	private final BroadcastReceiver messageReceiver = new MessageReceiver(); 

	private float					mYNewMsg;
	
	private boolean					bClickedOnce;

	public 	static final String 	SAVED_STATE_ACTION_BAR_HIDDEN 	= "saved_state_action_bar_hidden";
	private static final String 	ACTION_BAR_TITLE_FONT 			= "fonts/intriquescript.ttf";
	private static final String		KEY_LATITUDE					= "latitude";
	private static final String		KEY_LONGITUDE					= "longitude";
	private static final String		KEY_SERVERID					= "server_id";
	private static final String		KEY_MESSAGE						= "msg";
	private static final String		KEY_NEWUSER						= "new_user";
	private static final String 	KEY_LOSTUSER 					= "lost_user";
	private static final float 		ACTION_BAR_TITLE_SIZE 			= 37.0f;
	private static final float 		SCREEN_X_MAX 					= 1000.0f;
	private static final float		SCREEN_Y_MAX					= 1220.0f;
	private static final int		WAVE_DURATION					= 5000;
	private static final int		CLOSE_USER_DIM					= 80;  //dp
	private static final int		NOTIFICATION_DIM				= 140;  //dp

	private static final int[] btnCenterBg = { R.drawable.btn_add_msg, R.drawable.btn_send_msg };

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radar);

		mComManager = ComManager.getInstance();
		mComManager.setMainAct(this);
		// AlarmManager pour réception des users
		mComManager.startUsersAlarmManager();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.google.android.c2dm.intent.RECEIVE");
		registerReceiver(messageReceiver, filter);

		mainContent = (RelativeLayout) findViewById(R.id.mainContent);
		
		mAnimationView = (CanvasAnimationView) findViewById(R.id.wave_view);
		mCirclesAnimation = new CirclesCanvasAnimation(WAVE_DURATION, true);
		
		Button btnNewMsg = (Button) findViewById(R.id.btnNewMsg);
		btnNewMsg.setY(mAnimationView.getHeight() + getActionBarHeight() / 2);

		buildActionBar();
		buildHistory();

		mCloseUsers = new HashMap<String, Button>();
		buildCloseUsers();

		mNewMsg = (EditText) findViewById(R.id.txtNewMsg);
		mNewMsg.setVisibility(View.GONE);

		bDisplayMsg = false;
		bClickedOnce = false;

		boolean actionBarHidden = savedInstanceState != null && savedInstanceState.getBoolean(SAVED_STATE_ACTION_BAR_HIDDEN, false);
		if (actionBarHidden) 
		{
			int actionBarHeight = getActionBarHeight();
			setActionBarTranslation(-actionBarHeight);//will "hide" an ActionBar
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Typeface tF = Typeface.createFromAsset(getAssets(), ACTION_BAR_TITLE_FONT);

		TextView txt = (TextView) findViewById(R.id.historyTxt);
		txt.setTypeface(tF);

		mainContent = (RelativeLayout) findViewById(R.id.mainContent);
		final Button btnNewMsg = (Button) findViewById(R.id.btnNewMsg);
		final Activity current = this;
		mYNewMsg = mNewMsg.getY();
		btnNewMsg.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				if( !bDisplayMsg )
				{
					mNewMsg.setVisibility(View.VISIBLE);
					if(mYNewMsg < 0.1f)
					{
						mYNewMsg = mNewMsg.getY() + 40.0f;
						mNewMsg.setY(mYNewMsg);
					}
					else
					{
						if( !bClickedOnce )
						{
							mYNewMsg = mNewMsg.getY();
							bClickedOnce = true;
						}
						mNewMsg.setY(mYNewMsg + 40.0f);
					}
					// setBackground not present in Android 4.0.3, changed to setBackgroundDrawable
					btnNewMsg.setBackgroundDrawable(getResources().getDrawable(btnCenterBg[1]));
					bDisplayMsg = true;

					mHistoryLayout.hidePanel();
				}
				else
				{
					if( mNewMsg.getText().toString().equals("") )
					{
						Toast emptyMsg = Toast.makeText(getBaseContext(), "Le message est vide.", Toast.LENGTH_SHORT);
						emptyMsg.show();
					}

					mNewMsg.clearFocus();
					mNewMsg.setVisibility(View.GONE);
					// force keyboard closure
					InputMethodManager imm = (InputMethodManager) current.getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm.isAcceptingText())
					{
						imm.hideSoftInputFromWindow(mNewMsg.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
					if( !mNewMsg.getText().toString().equals("") )
					{
						//send the message
						mComManager.sendMessage(mNewMsg.getText().toString());
						mNewMsg.setText("");
						//start the wave
						launchCircleAnimation(v);
					}
					//at end of wave trigger change of button
					// setBackground not present in Android 4.0.3, changed to setBackgroundDrawable
					btnNewMsg.setBackgroundDrawable(getResources().getDrawable(btnCenterBg[0]));
					bDisplayMsg = false;

					mHistoryLayout.showPanel();
				}
			}
		});
	}

	private void buildActionBar()
	{
		ActionBar actionBar = getActionBar();
		//actionBar.setCustomView(R.layout.actionbar);
		actionBar.setLogo(R.drawable.logo);
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView) findViewById(titleId);
		abTitle.setTextSize(ACTION_BAR_TITLE_SIZE);
		abTitle.setPadding(180, 0, 0, 0);
		Typeface tF = Typeface.createFromAsset(getAssets(), ACTION_BAR_TITLE_FONT);
		abTitle.setTypeface(tF);
	}


	private void buildHistory()
	{
		mHistoryLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mHistoryList = (ListView) findViewById(R.id.historyList);
		//mHistoryList.setAdapter(mHistoryListAdapter); TODO: create an adapter
		mHistoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mHistoryList.setAdapter(mHistoryAdapter);

		final Button btnFilter = (Button) findViewById(R.id.historyFilter);
		btnFilter.setVisibility(View.GONE);

		final TextView historyTxt = (TextView) findViewById(R.id.historyTxt);
		final int PADDING_BAR = 128;
		historyTxt.setPadding(PADDING_BAR, 0, 0, 0);

		mHistoryLayout.setPanelSlideListener(new PanelSlideListener() 
		{
			@Override
			public void onPanelSlide(View panel, float slideOffset) 
			{
				setActionBarTranslation(mHistoryLayout.getCurrentParalaxOffset());
			}

			@Override
			public void onPanelExpanded(View panel) 
			{
				Drawable logo = getResources().getDrawable(R.drawable.logo);
				final int DRAWABLE_BOUND = 128;
				logo.setBounds(0, 0, DRAWABLE_BOUND, 96);
				historyTxt.setCompoundDrawables(logo, null, null, null);

				final int PADDING_BAR_2 = 60;
				historyTxt.setPadding(PADDING_BAR_2, 0, PADDING_BAR_2, 0);

				btnFilter.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPanelCollapsed(View panel) 
			{
				historyTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				historyTxt.setPadding(PADDING_BAR, 0, 0, 0);

				btnFilter.setVisibility(View.GONE);
			}

			@Override
			public void onPanelAnchored(View panel) 
			{}

			@Override
			public void onPanelHidden(View panel) 
			{
				historyTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		});

		btnFilter.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				//Set the filter visible or not
				mFilteredHistoryList = (ListView) findViewById(R.id.filteredHistoryList);
				Button filterBtn = (Button) findViewById(R.id.historyFilter);
				LinearLayout selectHashtagsArea = (LinearLayout) findViewById(R.id.selectHashtagsLayout);
				if(selectHashtagsArea.getVisibility() == View.GONE) {
					Toast.makeText(getApplicationContext(), "Filters on", Toast.LENGTH_SHORT).show();
					selectHashtagsArea.setVisibility(View.VISIBLE);
					filterBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.filters_on));
					mHistoryList.setVisibility(View.GONE);
					mFilteredHistoryList.setVisibility(View.VISIBLE);
				}
				else {
					Toast.makeText(getApplicationContext(), "Filters off", Toast.LENGTH_SHORT).show();
					selectHashtagsArea.setVisibility(View.GONE);
					filterBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.filter));
					mHistoryList.setVisibility(View.VISIBLE);
					mFilteredHistoryList.setVisibility(View.GONE);
				}
				
				//Set the tags in the dropdown list
				setTagsInSpinner();
				
				//Filter on value selection
				final Spinner s = (Spinner) findViewById(R.id.listTagsSpinner);
				s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { 
				        // Your code here
				    	filterList(s.getSelectedItem().toString());
				    } 

				    public void onNothingSelected(AdapterView<?> adapterView) {
				        return;
				    } 
				}); 
			}
		});
	}

	private void buildCloseUsers() 
	{
		mCloseUsers.clear();
		int i;
		for(i = 0; i < mComManager.getUsers().size(); ++i)
		{
			View v = mainContent.findViewWithTag("userView" + i);
			if(v != null)
			{
				mainContent.removeView(v);
			}
			addNewCloseUser(mComManager.getUsers().get(i).toString(), i);
		}
	}
	
	private void addNewCloseUser(String serverId, int tag)
	{
		Button user = new Button(getBaseContext());
		// setBackground not present in Android 4.0.3, changed to setBackgroundDrawable
		user.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_close_user));
		user.setLayoutParams(new LayoutParams(CLOSE_USER_DIM, CLOSE_USER_DIM));

		Random fate = new Random();
		user.setX(fate.nextInt(Math.round(SCREEN_X_MAX)));
		user.setY(fate.nextInt(Math.round(SCREEN_Y_MAX)));
		final float MIDDLE_SCREEN_X = SCREEN_X_MAX / 2.0f;
		while( (user.getX() > (MIDDLE_SCREEN_X - 120.0f) && user.getX() < (MIDDLE_SCREEN_X + 120.0f)) 
				&& (user.getY() > 520.0f && user.getY() < 800.0f) )
		{
			user.setX(fate.nextInt(Math.round(SCREEN_X_MAX)));
			user.setY(fate.nextInt(Math.round(SCREEN_Y_MAX)));
		}
		user.setTag("userView" + tag);
		mainContent.addView(user);
		mCloseUsers.put(serverId, user);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(SAVED_STATE_ACTION_BAR_HIDDEN, mHistoryLayout.isPanelExpanded());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.radar, menu);
		MenuItem item = menu.findItem(R.id.action_toggle);
		if (mHistoryLayout != null) 
		{
			if (mHistoryLayout.isPanelHidden()) 
			{
				item.setTitle(R.string.action_show);
			} else {
				item.setTitle(R.string.action_hide);
			}
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId())
		{
		case R.id.action_toggle: 
		{
			if (mHistoryLayout != null) {
				if (!mHistoryLayout.isPanelHidden())
				{
					mHistoryLayout.hidePanel();
					item.setTitle(R.string.action_show);
				} else 
				{
					mHistoryLayout.showPanel();
					item.setTitle(R.string.action_hide);
				}
			}
			return true;
		}
		case R.id.action_filter:
		{
			Toast.makeText(getApplicationContext(), "Filters on", Toast.LENGTH_SHORT).show();
			return true;
		}
		case R.id.action_settings: 
		{
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private int getActionBarHeight()
	{
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}

	public void setActionBarTranslation(float y) 
	{
		// Figure out the actionbar height
		int actionBarHeight = getActionBarHeight();
		// A hack to add the translation to the action bar
		ViewGroup content = ((ViewGroup) findViewById(android.R.id.content).getParent());
		int children = content.getChildCount();
		int i;
		for (i = 0; i < children; ++i) 
		{
			View child = content.getChildAt(i);
			if (child.getId() != android.R.id.content) 
			{
				if (y <= -actionBarHeight)
				{
					child.setVisibility(View.GONE);
				} else
				{
					child.setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
					{
						child.setTranslationY(y);
					} else
					{
						AnimatorProxy.wrap(child).setTranslationY(y);
					}
				}
			}
		}
	}

	/*
	 * When a push message is arrived, MessageReceiver call MainActivity
	 * activity with a new intent, including the value of the message received *
	 * 
	 * @see
	 * android.support.v4.app.FragmentActivity#onNewIntent(android.content.Intent
	 * )
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		Log.d("Radar", "onNewIntent is called!");

		String lat = intent.getStringExtra(KEY_LATITUDE);
		String lon = intent.getStringExtra(KEY_LONGITUDE);
		
		String newUser = intent.getStringExtra(KEY_NEWUSER);
		String lostUser = intent.getStringExtra(KEY_LOSTUSER);
		
		final String currentMessage = intent.getStringExtra(KEY_MESSAGE);
		final String currentServerId = intent.getStringExtra(KEY_SERVERID);
		
		//		 Case 1 : A message is received and displayed
		if (currentMessage != null && currentServerId != null) 
		{
			//Update history
			if(mHistoryAdapter.getPosition(currentMessage) == -1)
			{
				mHistoryAdapter.add(currentMessage);
			}
			//Update close users messages
			final Button user = mCloseUsers.get(currentServerId);
			if(user != null)
			{
				final Button notif = new Button(getBaseContext());
				// setBackground not present in Android 4.0.3, changed to setBackgroundDrawable
				notif.setBackgroundDrawable(getResources().getDrawable(R.drawable.notification));
				notif.setTag("notifUser" + currentServerId);
				int[] location = new int[2];
				user.getLocationOnScreen(location);
				notif.setX(location[0] - 30.0f);
				notif.setY(user.getY() - 255.0f);
				notif.setLayoutParams(new LayoutParams(NOTIFICATION_DIM, NOTIFICATION_DIM));
				mainContent.addView(notif);
				OnClickListener readingMessage = (new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						ActionItem msg = new ActionItem(1, currentMessage, getResources().getDrawable(R.drawable.spread_icon));
						final QuickAction quickAction = new QuickAction(getBaseContext(), QuickAction.VERTICAL);
						quickAction.addActionItem(msg);
						quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener()
						{
							@Override
							public void onItemClick(QuickAction source, int pos, int actionId)
							{
								mComManager.sendMessage(currentMessage); // THE SPREAD
							}
						});
						quickAction.show(user);
						mainContent.removeView(notif);
					}
				});
				user.setOnClickListener(readingMessage);
				notif.setOnClickListener(readingMessage);
			}
		}
		//		 Case 2 : A new location is received and sent to server
		else if (lat != null)
		{
			mComManager.sendLocation(Double.valueOf(lat), Double.valueOf(lon));
			//buildCloseUsers();
			Log.d("tag", "sent location. Latitude : " + lat + " longitude : " + lon + " for servid : " + mComManager.getServer_id());
		}
		//		Case 3 : A new user is located
		else if (newUser != null)
		{
			if( !mCloseUsers.containsKey(newUser) )
			{
				addNewCloseUser(newUser, mCloseUsers.size());
			}
		}
		else if (lostUser != null)
		{
			if( mCloseUsers.containsKey(lostUser) )
			{
				mainContent.removeView(mCloseUsers.get(lostUser));
				mCloseUsers.remove(lostUser);
				View v = mainContent.findViewWithTag("notifUser" + lostUser);
				if(v != null)
				{
					mainContent.removeView(v);
				}
			}
		}

		super.onNewIntent(intent);
	}
	
	private void buildQuitDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Êtes-vous sûr de vouloir quitter l'application ?")
				.setPositiveButton(R.string.quitApp,
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id)
					{
						mComManager.sendLogout();
						unregisterReceiver(messageReceiver);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						RadarActivity.this.onDestroy();
						//System.exit(0);
					}
				})
				.setNegativeButton(R.string.cancelQuit,
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	@Override
	public void onBackPressed()
	{
		buildQuitDialog();
	}
	
	@Override 
	public void onDestroy() 
	{
		Editor editor = getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		ClearCacheDataUtils.getInstance().trimCache();
		
		super.onDestroy();
		
		int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
	}
	
	public void launchCircleAnimation(View view)
	{
		final Collection<Circle> circles = new ArrayList<Circle>();
		final int minRadiusMaxValue = Math.min(mAnimationView.getMeasuredWidth(), mAnimationView.getMeasuredHeight());
		for (int i = 0; i < 5; ++i)
		{
			final int x = mAnimationView.getMeasuredWidth()/2;
			final int y = mAnimationView.getMeasuredHeight()/2 - getActionBar().getHeight()/2;
			int minRadius = 0;
			if(i==1)
				minRadius = 100;
			else
				minRadius = 100 + i*100;
			final int color = Color.rgb(255, 164, 104);
			circles.add(new Circle(x, y, minRadius, minRadiusMaxValue, color));
		}
		mCirclesAnimation.setCircles(circles);

		mAnimationView.setCanvasAnimation(mCirclesAnimation);
		mAnimationView.startCanvasAnimation();
	}
	
	/*
	 * Inner class receiving the alarmIntent responsible of /users
	 */
	public class AlarmReceiverUsers extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			mComManager.getSurroundingUsers();
			Log.d("tag", "Surrounding users updated.");
		}
	}
	
	public void setTagsInSpinner() 
	{
		//Parse list view to get hashtags
		List<String> tagList = new ArrayList<String>();
		for(int i=0 ; i<mHistoryAdapter.getCount() ; i++){
			String value = mHistoryAdapter.getItem(i).toString();
			Matcher matcher = Pattern.compile("#(\\w+)").matcher(value);
			while(matcher.find()) {
				int it=0;
				for(String s : tagList) {
					if(s.equals(matcher.group(0))) {
						it++;
					}
				}
				if(it==0) {
					tagList.add(matcher.group(0));
				}
			}
		}
		
		//Set the values in the spinner
		Spinner s = (Spinner) findViewById(R.id.listTagsSpinner);
		String[] tags = new String[tagList.size()];
		tags = tagList.toArray(tags);
		ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, tags);
        s.setAdapter(newAdapter);
	}
	
	public void filterList(String tag) 
	{
		List<String> filteredValues = new ArrayList<String>();
		for(int i=0 ; i<mHistoryAdapter.getCount() ; i++){
			String value = mHistoryAdapter.getItem(i).toString();
			if (value.contains(tag)) 
			{
				filteredValues.add(value);
			}
		}
		String[] filteredValuesArray = new String[filteredValues.size()];
		filteredValuesArray = filteredValues.toArray(filteredValuesArray);
		ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filteredValuesArray);
		mFilteredHistoryList.setAdapter(newAdapter);
	}
}
