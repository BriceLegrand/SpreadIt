package com.spreadit.splash;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.spreadit.R;
import com.spreadit.network.ComManager;

public class SplashScreen extends Activity
{
	private ComManager mComManager;
	
	public static Context AppContext;
	/** Duration of wait **/
	//private final int SPLASH_DISPLAY_LENGTH = 1500;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		AppContext = getApplicationContext();
		
		mComManager = ComManager.getInstance();
		mComManager.connectAndGetGcmId(this);
		mComManager.setMainAct(this);

		/*
		 * New Handler to start the Menu-Activity and close this Splash-Screen
		 * after some seconds.
		 */
		// new Handler().postDelayed(new Runnable(){
		// @Override
		// public void run() {
		/* Create an Intent that will start the Menu-Activity. */
		// if(mComManager.isLocationEnabled())
		// {
		// Intent mainIntent = new Intent(SplashScreen.this,
		// RadarActivity.class);
		// SplashScreen.this.startActivity(mainIntent);
		// SplashScreen.this.finish();
		// }
		// }
		// }, SPLASH_DISPLAY_LENGTH);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		// Case 1 : La localisation est prete, lancer l'alarme sur la recup des
		// users tous les
		// x time
		if (intent.getStringExtra("LocReady") != null)
		{
			if (intent.getStringExtra("LocReady").equals("DONE"))
			{
				mComManager.getSurroundingUsers();
			}
			else
			{
				Log.d("tag", "New intent users not here.");
			}
		}

		// Case 2 : A new location is received and sent to server
		else if (intent.getStringExtra("latitude") != null)
		{
			String lat = intent.getStringExtra("latitude");
			String lon = intent.getStringExtra("longitude");
			mComManager.sendLocation(Double.valueOf(lat), Double.valueOf(lon));
			Log.d("tag", "sent location. Latitude : " + lat + " longitude : "
					+ lon + " for servid : " + mComManager.getServer_id());
		}

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
}
