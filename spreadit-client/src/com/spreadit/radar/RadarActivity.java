package com.spreadit.radar;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import com.spreadit.radar.CirclesCanvasAnimation.Circle;
import com.spreadit.radar.CanvasAnimationView;
import com.spreadit.radar.CirclesCanvasAnimation;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.spreadit.R;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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

public class RadarActivity extends Activity 
{
	private SlidingUpPanelLayout 	mHistoryLayout;
	
	private RelativeLayout 			mainContent;
	
	private EditText 				mNewMsg;
	
	private ListView 				mHistoryList;
	
	private boolean 				bDisplayMsg;
	
	private Vector<Button>			mCloseUsers;

	private CanvasAnimationView 	mAnimationView;
	
	private CirclesCanvasAnimation  mCirclesAnimation;
	
	public 	static final String 	SAVED_STATE_ACTION_BAR_HIDDEN 	= "saved_state_action_bar_hidden";
	private static final String 	ACTION_BAR_TITLE_FONT 			= "fonts/intriquescript.ttf";
	private static final float 		ACTION_BAR_TITLE_SIZE 			= 37.0f;
	private static final float 		SCREEN_X_MAX 					= 1050.0f;
	private static final float		SCREEN_Y_MAX					= 1280.0f;
	private static final int		NB_CLOSE_USERS					= 5;
	private static final int		CLOSE_USER_DIM					= 40;  //dp
	private static final int		ONE_LINE_LENGTH_MAX				= 50; //chars
	
	private static final int[] btnCenterBg = { R.drawable.btn_add_msg, R.drawable.btn_send_msg };

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radar);
		
		mAnimationView = (CanvasAnimationView) findViewById(R.id.wave_view);
		mCirclesAnimation = new CirclesCanvasAnimation(5000, true);
		
		mainContent = (RelativeLayout) findViewById(R.id.mainContent);
		
		buildActionBar();
		buildHistory();
		buildCloseUsers();
		
		mNewMsg = (EditText) findViewById(R.id.txtNewMsg);
		mNewMsg.setVisibility(View.GONE);
		
		Button btnNewMsg = (Button) findViewById(R.id.btnNewMsg);
		btnNewMsg.setY(mAnimationView.getHeight()+getActionBarHeight()/2);
		
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
		btnNewMsg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-ge	nerated method stub
				if( !bDisplayMsg )
				{
					mNewMsg.setVisibility(View.VISIBLE);
					mNewMsg.setY(mNewMsg.getY()+40);
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
					//start the wave
					if( !mNewMsg.getText().toString().equals("") )
					{
						launchCircleAnimation(v);
					}
					mNewMsg.setText("");
				
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
		mCloseUsers = new Vector<Button>();
		int i;
		for(i = 0; i < NB_CLOSE_USERS; ++i)
		{
			Button user = new Button(getBaseContext());
			user.setBackground(getResources().getDrawable(R.drawable.btn_close_user));
			user.setLayoutParams(new LayoutParams(CLOSE_USER_DIM, CLOSE_USER_DIM));
			
			Random fate = new Random();
			user.setX(fate.nextInt(Math.round(SCREEN_X_MAX)));
			user.setY(fate.nextInt(Math.round(SCREEN_Y_MAX)));
			final float MIDDLE_SCREEN_X = SCREEN_X_MAX / 2.0f;
			while( (user.getX() > (MIDDLE_SCREEN_X - 100.0f) && user.getX() < (MIDDLE_SCREEN_X + 100.0f)) 
					&& (user.getY() > 600.0f && user.getY() < 780.0f) )
			{
				user.setX(fate.nextInt(Math.round(SCREEN_X_MAX)));
				user.setY(fate.nextInt(Math.round(SCREEN_Y_MAX)));
			}
			final float userX = user.getX();
			final float userY = user.getY();
			final int nb = i;
			user.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					final TextView msgBox = (TextView) getLayoutInflater().inflate(R.layout.msg_box, null);
					msgBox.setText("I am user " + nb);
					// calculation of message position
					msgBox.setY(userY + 10.0f);
					if( userX > (MIDDLE_SCREEN_X - 20.0f ) )
					{
						float msgPonderation = Math.min(((float) msgBox.getText().length()) / ONE_LINE_LENGTH_MAX, 1.0f);
						msgBox.setX(userX - 2.5f * msgPonderation * ((MIDDLE_SCREEN_X - 20.0f) - (SCREEN_X_MAX - userX)));
					}
					else
					{
						msgBox.setX(userX);
					}
					
					msgBox.setOnClickListener(new OnClickListener() 
					{
						
						@Override
						public void onClick(View v) 
						{
							msgBox.setVisibility(View.GONE);
						}
					});
					mainContent.addView(msgBox);
				}
			});
			
			mainContent.addView(user);
			mCloseUsers.add(user);
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
	
	public void launchCircleAnimation(View view) {
		final Collection<Circle> circles = new ArrayList<Circle>();
		final Random random = new Random(); 
		final int minRadiusMaxValue = Math.min(mAnimationView.getMeasuredWidth(), mAnimationView.getMeasuredHeight());
		for (int i = 0; i < 5; i ++) {
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

}
