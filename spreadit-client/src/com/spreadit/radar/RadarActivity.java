package com.spreadit.radar;


import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.spreadit.R;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class RadarActivity extends Activity 
{
	private SlidingUpPanelLayout mHistoryLayout;
	
	public static final String SAVED_STATE_ACTION_BAR_HIDDEN = "saved_state_action_bar_hidden";
	private static final float ACTION_BAR_TITLE_SIZE = 37.0f;
	private static final String ACTION_BAR_TITLE_FONT = "fonts/intriquescript.ttf";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		buildActionBar();
		setContentView(R.layout.activity_radar);

		mHistoryLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mHistoryLayout.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                setActionBarTranslation(mHistoryLayout.getCurrentParalaxOffset());
            }

            @Override
            public void onPanelExpanded(View panel) {
            }

            @Override
            public void onPanelCollapsed(View panel) {
            }

            @Override
            public void onPanelAnchored(View panel) {
            }

            @Override
            public void onPanelHidden(View panel) {
            }
        });
		
        boolean actionBarHidden = savedInstanceState != null && savedInstanceState.getBoolean(SAVED_STATE_ACTION_BAR_HIDDEN, false);
        if (actionBarHidden) 
        {
            int actionBarHeight = getActionBarHeight();
            setActionBarTranslation(-actionBarHeight);//will "hide" an ActionBar
        }
		
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
	    
	    TextView txt = (TextView) findViewById(R.id.historyTxt);
	    txt.setTypeface(tF);
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
        if (mHistoryLayout != null) {
            if (mHistoryLayout.isPanelHidden()) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_toggle: {
                if (mHistoryLayout != null) {
                    if (!mHistoryLayout.isPanelHidden()) {
                    	mHistoryLayout.hidePanel();
                        item.setTitle(R.string.action_show);
                    } else {
                    	mHistoryLayout.showPanel();
                        item.setTitle(R.string.action_hide);
                    }
                }
                return true;
            }
            case R.id.action_settings: {
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
        for (int i = 0; i < children; i++) {
            View child = content.getChildAt(i);
            if (child.getId() != android.R.id.content) {
                if (y <= -actionBarHeight) {
                    child.setVisibility(View.GONE);
                } else {
                    child.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        child.setTranslationY(y);
                    } else {
                        AnimatorProxy.wrap(child).setTranslationY(y);
                    }
                }
            }
        }
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
