//package com.spreadit.network;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//public class MainActivity extends Activity {
//
//	private TextView mDisplay;
//	private Button buttonOne;
//	private EditText textEdit;
//	private String currentMessage;
//	private ComManager comManager = new ComManager();
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		mDisplay = (TextView) findViewById(R.id.display);
//		textEdit = (EditText) findViewById(R.id.editText1);
//
//		comManager.connectAndGetGcmId(MainActivity.this);
//		comManager.setMainAct(MainActivity.this);
//
//		mDisplay.setText(comManager.getRegid());
//		buttonOne = (Button) findViewById(R.id.button1);
//
//		buttonOne.setOnClickListener(new Button.OnClickListener() {
//			public void onClick(View v) {
//				
//				comManager.sendMessage(textEdit.getText().toString());
//			}
//		});
//
//		// reset server database
//		Button buttonReset;
//		buttonReset = (Button) findViewById(R.id.button2);
//		buttonReset.setOnClickListener(new Button.OnClickListener() {
//			public void onClick(View v) {
//				comManager.sendResetDatabase();
//			}
//		});
//
//	}
//
//	// You need to do the Play Services APK check here too, likewise we send
//	// location again
//	@Override
//	protected void onResume() {
//		super.onResume();
//		comManager.checkPlayServices(MainActivity.this);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	public void onDestroy() {
//		comManager.sendLogout();
//		super.onDestroy();
//	}
//
//	/*
//	 * When a push message is arrived, MessageReceiver call MainActivity
//	 * activity with a new intent, including the value of the message received *
//	 * 
//	 * @see
//	 * android.support.v4.app.FragmentActivity#onNewIntent(android.content.Intent
//	 * )
//	 */
//	@Override
//	protected void onNewIntent(Intent intent) {
//		Log.d("MainActivity", "onNewIntent is called!");
//
//		currentMessage = intent.getStringExtra("msg");
//		String lat = intent.getStringExtra("latitude");
//		String lon = intent.getStringExtra("longitude");
//
//		// Case 1 : A message is received and displayed
//		if (currentMessage != null) {
//			currentMessage = intent.getStringExtra("msg");
//			mDisplay.setText(currentMessage);
//		}
//		// Case 2 : A new location is received and sent to server
//		else if (lat != null) {
//			comManager.sendLocation(Double.valueOf(lat), Double.valueOf(lon));
//			Log.d("tag", "sent location. Latitude : " + lat + " longitude : " + lon + " for servid : " + comManager.getServer_id());
//		}
//		super.onNewIntent(intent);
//	}
//
//	
//}
