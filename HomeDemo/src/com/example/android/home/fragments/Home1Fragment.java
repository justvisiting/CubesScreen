package com.example.android.home.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.home.ApplicationInfo;
import com.example.android.home.NormalApplicationsStackLayout;
import com.example.android.home.R;
import com.example.android.home.ScreenSlidePagerActivity;
import com.example.android.home.UserData;

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

	private NormalApplicationsStackLayout mNormalApplicationsFaves;

	private ViewGroup mRootView;

	private ImageView mAdImageView;

	private int mPosition;

	private View mShowApplications;
	private CheckBox mShowApplicationsCheck;
	private GridView mGrid;
	private LayoutAnimationController mShowLayoutAnimation;
	private LayoutAnimationController mHideLayoutAnimation;
	private Animation mGridEntry;
	private Animation mGridExit;

	private boolean mBlockAnimation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.home, container, false);

		mRootView = rootView;

		// Retrieve arguments
		Bundle arguments = getArguments();
		if (arguments != null) {
			mPosition = arguments.getInt(ScreenSlidePagerActivity.POSITION);
		}
		
		mGridEntry = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_entry);
		mGridExit = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_exit);

		mAdImageView = (ImageView) rootView.findViewById(R.id.ad);
		mAdImageView.setImageResource(UserData.GetUserPages().get(mPosition).imageName);
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
		

		rootView.setBackgroundResource(UserData.GetUserPages().get(mPosition).backgroundImg);
		return rootView;
	}

	/**
	 * Binds actions to the various buttons.
	 */
	private void bindButtons(ViewGroup rootView) {
		mShowApplications = rootView.findViewById(R.id.show_all_apps);
		mShowApplications.setOnClickListener(new ShowApplications());
		mShowApplicationsCheck = (CheckBox) rootView.findViewById(R.id.show_all_apps_check);

		mGrid.setOnItemClickListener(new ApplicationLauncher());
		
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
		//bindRecents();
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

		if (mGrid == null) {
			mGrid = (GridView) rootView.findViewById(R.id.all_apps);
		}
		mGrid.setAdapter(new ApplicationsAdapter(getActivity(), mApplications));
		mGrid.setSelection(0);

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
		if (mNormalApplicationsFaves == null) {
			mNormalApplicationsFaves = (NormalApplicationsStackLayout) rootView.findViewById(R.id.normal_faves);
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
				
				try
				{
				Context otherAppCtxt = getActivity().getBaseContext().createPackageContext(info.activityInfo.applicationInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
			    //info.drawableAppIcon = 
			    		application.icon = otherAppCtxt.getResources().getDrawableForDensity(info.activityInfo.applicationInfo.icon, DisplayMetrics.DENSITY_XHIGH);
				//application.icon = info.d .activityInfo.loadIcon(manager);
				} catch (PackageManager.NameNotFoundException e) {
				    e.printStackTrace();
				}
				mApplications.add(application);
			}
		}
		
		ArrayList<ApplicationInfo> selectedApps = new ArrayList<ApplicationInfo>();
		UserData userData = UserData.GetUserPages().get(mPosition);
		String[] suggestPackageNames = userData.suggestPackageNames;
		
		for (int i = 0; i < suggestPackageNames.length; i++){
			String suggestPackageName = suggestPackageNames[i];
			for (int j = 0; j < mApplications.size(); j++){
				String appPackageName = mApplications.get(j).intent.getComponent().getClassName();
				if (appPackageName.contains(suggestPackageName)){
					selectedApps.add(mApplications.get(j));
				}
			}
		}
		int test = 1;
		if (selectedApps.size() > 12){
			test = 4;
		}
		else if (selectedApps.size() > 8){
			test = 3;
			//mNormalApplicationsStack4.setVisibility(View.GONE);
		}
		else if (selectedApps.size() > 4){
			test = 2;
			//mNormalApplicationsStack4.setVisibility(View.GONE);
			//mNormalApplicationsStack3.setVisibility(View.GONE);
		}
		else if (selectedApps.size() > 0){
			test = 1;
			//mNormalApplicationsStack4.setVisibility(View.GONE);
			//mNormalApplicationsStack3.setVisibility(View.GONE);
			//mNormalApplicationsStack2.setVisibility(View.GONE);
		}
		
		Log.e(getClass().getSimpleName(), "test ="+test);

		if (test == 4){
			mNormalApplicationsStack4.setRecents(mApplications, 4, mPosition, true);
			bindButtons(mRootView);
		}
		else{
			mNormalApplicationsStack4.setRecents(mApplications, 4, mPosition, false);
		}
		
		if (test == 3){
			mNormalApplicationsStack3.setRecents(mApplications, 3, mPosition, true);
			bindButtons(mRootView);
		}
		else{
			mNormalApplicationsStack3.setRecents(mApplications, 3, mPosition, false);
		}
		
		if (test == 2){
			mNormalApplicationsStack2.setRecents(mApplications, 2, mPosition, true);
			bindButtons(mRootView);
		}
		else{
			mNormalApplicationsStack2.setRecents(mApplications, 2, mPosition, false);
		}
		
		if (test == 1){
			mNormalApplicationsStack1.setRecents(mApplications, 1, mPosition, true);
			bindButtons(mRootView);
		}
		else{
			mNormalApplicationsStack1.setRecents(mApplications, 1, mPosition, false);
		}
		
		
		mNormalApplicationsFaves.setFavorites(mApplications, 1, mPosition, false);
	}

	public ArrayList<ApplicationInfo> getListApplication(){
		return null;
	}

	private ApplicationInfo getApplicationInfo(PackageManager manager, Intent intent) {
		final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

		if (resolveInfo == null) {
			return null;
		}

		final ApplicationInfo info = new ApplicationInfo();
		final ActivityInfo activityInfo = resolveInfo.activityInfo;

        try {
        Context otherAppCtxt = getActivity().getBaseContext().createPackageContext(activityInfo.applicationInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
        //info.drawableAppIcon =
        info.icon = otherAppCtxt.getResources().getDrawableForDensity(activityInfo.applicationInfo.icon, DisplayMetrics.DENSITY_XHIGH);
        //application.icon = info.d .activityInfo.loadIcon(manager);
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }


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

	/**
	 * GridView adapter to show the list of all installed applications.
	 */
	private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
		private Rect mOldBounds = new Rect();

		public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
			super(context, 0, apps);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ApplicationInfo info = mApplications.get(position);

			if (convertView == null) {
				final LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(R.layout.application, parent, false);
			}

			Drawable icon = info.icon;

			if (!info.filtered) {
				final Resources resources = getContext().getResources();
				int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
				int height = (int) resources.getDimension(android.R.dimen.app_icon_size);

				final int iconWidth = icon.getIntrinsicWidth();
				final int iconHeight = icon.getIntrinsicHeight();

				if (icon instanceof PaintDrawable) {
					PaintDrawable painter = (PaintDrawable) icon;
					painter.setIntrinsicWidth(width);
					painter.setIntrinsicHeight(height);
				}

				if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
					final float ratio = (float) iconWidth / iconHeight;

					if (iconWidth > iconHeight) {
						height = (int) (width / ratio);
					} else if (iconHeight > iconWidth) {
						width = (int) (height * ratio);
					}

					final Bitmap.Config c =
							icon.getOpacity() != PixelFormat.OPAQUE ?
									Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
					final Bitmap thumb = Bitmap.createBitmap(width, height, c);
					final Canvas canvas = new Canvas(thumb);
					canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
					// Copy the old bounds to restore them later
					// If we were to do oldBounds = icon.getBounds(),
					// the call to setBounds() that follows would
					// change the same instance and we would lose the
					// old bounds
					mOldBounds.set(icon.getBounds());
					icon.setBounds(0, 0, width, height);
					icon.draw(canvas);
					icon.setBounds(mOldBounds);
					icon = info.icon = new BitmapDrawable(thumb);
					info.filtered = true;
				}
			}

			final TextView textView = (TextView) convertView.findViewById(R.id.label);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
			textView.setText(info.title);

			return convertView;
		}
	}

	/**
	 * Shows and hides the applications grid view.
	 */
	private class ShowApplications implements View.OnClickListener {
		public void onClick(View v) {
			if (mGrid.getVisibility() != View.VISIBLE) {
				showApplications(true);
			} else {
				hideApplications();
			}
		}
	}

	/**
	 * Hides the applications grid when the layout animation is over.
	 */
	private class HideGrid implements Animation.AnimationListener {
		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
			mBlockAnimation = false;
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}

	/**
	 * Shows the applications grid when the layout animation is over.
	 */
	private class ShowGrid implements Animation.AnimationListener {
		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
			mBlockAnimation = false;
			// ViewDebug.stopHierarchyTracing();
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}

	/**
	 * Starts the selected activity/application in the grid view.
	 */
	private class ApplicationLauncher implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
			startActivity(app.intent);
		}
	}

	/**
	 * Shows all of the applications by playing an animation on the grid.
	 */
	private void showApplications(boolean animate) {
		if (mBlockAnimation) {
			return;
		}
		mBlockAnimation = true;

		mShowApplicationsCheck.toggle();

		if (mShowLayoutAnimation == null) {
			mShowLayoutAnimation = AnimationUtils.loadLayoutAnimation(
					getActivity(), R.anim.show_applications);
		}

		// This enables a layout animation; if you uncomment this code, you need to
		// comment the line mGrid.startAnimation() below
		//        mGrid.setLayoutAnimationListener(new ShowGrid());
		//        mGrid.setLayoutAnimation(mShowLayoutAnimation);
		//        mGrid.startLayoutAnimation();

		if (animate) {
			mGridEntry.setAnimationListener(new ShowGrid());
			mGrid.startAnimation(mGridEntry);
		}

		mGrid.setVisibility(View.VISIBLE);

		if (!animate) {
			mBlockAnimation = false;
		}

		// ViewDebug.startHierarchyTracing("Home", mGrid);
	}

	/**
	 * Hides all of the applications by playing an animation on the grid.
	 */
	private void hideApplications() {
		if (mBlockAnimation) {
			return;
		}
		mBlockAnimation = true;

		mShowApplicationsCheck.toggle();

		if (mHideLayoutAnimation == null) {
			mHideLayoutAnimation = AnimationUtils.loadLayoutAnimation(
					getActivity(), R.anim.hide_applications);
		}

		mGridExit.setAnimationListener(new HideGrid());
		mGrid.startAnimation(mGridExit);
		mGrid.setVisibility(View.INVISIBLE);
		mShowApplications.requestFocus();

		// This enables a layout animation; if you uncomment this code, you need to
		// comment the line mGrid.startAnimation() above
		//        mGrid.setLayoutAnimationListener(new HideGrid());
		//        mGrid.setLayoutAnimation(mHideLayoutAnimation);
		//        mGrid.startLayoutAnimation();
	}
}
