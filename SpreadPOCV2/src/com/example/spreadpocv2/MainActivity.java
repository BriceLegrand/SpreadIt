package com.example.spreadpocv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	TextView mDisplay;
	Button buttonOne;
	EditText textEdit;
	String currentMessage;
	ComManager comManager = new ComManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDisplay = (TextView) findViewById(R.id.display);
		textEdit = (EditText) findViewById(R.id.editText1);
		comManager.connectAndGetGcmId(MainActivity.this);
		buttonOne = (Button) findViewById(R.id.button1);
		buttonOne.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				comManager.sendMessage(textEdit.getText().toString());
			}
		});
	}

	// You need to do the Play Services APK check here too.
	@Override
	protected void onResume() {
		super.onResume();
		comManager.checkPlayServices(MainActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	protected void onNewIntent(Intent intent) {
		Log.d("MainActivity", "onNewIntent is called!");
		currentMessage = intent.getStringExtra("msg");
		mDisplay.setText(currentMessage);
		super.onNewIntent(intent);
	} // End of onNewIntent(Intent intent)
	

}
