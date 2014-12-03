package com.example.spreadpocv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        String newMessage = intent.getExtras().getString("msg");
        Log.d("tag","Message received from GCM " + newMessage);
        
        Intent intent2open = new Intent(context, MainActivity.class);
        intent2open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String name = "msg";
        String value = newMessage;
        intent2open.putExtra(name, value);
        context.startActivity(intent2open);
        
        
	}
	
}
