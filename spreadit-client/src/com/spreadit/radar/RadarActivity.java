package com.spreadit.radar;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.spreadit.R;
import com.spreadit.network.ComManager;
import com.spreadit.utils.ActionItem;
import com.spreadit.utils.QuickAction;

public class RadarActivity extends Activity 
{
	private SlidingUpPanelLayout 	mHistoryLayout;

	private RelativeLayout 			mainContent;

	private EditText 				mNewMsg;

	private ListView 				mHistoryList;

	private boolean 				bDisplayMsg;

	private Map<String, Button>		mCloseUsers;

	private ComManager				mComManager;


	public 	static final String 	SAVED_STATE_ACTION_BAR_HIDDEN 	= "saved_state_action_bar_hidden";
	private static final String 	ACTION_BAR_TITLE_FONT 			= "fonts/intriquescript.ttf";
	private static final float 		ACTION_BAR_TITLE_SIZE 			= 37.0f;
	private static final float 		SCREEN_X_MAX 					= 1000.0f;
	private static final float		SCREEN_Y_MAX					= 1220.0f;
	private static final int		NB_CLOSE_USERS					= 5;
	private static final int		CLOSE_USER_DIM					= 100;  //dp

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

		mainContent = (RelativeLayout) findViewById(R.id.mainContent);

		buildActionBar();
		buildHistory();

		mCloseUsers = new HashMap<String, Button>();
		buildCloseUsers();

		mNewMsg = (EditText) findViewById(R.id.txtNewMsg);
		mNewMsg.setVisibility(View.GONE);

		bDisplayMsg = false;

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
		btnNewMsg.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// TODO Auto-ge	nerated method stub
				if( !bDisplayMsg )
				{
					mNewMsg.setVisibility(View.VISIBLE);
					btnNewMsg.setBackground(getResources().getDrawable(btnCenterBg[1]));
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
					//send the message
					mComManager.sendMessage(mNewMsg.getText().toString());
					
					mNewMsg.setText("");
					//start the wave
					//at end of wave trigger change of button
					btnNewMsg.setBackground(getResources().getDrawable(btnCenterBg[0]));
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
		String[] values = new String[] { "#API12 So Fresh !", "Such concert #Amaze",
				"Alea Jacta #Est", "Avé #Cesar", "Android & SMA #API12" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		mHistoryList.setAdapter(adapter);

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
				Toast.makeText(getApplicationContext(), "Filters on", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void buildCloseUsers() 
	{
		mCloseUsers.clear();
		int i;
		//for(i = 0; i < NB_CLOSE_USERS; ++i)
		for(i = 0; i < mComManager.getUsers().size(); ++i)
		{
			Button user = new Button(getBaseContext());
			user.setBackground(getResources().getDrawable(R.drawable.btn_close_user));
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

			View v = mainContent.findViewWithTag("userView" + i);
			if(v != null)
			{
				mainContent.removeView(v);
			}
			user.setTag("userView" + i);
			mainContent.addView(user);
			//mCloseUsers.put(mComManager.getUsers().get(i).toString(), user);
			mCloseUsers.put(Integer.toString(i), user);
		}
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
	protected void onNewIntent(Intent intent) {
		Log.d("Radar", "onNewIntent is called!");

		String lat = intent.getStringExtra("latitude");
		String lon = intent.getStringExtra("longitude");

		//		 Case 1 : A message is received and displayed
		final String currentMessage = intent.getStringExtra("msg");
		if (currentMessage != null) 
		{
			//for(int i = 0; i < mCloseUsers.size(); ++i)
			//{
				Button user = mCloseUsers.get(Integer.toString(0));
				user.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						ActionItem msg = new ActionItem(1, currentMessage, getResources().getDrawable(R.drawable.spread_icon));
						final QuickAction quickAction = new QuickAction(getBaseContext(), QuickAction.VERTICAL);
						quickAction.addActionItem(msg);
						quickAction.show(v);
					}
				});
			//}
		}
		//		 Case 2 : A new location is received and sent to server
		if (lat != null)
		{
			mComManager.sendLocation(Double.valueOf(lat), Double.valueOf(lon));
			Log.d("tag", "sent location. Latitude : " + lat + " longitude : " + lon + " for servid : " + mComManager.getServer_id());
		}

		super.onNewIntent(intent);
	}
	
	@Override
	public void onDestroy()
	{
		mComManager.sendLogout();
		super.onDestroy();
	}

}
