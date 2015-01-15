package com.spreadit.splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.spreadit.R;
import com.spreadit.network.ComManager;

public class SplashScreen extends Activity
{
	private ComManager mComManager;
	
	private ImageView mImgConnexion;

	private ImageView mImgGeoloc;
	
	public static Context AppContext;
	
	public static final int[] LOG_ICONS = { R.drawable.cross, R.drawable.valid };

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		AppContext = getApplicationContext();
		
		mImgConnexion = (ImageView) findViewById(R.id.imgCnx);
		mImgGeoloc = (ImageView) findViewById(R.id.imgGeo);
		
		mComManager = ComManager.getInstance();
		mComManager.connectAndGetGcmId(this);
		mComManager.setMainAct(this);
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
		// Case 1 : La localisation est prete, lancer l'alarme sur la recup des users tous les x time
		if (intent.getStringExtra("LocReady") != null)
		{
			if (intent.getStringExtra("LocReady").equals("DONE"))
			{
				turnGeolocValid();
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
		else if(intent.getStringExtra("logConnexion") != null)
		{
			turnConnexionValid();
		}
	}
	
	public void turnConnexionValid()
	{
		mImgConnexion.setImageDrawable(getResources().getDrawable(LOG_ICONS[1]));
	}
	
	public void turnGeolocValid()
	{
		mImgGeoloc.setImageDrawable(getResources().getDrawable(LOG_ICONS[1]));
	}
}
