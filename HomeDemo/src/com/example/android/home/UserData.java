package com.example.android.home;

import java.util.ArrayList;

import android.R.color;

public class UserData {

	public int backgrouncColor;
	public String imageName;
	public String[] favpackNames;
	public String[] suggestPackageNames;
	
	private static String[] page1fav = {"com.google.maps", "com.google.phone"}; //if any of them is not installed, skip it
	private static String[] page1sug = {"com.google.skype", "com.google.outlook"}; //if any of them is not installed skip it
	
	private static String[] page2fav = {"com.google.maps", "com.google.phone"}; //if any of them is not installed, skip it
	private static String[] page2sug = {"com.google.skype", "com.google.outlook"}; //if any of them is not installed skip it
	
	public static ArrayList<UserData> GetUserPages()
	{
		ArrayList<UserData>  rv = new ArrayList<UserData>();
		UserData page1 = new UserData();
		page1.backgrouncColor = color.holo_blue_light;
		page1.imageName = "page1.jpg"; //assume we have that in resource folder
		page1.favpackNames = page1fav; //if any of them is not installed, skip it
		page1.suggestPackageNames = page1sug; //if any of them is not installed skip it
		
		UserData page2 = new UserData();
		page2.backgrouncColor = color.holo_blue_light;
		page2.imageName = "page1.jpg"; //assume we have that in resource folder
		page2.favpackNames = page1fav; //if any of them is not installed, skip it
		page2.suggestPackageNames = page1sug; //if any of them is not installed skip it
		
		rv.add(page1);
		rv.add(page2);
		
		return rv; 
	}
}
