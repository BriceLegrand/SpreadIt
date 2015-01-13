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
		String nameMsg = "msg";
		String messageEtServerId = intent.getExtras().getString(nameMsg);
		Log.d("tag","Message received from GCM " + messageEtServerId);
		
		Intent intent2open = new Intent(context, RadarActivity.class);
		intent2open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent2open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		String valueMsg = messageEtServerId.substring(messageEtServerId.lastIndexOf('|') + 1);
		intent2open.putExtra(nameMsg, valueMsg);
		
		String nameServerId = "server_id";
		String valueServerId = messageEtServerId.substring(0, messageEtServerId.lastIndexOf('|'));
		intent2open.putExtra(nameServerId, valueServerId);
		
		context.startActivity(intent2open);
	}

}
