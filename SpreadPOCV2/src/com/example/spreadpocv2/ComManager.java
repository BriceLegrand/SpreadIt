package com.example.spreadpocv2;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class ComManager implements AsyncResponse {
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	RequestServerIdHttpTask gcmIdhttpTask;
	SendMessageHttpTask sendMsgHttpTask;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "168328884942";

	static final String TAG = "GCMDemo";

	private Context context;
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private SharedPreferences prefs;
	private String regid;
	private String serverId;

	public void sendRegIdToServer(String gcm_id) {
		Log.d("tag", "sendRegIdToServer called with reg id " + gcm_id);
		gcmIdhttpTask = new RequestServerIdHttpTask();
		gcmIdhttpTask.delegate = this;
		gcmIdhttpTask.execute("http://192.168.1.19:8080/login", gcm_id);
	}

	public void sendMessage(String message) {
		sendMsgHttpTask = new SendMessageHttpTask();
		sendMsgHttpTask.delegate = this;
		sendMsgHttpTask.execute("http://192.168.1.19:8080/send", serverId,
				message);
	}

	public void processReqServIdFinish(String serverId) {
		// this you will received result fired from async class of
		// onPostExecute(result) method of RequestServerIdHttpTask.
		Log.d("tag", "http result from process Finish " + serverId);
		this.setServer_id(serverId);
	}

	public void processSendMessageFinish() {
		// this you will received result fired from async class of
		// onPostExecute() method of SendMessageHttpTask.
		Log.d("tag", "Message successfully sent");
	}

	/*
	 * Method called when the application is launched
	 */
	public void connectAndGetGcmId(Activity act) {
		// Check device for Play Services APK.
		if (checkPlayServices(act)) {
			// If this check succeeds, proceed with normal processing.
			// Otherwise, prompt user to get valid Play Services APK.
			gcm = GoogleCloudMessaging.getInstance(SpreadPOCV2.getAppContext());
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				Log.d(TAG, "is empty");
				registerInBackground();
			} else {
				sendRegIdToServer(regid);
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
				.isGooglePlayServicesAvailable(SpreadPOCV2.getAppContext());
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
		final SharedPreferences prefs = getGCMPreferences(context);
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
		return SpreadPOCV2.getAppContext().getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = SpreadPOCV2
					.getAppContext()
					.getPackageManager()
					.getPackageInfo(
							SpreadPOCV2.getAppContext().getPackageName(), 0);
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
					gcm = GoogleCloudMessaging.getInstance(context);
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
				storeRegistrationId(context, regid);
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
			sendRegIdToServer(msg);
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
}
