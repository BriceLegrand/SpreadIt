package com.spreadit.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.spreadit.radar.RadarActivity;
import com.spreadit.splash.SplashScreen;

public class LocationsManager implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener
{
	private final static int
	CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private Activity mainAct; 

	LocationClient mLocationClient;
	LocationRequest mLocationRequest;
	private Context mAppContext;
	private Double latitude;
	private Double longitude;

	private boolean bIsSplashOn;


	public LocationsManager(Context appContext)
	{
		super();
		this.mAppContext = appContext;
		this.mLocationClient = new LocationClient(this.mAppContext, this, this);
		this.mLocationRequest = new LocationRequest();
		bIsSplashOn = true;
	}

	/*
	 * Called by Location Services if the attempt to
	 * Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{
		if (connectionResult.hasResolution())
		{
			try
			{
				connectionResult.startResolutionForResult(
						this.getMainAct(),
						CONNECTION_FAILURE_RESOLUTION_REQUEST);

			} catch (IntentSender.SendIntentException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.d("tag", Integer.toString(connectionResult.getErrorCode()));
		}
	}


	/*
	 * Called by Location Services when the request to connect the
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle arg0)
	{
		//Toast.makeText(this.getMainAct(), "Connected", Toast.LENGTH_SHORT).show();
		if(bIsSplashOn)
		{
			Intent intentLog = new Intent(this.mAppContext, SplashScreen.class);
			intentLog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intentLog.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			intentLog.putExtra("logConnexion", "DONE");
			this.mAppContext.startActivity(intentLog);
		}
		
		Location currentLoc = mLocationClient.getLastLocation();

		if (currentLoc == null)
		{
			//then the location is not available for the moment, let's send a location request
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			Log.d("tag", "Location is not available at the moment, request location updates");
		}
		else
		{
			sendLocationToActivity(currentLoc);
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			Log.d("tag", "sending location to activity");
		}

	}

	/*
	 * Called by Location Services if the connection to the
	 * location client drops because of an error.
	 */
	@Override
	public void onDisconnected()
	{
		// TODO Auto-generated method stub
		Toast.makeText(this.getMainAct(), "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	public void startLocationTracking()
	{
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

		// Set the update interval to 2 minutes
		mLocationRequest.setInterval(120000);
		// Set the fastest update interval to 20 seconds
		mLocationRequest.setFastestInterval(20000);

		mLocationClient.connect();
	}

	public void stopLocationTracking()
	{
		mLocationClient.disconnect();
	}

	public boolean isTrackingStarted()
	{
		if(mLocationClient.isConnected())
		{
			return true;
		}
		return false;
	}

	public Activity getMainAct()
	{
		return mainAct;
	}

	public void setMainAct(Activity mainAct)
	{
		this.mainAct = mainAct;
	}


	public boolean isSplashOn()
	{
		return bIsSplashOn;
	}

	public void setIsSplashOn(boolean bIsSplashOn)
	{
		this.bIsSplashOn = bIsSplashOn;
	}

	@Override
	public void onLocationChanged(Location currentLoc)
	{
		Log.d("tag", "location changed arg0 : " + currentLoc.toString());

		if (currentLoc != null)
		{
			sendLocationToActivity(currentLoc);
		}
	}

	public void sendLocationToActivity(Location currentLoc)
	{
		latitude = currentLoc.getLatitude();
		longitude = currentLoc.getLongitude();
		Intent intent2open;
		if(bIsSplashOn)
		{
			intent2open = new Intent(this.mAppContext, SplashScreen.class);
		}
		else
		{
			intent2open = new Intent(this.mAppContext, RadarActivity.class);
		}
		intent2open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent2open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		intent2open.putExtra("latitude", latitude.toString());
		intent2open.putExtra("longitude", longitude.toString());

		this.mAppContext.startActivity(intent2open);
	}

}
