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
		String messageOuNewUserEtServerId = intent.getExtras().getString(nameMsg);
		
		Intent intent2open = new Intent(context, RadarActivity.class);
		intent2open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent2open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if(messageOuNewUserEtServerId != null)
		{
			String key = messageOuNewUserEtServerId.substring(0, messageOuNewUserEtServerId.lastIndexOf('|'));
			String nameServerId = "server_id";
			if(key.equals("NEWUSER"))
			{
				Log.d("tag","New user received from GCM " + messageOuNewUserEtServerId);
				String valueServerId = messageOuNewUserEtServerId.substring(messageOuNewUserEtServerId.lastIndexOf('|') + 1);
				intent2open.putExtra("new_user", valueServerId);
			}
			else if(key.equals("LOSTUSER"))
			{
				Log.d("tag","Lost user received from GCM " + messageOuNewUserEtServerId);
				String valueServerId = messageOuNewUserEtServerId.substring(messageOuNewUserEtServerId.lastIndexOf('|') + 1);
				intent2open.putExtra("lost_user", valueServerId);
			}
			else
			{
				Log.d("tag","Message received from GCM " + messageOuNewUserEtServerId);
				String valueMsg = messageOuNewUserEtServerId.substring(messageOuNewUserEtServerId.lastIndexOf('|') + 1);
				intent2open.putExtra(nameServerId, key);
				intent2open.putExtra(nameMsg, valueMsg);
			}
			context.startActivity(intent2open);
		}
	}
}
