package com.spreadit.utils;

import java.io.File;

import android.app.Application;
import android.content.Context;

public class ClearCacheDataUtils extends Application
{
	private static ClearCacheDataUtils instance;

	@Override
	public void onCreate() 
	{
		super.onCreate();
		instance = this;
	}

	public static ClearCacheDataUtils getInstance()
	{
		return instance;
	}
	
	public void trimCache() 
	{
		try 
		{
			File dir = getCacheDir();
			if (dir != null && dir.isDirectory()) 
			{
				deleteDir(dir);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}


	public boolean deleteDir(File dir) 
	{
		if (dir != null && dir.isDirectory()) 
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) 
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
				{
					return false;
				}
			}
		}

		return dir.delete();
	}
}
