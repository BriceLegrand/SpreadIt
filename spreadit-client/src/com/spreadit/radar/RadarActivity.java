package com.spreadit.radar;


import com.spreadit.R;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class RadarActivity extends Activity 
{
	private GestureDetector mGestureDetector;
	
	private final float ACTION_BAR_TITLE_SIZE = 37.0f;
	private final String ACTION_BAR_TITLE_FONT = "fonts/intriquescript.ttf";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		buildActionBar();
		setContentView(R.layout.activity_radar);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		

	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	    Typeface tF = Typeface.createFromAsset(getAssets(), ACTION_BAR_TITLE_FONT);
	    
	    Button btn = (Button) findViewById(R.id.historyBtn);
	    btn.setTypeface(tF);
	}
	
	private void buildActionBar()
	{
	   // SpannableString s = new SpannableString(ACTION_BAR_TITLE);
	   // s.setSpan(new TypefacesSpan(this, ACTION_BAR_TITLE_FONT), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	   // Update the action bar title with the TypefaceSpan instance
	    ActionBar actionBar = getActionBar();
	    //actionBar.setTitle(s);
	    //actionBar.setCustomView(R.layout.actionbar);
	    actionBar.setLogo(R.drawable.logo);
	    int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
	    TextView abTitle = (TextView) findViewById(titleId);
	    abTitle.setTextSize(ACTION_BAR_TITLE_SIZE);
	    abTitle.setPadding(180, 0, 0, 0);
	    Typeface tF = Typeface.createFromAsset(getAssets(), ACTION_BAR_TITLE_FONT);
	    abTitle.setTypeface(tF);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.radar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{ 
		 return mGestureDetector.onTouchEvent(event); 
	} 
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_radar,
					container, false);
			return rootView;
		}
	}

}
