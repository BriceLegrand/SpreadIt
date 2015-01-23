package com.spreadit.utils;

import java.io.File;

import android.content.Context;

public class ClearCacheDataUtils
{	
	public static void trimCache(Context context) 
	{
		try 
		{
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) 
			{
				deleteDir(dir);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}


	public static boolean deleteDir(File dir) 
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
