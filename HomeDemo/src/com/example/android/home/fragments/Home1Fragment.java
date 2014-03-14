package com.example.android.home.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.home.ApplicationInfo;
import com.example.android.home.NormalApplicationsStackLayout;
import com.example.android.home.R;

public class Home1Fragment extends Fragment{

	/**
	 * Tag used for logging errors.
	 */
	private static final String LOG_TAG = "Home";   

	/**
	 * Maximum number of recent tasks to query.
	 */
	private static final int MAX_RECENT_TASKS = 20;

	private static boolean mWallpaperChecked;
	private static ArrayList<ApplicationInfo> mApplications;

	private final BroadcastReceiver mWallpaperReceiver = new WallpaperIntentReceiver();
	private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
	
	private NormalApplicationsStackLayout mNormalApplicationsStack1;
	private NormalApplicationsStackLayout mNormalApplicationsStack2;
	private NormalApplicationsStackLayout mNormalApplicationsStack3;
	private NormalApplicationsStackLayout mNormalApplicationsStack4;

	private ViewGroup mRootView;
	
	private ImageView mAdImageView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.home, container, false);

		mRootView = rootView;
		
		mAdImageView = (ImageView) rootView.findViewById(R.id.ad);
		mAdImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
            	startActivity(browserIntent);
            }
        });
		
		getActivity().setDefaultKeyMode(3);

		registerIntentReceivers();

		setDefaultWallpaper();

		loadApplications(true);

		bindApplications(rootView);
		bindRecents();
		
		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		getActivity().unregisterReceiver(mWallpaperReceiver);
		getActivity().unregisterReceiver(mApplicationsReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		bindRecents();
	}

	/**
	 * Registers various intent receivers. The current implementation registers
	 * only a wallpaper intent receiver to let other applications change the
	 * wallpaper.
	 */
	private void registerIntentReceivers() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
		getActivity().registerReceiver(mWallpaperReceiver, filter);

		filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		getActivity().registerReceiver(mApplicationsReceiver, filter);
	}

	/**
	 * Creates a new appplications adapter for the grid view and registers it.
	 */
	private void bindApplications(ViewGroup rootView) {
		
		if (mNormalApplicationsStack1 == null){
			mNormalApplicationsStack1 = (NormalApplicationsStackLayout) rootView.findViewById(R.id.normal_faves_and_recents1);
		}
		if (mNormalApplicationsStack2 == null){
			mNormalApplicationsStack2 = (NormalApplicationsStackLayout) rootView.findViewById(R.id.normal_faves_and_recents2);
		}
		if (mNormalApplicationsStack3 == null){
			mNormalApplicationsStack3 = (NormalApplicationsStackLayout) rootView.findViewById(R.id.normal_faves_and_recents3);
		}
		if (mNormalApplicationsStack4 == null) {
			mNormalApplicationsStack4 = (NormalApplicationsStackLayout) rootView.findViewById(R.id.normal_faves_and_recents4);
		}
	}

	/**
	 * When no wallpaper was manually set, a default wallpaper is used instead.
	 */
	private void setDefaultWallpaper() {
		if (!mWallpaperChecked) {
			Drawable wallpaper = getActivity().peekWallpaper();
			if (wallpaper == null) {
				try {
					getActivity().clearWallpaper();
				} catch (IOException e) {
					Log.e(LOG_TAG, "Failed to clear wallpaper " + e);
				}
			} else {
				getActivity().getWindow().setBackgroundDrawable(new ClippedDrawable(wallpaper));
			}
			mWallpaperChecked = true;
		}
	}

	/**
	 * Refreshes the recently launched applications stacked over the favorites. The number
	 * of recents depends on how many favorites are present.
	 */
	private void bindRecents() {
		/*final PackageManager manager = getActivity().getPackageManager();
		final ActivityManager tasksManager = (ActivityManager) getActivity().getSystemService(getActivity().ACTIVITY_SERVICE);
		final List<ActivityManager.RecentTaskInfo> recentTasks = tasksManager.getRecentTasks(
				MAX_RECENT_TASKS, 0);

		final int count = recentTasks.size();
		final ArrayList<ApplicationInfo> recents = new ArrayList<ApplicationInfo>();

		for (int i = count - 1; i >= 0; i--) {
			final Intent intent = recentTasks.get(i).baseIntent;

			if (Intent.ACTION_MAIN.equals(intent.getAction()) &&
					!intent.hasCategory(Intent.CATEGORY_HOME)) {

				ApplicationInfo info = getApplicationInfo(manager, intent);
				if (info != null) {
					info.intent = intent;
					//if (!mFavorites.contains(info)) {
						recents.add(info);
					//}
				}
			}
		}*/
		
		PackageManager manager = getActivity().getPackageManager();

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

		if (apps != null) {
			final int count = apps.size();

			if (mApplications == null) {
				mApplications = new ArrayList<ApplicationInfo>(count);
			}
			mApplications.clear();

			for (int i = 0; i < count; i++) {
				ApplicationInfo application = new ApplicationInfo();
				ResolveInfo info = apps.get(i);

				application.title = info.loadLabel(manager);
				application.setActivity(new ComponentName(
						info.activityInfo.applicationInfo.packageName,
						info.activityInfo.name),
						Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				application.icon = info.activityInfo.loadIcon(manager);

				mApplications.add(application);
			}
		}

		// TODO: fix get list from array.xml
		if (mApplications.size() > 40) mNormalApplicationsStack1.setRecents(mApplications.subList(30, 40));
		if (mApplications.size() > 30) mNormalApplicationsStack2.setRecents(mApplications.subList(20, 30));
		if (mApplications.size() > 20) mNormalApplicationsStack3.setRecents(mApplications.subList(10, 20));
		if (mApplications.size() > 10) mNormalApplicationsStack4.setRecents(mApplications.subList(0, 10));
	}

	private static ApplicationInfo getApplicationInfo(PackageManager manager, Intent intent) {
		final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

		if (resolveInfo == null) {
			return null;
		}

		final ApplicationInfo info = new ApplicationInfo();
		final ActivityInfo activityInfo = resolveInfo.activityInfo;
		info.icon = activityInfo.loadIcon(manager);
		if (info.title == null || info.title.length() == 0) {
			info.title = activityInfo.loadLabel(manager);
		}
		if (info.title == null) {
			info.title = "";
		}
		return info;
	}

	/**
	 * Loads the list of installed applications in mApplications.
	 */
	private void loadApplications(boolean isLaunching) {
		if (isLaunching && mApplications != null) {
			return;
		}

		PackageManager manager = getActivity().getPackageManager();

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

		if (apps != null) {
			final int count = apps.size();

			if (mApplications == null) {
				mApplications = new ArrayList<ApplicationInfo>(count);
			}
			mApplications.clear();

			for (int i = 0; i < count; i++) {
				ApplicationInfo application = new ApplicationInfo();
				ResolveInfo info = apps.get(i);

				application.title = info.loadLabel(manager);
				application.setActivity(new ComponentName(
						info.activityInfo.applicationInfo.packageName,
						info.activityInfo.name),
						Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				application.icon = info.activityInfo.loadIcon(manager);

				mApplications.add(application);
			}
		}
	}

	/**
	 * Receives intents from other applications to change the wallpaper.
	 */
	private class WallpaperIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			getActivity().getWindow().setBackgroundDrawable(new ClippedDrawable(getActivity().getWallpaper()));
		}
	}

	/**
	 * Receives notifications when applications are added/removed.
	 */
	private class ApplicationsIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			loadApplications(false);
			bindApplications(mRootView);
			bindRecents();
			//bindFavorites(false);
		}
	}

	/**
	 * When a drawable is attached to a View, the View gives the Drawable its dimensions
	 * by calling Drawable.setBounds(). In this application, the View that draws the
	 * wallpaper has the same size as the screen. However, the wallpaper might be larger
	 * that the screen which means it will be automatically stretched. Because stretching
	 * a bitmap while drawing it is very expensive, we use a ClippedDrawable instead.
	 * This drawable simply draws another wallpaper but makes sure it is not stretched
	 * by always giving it its intrinsic dimensions. If the wallpaper is larger than the
	 * screen, it will simply get clipped but it won't impact performance.
	 */
	private class ClippedDrawable extends Drawable {
		private final Drawable mWallpaper;

		public ClippedDrawable(Drawable wallpaper) {
			mWallpaper = wallpaper;
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			super.setBounds(left, top, right, bottom);
			// Ensure the wallpaper is as large as it really is, to avoid stretching it
			// at drawing time
			mWallpaper.setBounds(left, top, left + mWallpaper.getIntrinsicWidth(),
					top + mWallpaper.getIntrinsicHeight());
		}

		public void draw(Canvas canvas) {
			mWallpaper.draw(canvas);
		}

		public void setAlpha(int alpha) {
			mWallpaper.setAlpha(alpha);
		}

		public void setColorFilter(ColorFilter cf) {
			mWallpaper.setColorFilter(cf);
		}

		public int getOpacity() {
			return mWallpaper.getOpacity();
		}
	}
}
