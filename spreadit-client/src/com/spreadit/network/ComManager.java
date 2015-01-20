package com.spreadit.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.spreadit.R;
import com.spreadit.radar.RadarActivity;
import com.spreadit.splash.SplashScreen;
import com.spreadit.radar.RadarActivity.AlarmReceiverUsers;

public class ComManager implements AsyncResponse {
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String PROPERTY_APP_VERSION = "appVersion";
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";

	SendLoginAndRequestServIdHttpTask gcmIdhttpTask;
	SendMessageHttpTask sendMsgHttpTask;
	SendLocationHttpTask sendLocationHttpTask;
	SendResetTTLHttpTask sendResetTTLHttpTask;
	SendLogoutHttpTask sendLogoutHttpTask;
	SendResetHttpTask sendResetHttpTask;
	GetSurroundingUsersHttpTask getSurroundUserHttpTask;

	private LocationsManager locManager;

	private String SENDER_ID;

	static final String TAG = "GCMDemo";
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;

	private AlarmManager mAlarmMgrUsers;
	private PendingIntent mAlarmIntentUsers;

	private String servUrl;

	private List<String> users;

	private static Context mContext;
	private GoogleCloudMessaging gcm;
	private String regid;
	private String serverId;

	private Activity mainAct;

	private boolean bIsLocationEnabled;

	/** Constructeur privé */
	private ComManager() {
		bIsLocationEnabled = false;
		users = new ArrayList<String>();
		servUrl = "http://62.210.236.244:8080";
		//servUrl = "http://192.168.43.202:8080";
		mContext = SplashScreen.AppContext;
		locManager = new LocationsManager(mContext);
		SENDER_ID = "168328884942";
	}

	private static class SingletonHolder {
		/** Instance unique non préinitialisée */
		private static ComManager COM_MANAGER_INSTANCE = new ComManager();
	}

	/** Point d'accès pour l'instance unique du singleton */
	public static ComManager getInstance() {
		return SingletonHolder.COM_MANAGER_INSTANCE;
	}

