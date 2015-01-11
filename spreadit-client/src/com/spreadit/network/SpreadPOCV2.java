package com.spreadit.network;

import android.app.Application;
import android.content.Context;

public class SpreadPOCV2 extends Application {
	 private static Context context;

	    public void onCreate(){
	        super.onCreate();
	        SpreadPOCV2.context = getApplicationContext();
	    }

	    public static Context getAppContext() {
	        return SpreadPOCV2.context;
	    }
}
