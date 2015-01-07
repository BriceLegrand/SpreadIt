package com.spreadit.splash;

import com.spreadit.R;
import com.spreadit.R.id;
import com.spreadit.R.layout;
import com.spreadit.R.menu;
//import com.spreadit.network.ComManager;
import com.spreadit.radar.RadarActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

public class SplashScreen extends Activity 
{
	//private ComManager mComManager = new ComManager();
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1500;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		//mComManager.connectAndGetGcmId(this);
		//mComManager.setMainAct(this);
		/* New Handler to start the Menu-Activity 
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
		//if(mComManager.isLocationEnabled())
		//{
            Intent mainIntent = new Intent(SplashScreen.this, RadarActivity.class);
            SplashScreen.this.startActivity(mainIntent);
            SplashScreen.this.finish();
		//}
            }
        }, SPLASH_DISPLAY_LENGTH);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
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
}