	public void startUsersAlarmManager() {
		mAlarmMgrUsers = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		long duration = 1000 * 60 * 5;
		mAlarmMgrUsers.setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME_WAKEUP, duration, duration,
				mAlarmIntentUsers);
		Intent intent2 = new Intent(mContext, AlarmReceiverUsers.class);
		mAlarmIntentUsers = PendingIntent.getBroadcast(mContext, 0, intent2, 0);
	}

	public void sendLogin(String gcm_id) {
		Log.d("tag", "sendRegIdToServer called with reg id " + gcm_id);
		gcmIdhttpTask = new SendLoginAndRequestServIdHttpTask();
		gcmIdhttpTask.delegate = this;
		gcmIdhttpTask.execute(servUrl + "/login", gcm_id);

		// Launch alarmManager to send /reset_ttl every X minutes
		alarmMgr = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		long duration = 1000 * 60 * 5;
		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				duration, duration, alarmIntent);

		Intent intent = new Intent(mContext, AlarmReceiverTTL.class);
		alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
	}

	public void sendMessage(String message) {
		sendMsgHttpTask = new SendMessageHttpTask();
		sendMsgHttpTask.delegate = this;
		sendMsgHttpTask.execute(servUrl + "/send", serverId, message);
	}

	public void sendLocation(Double latitude, Double longitude) {
		if (locManager.isSplashOn()) {
			sendLocationHttpTask = new SendLocationHttpTask();
			sendLocationHttpTask.delegate = this;
			sendLocationHttpTask.execute(servUrl + "/position", serverId,
					latitude.toString(), longitude.toString());
		}
	}

	public void getSurroundingUsers() {
		if (locManager.isSplashOn()) {
			getSurroundUserHttpTask = new GetSurroundingUsersHttpTask();
			getSurroundUserHttpTask.delegate = this;
			getSurroundUserHttpTask.execute(servUrl + "/users", serverId);
		}
	}

	public void sentResetTTL() {
		sendResetTTLHttpTask = new SendResetTTLHttpTask();
		sendResetTTLHttpTask.delegate = this;
		sendResetTTLHttpTask.execute(servUrl + "/reset_ttl", serverId);
	}

	public void sendLogout() {
		sendLogoutHttpTask = new SendLogoutHttpTask();
		sendLogoutHttpTask.delegate = this;
		sendLogoutHttpTask.execute(servUrl + "/logout", serverId);
	}

	public void sendResetDatabase() {
		sendResetHttpTask = new SendResetHttpTask();
		sendResetHttpTask.delegate = this;
		sendResetHttpTask.execute(servUrl + "/");
	}

	@Override
	public void processReqServIdFinish(String serverId) {
		// this you will receive result fired from async class of onPostExecute(result) method of RequestServerIdHttpTask.
		Log.d("tag", "http result from process Finish " + serverId);
		this.setServer_id(serverId);


		// We send location during connection after having verified that location services are activated
		if (checkAndAskForLocationTrackingEnabled() && !locManager.isTrackingStarted())
			locManager.startLocationTracking();

	}

	@Override
	public void processSendMessageFinish() {
		// this you will received result fired from async class of
		// onPostExecute() method of SendMessageHttpTask.
		Log.d("tag", "Message successfully sent");
	}

	@Override
	public void processSendLocationFinish() {
		Log.d("tag", "Location successfully sent");
		if (locManager.isSplashOn()) {
			Intent mainIntent = new Intent(this.getMainAct(), SplashScreen.class);
			mainIntent.putExtra("LocReady", "DONE");
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(mainIntent);
		}

	}

	/*
	 * Method called when the application is launched
	 */
	public void connectAndGetGcmId(Activity act) {
		// Check device for Play Services APK.
		if (checkPlayServices(act)) {
			// If this check succeeds, proceed with normal processing.
			// Otherwise, prompt user to get valid Play Services APK.
			gcm = GoogleCloudMessaging.getInstance(mContext);
			regid = getRegistrationId(mContext);

			if (regid.isEmpty()) {
				Log.d(TAG, "is empty");
				registerInBackground();
			} else {
				sendLogin(regid);
			}

		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	public boolean checkPlayServices(Activity act) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(act.getApplicationContext());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, act,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				act.finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(mContext);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return mContext.getSharedPreferences(
				RadarActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageInfo(mContext.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/*
	 * Inner class responsible of the registration in an AsyncTask
	 */
	public class Register extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			String msg = "";
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(mContext);
				}
				regid = gcm.register(SENDER_ID);
				msg = "Device registered, registration ID=" + regid;

				// You should send the registration ID to your server over HTTP,
				// so it can use GCM/HTTP or CCS to send messages to your app.
				// The request to your server should be authenticated if your
				// app
				// is using accounts.
				sendRegistrationIdToBackend();

				// For this demo: we don't need to send it because the device
				// will send upstream messages to a server that echo back the
				// message using the 'from' address in the message.

				// Persist the regID - no need to register again.
				storeRegistrationId(mContext, regid);
			} catch (IOException ex) {
				msg = "Error :" + ex.getMessage();
				// If there is an error, don't just keep trying to register.
				// Require the user to click a button again, or perform
				// exponential back-off.
			}
			Log.d("tag", msg);
			return regid;
		}

		@Override
		protected void onPostExecute(String msg) {
			sendLogin(msg);
		}
	}

	/*
	 * Inner class receiving the alarmIntent responsible of /reset_ttl
	 */
	public class AlarmReceiverTTL extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ComManager.this.sentResetTTL();
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */

	private void registerInBackground() {
		Register reg = new Register();
		reg.execute();
	}

	private void sendRegistrationIdToBackend() {
		// Your implementation here.
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public String getServer_id() {
		return serverId;
	}

	public void setServer_id(String server_id) {
		this.serverId = server_id;
	}

	public String getRegid() {
		return regid;
	}

	public void setRegid(String regid) {
		this.regid = regid;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public boolean isLocationEnabled() {
		return bIsLocationEnabled;
	}

	public void setIsLocationEnabled(boolean bIsLocationEnabled) {
		this.bIsLocationEnabled = bIsLocationEnabled;
	}

	@Override
	public void processSendResetTTLFinish() {
		Log.d("tag", "Reset ttl sent");
	}

	@Override
	public void processSendLogoutFinish() {
		Log.d("tag", "send logout finished");
		this.getMainAct().finish();
		locManager.stopLocationTracking();
	}

	public void processGetSurroundingUsersFinish(String response) {
		if (response != null) 
		{
			if (!response.equals(""))
				users = Arrays.asList(response.split(","));
			if (!response.equals("Time to live expired or user not logged in")) {
				// on n'est plus sensés envoyer des intent au splashscreen
				locManager.setIsSplashOn(false);
				// fin : lancement de Radar activity
				Intent mainIntent = new Intent(this.getMainAct(),
						RadarActivity.class);
				this.getMainAct().startActivity(mainIntent);
				this.getMainAct().finish();
			}
		}
	}

	@Override
	public void processSendResetDatabaseFinish() {
		// TODO Auto-generated method stub
		Log.d("tag", "reset database done");
	}

	public Activity getMainAct() {
		return mainAct;
	}

	public void setMainAct(Activity mainAct) {
		this.mainAct = mainAct;
		locManager.setMainAct(mainAct);
	}

	public boolean checkAndAskForLocationTrackingEnabled() {

		bIsLocationEnabled = false;
		LocationManager manager = (LocationManager) this.getMainAct()
				.getSystemService(Context.LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			bIsLocationEnabled = false;
			Log.d("tag", "location still unavailable");

			AlertDialog.Builder builder = new AlertDialog.Builder(this.getMainAct());
			builder.setMessage(
					"Cette application recquiert l'activation de la géolocalisation, voulez-vous l'activer ?")
					.setPositiveButton(R.string.fire,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							Intent gpsOptionsIntent = new Intent(
									android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							mContext.startActivity(gpsOptionsIntent);
						}
					})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ComManager.this.getMainAct().finish();
						}
					});

			AlertDialog alertDialog = builder.create();
			Log.d("tag", "alertbuilder created");
			alertDialog.show();
		}

		bIsLocationEnabled = true;

		return bIsLocationEnabled;

	}

}
