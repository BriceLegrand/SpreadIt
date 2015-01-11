package com.spreadit.network;

import com.spreadit.radar.RadarActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver
{
	/*
	 * Allow the application to receive GCM push messages
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		String newMessage = intent.getExtras().getString("msg");
		Log.d("tag","Message received from GCM " + newMessage);

		Intent intent2open = new Intent(context, RadarActivity.class);
		intent2open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent2open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		String name = "msg";
		String value = newMessage;
		intent2open.putExtra(name, value);
		context.startActivity(intent2open);
	}

}
