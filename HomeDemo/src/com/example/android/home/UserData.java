package com.example.android.home;

import java.util.ArrayList;

import android.R.color;

public class UserData {

	public String backgroundImg;
	public String imageName;
	public String[] favpackNames;
	public String[] suggestPackageNames;
	
	private static String[] fav = {"com.android.chrome", "com.google.android.dialer"}; //if any of them is not installed, skip it
	private static String[] page1sug = {"com.facebook.katana"
		, "com.google.android.apps.docs"
		, "com.google.android.gm"
		, "com.example.android.home"
		, "com.google.android.googlequicksearchbox"
		, "com.meetup"
		, "com.tul.aviate"
		, "com.estrongs.android.pop"

	}; //if any of them is not installed skip it
	
	private static String[] page2sug = {"com.whatsapp", "com.example.android.home", "com.meetup"}; //if any of them is not installed skip it
	
	public static ArrayList<UserData> GetUserPages()
	{
		ArrayList<UserData>  rv = new ArrayList<UserData>();
		UserData page1 = new UserData();
		page1.backgroundImg = "page1.png";//assumes img is in res folder
		page1.imageName = "page1top.png"; //assume we have that in resource folder
		page1.favpackNames = fav; //if any of them is not installed, skip it
		page1.suggestPackageNames = page1sug; //if any of them is not installed skip it
		
		UserData page2 = new UserData();
		page2.backgroundImg = "page2.png"; //assumes img is in res folder
		page2.imageName = "page2top.png"; //assume we have that in resource folder
		page2.favpackNames = fav; //if any of them is not installed, skip it
		page2.suggestPackageNames = page2sug; //if any of them is not installed skip it
		
		rv.add(page1);
		rv.add(page2);
		
		return rv; 
	}
}
