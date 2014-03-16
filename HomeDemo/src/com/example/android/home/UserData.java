package com.example.android.home;

import java.util.ArrayList;

public class UserData {

	public int backgroundImg;
	public int imageName;
	public String[] favpackNames;
	public String[] suggestPackageNames;
	
	private static String[] fav = {"com.android.chrome"
		//, "com.google.android.dialer"
		//, "com.facebook.katana"
		//, "com.android.chrome"
		//, "com.google.android.googlequicksearchbox"
		//, "com.example.android.home"
		
	}; //if any of them is not installed, skip it
	
	private static String[] page1sug = {
		"flipboard.app"
		, "com.espn.score_center"
		, "com.yahoo.mobile.client.android.finance"
		, "com.aol.mobile.techcrunch"
		, "com.radio.fmradio"
		, "com.starbucks.mobilecard"
		, "com.cnn.mobile.android.phone"
		, "com.outlook.Z7"
		, "com.google.android.email"
		
		//, "com.facebook.katana"
		//, "com.google.android.apps.docs"
	//	, "com.google.android.gm"
		, "com.accuweather.android"
		, "com.google.android.calendar"
		//, "com.meetup"
//		, "com.estrongs.android.pop"
		, "com.google.android.apps.finance"
		//, "com.yelp.android"
	}; //if any of them is not installed skip it
	
	private static String[] page2sug = {
		"com.whatsapp"
		, "com.yelp.android"
		, "com.ubercab"
		, "com.socialnmobile.dictapps.notepad.color.note"
		, "com.google.android.talk"
		//, "com.google.android.apps.googlevoice" 
		, "com.example.android.home"
		, "com.linkedin.android"
		, "com.google.android.street"
		, "com.meetup"
		, "com.google.android.calendar"
		, "com.twitter.android"
		
	}; //if any of them is not installed skip it
	
	
	private static String[] page3sug = {
		"com.whatsapp"
		, "com.twitter.android"
		, "com.facebook.katana"
		, "com.google.android.deskclock"
		, "com.amazon.kindle"
		
		//, "com.google.android.music"
		, "com.instagram.android"
		, "com.rovio.angrybirds"
		, "com.google.android.youtube"
		, "com.microsoft.skydrive"
		, "com.meetup"
		, "com.amazon.mShop.android"
		, "com.tripadvisor.tripadvisor"
		, "com.espn.score_center"
		
		
	}; //if any of them is not installed skip it
	
	public static ArrayList<UserData> GetUserPages()
	{
		ArrayList<UserData>  rv = new ArrayList<UserData>();
		UserData page1 = new UserData();
		
		page1.backgroundImg = R.drawable.page1;//assumes img is in res folder
		page1.imageName = R.drawable.page1top; //assume we have that in resource folder
		page1.favpackNames = fav; //if any of them is not installed, skip it
		page1.suggestPackageNames = page1sug; //if any of them is not installed skip it
		
		
		UserData page2 = new UserData();
		page2.backgroundImg = R.drawable.page2; //assumes img is in res folder
		page2.imageName = R.drawable.page2top; //assume we have that in resource folder
		page2.favpackNames = fav; //if any of them is not installed, skip it
		page2.suggestPackageNames = page2sug; //if any of them is not installed skip it
		
		
		UserData page3 = new UserData();
		page3.backgroundImg = R.drawable.blackbg; //assumes img is in res folder
		page3.imageName = R.drawable.massage; //assume we have that in resource folder
		page3.favpackNames = fav; //if any of them is not installed, skip it
		page3.suggestPackageNames = page3sug; //if any of them is not installed skip it
		
		rv.add(page1);
		rv.add(page2);
		rv.add(page3);
		
		return rv; 
	}
}
