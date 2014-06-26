package de.xikolo.controller;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieSyncManager;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controller.fragments.ContentFragment;
import de.xikolo.controller.fragments.CourseListFragment;
import de.xikolo.controller.fragments.DownloadsFragment;
import de.xikolo.controller.fragments.ProfileFragment;
import de.xikolo.controller.fragments.SettingsFragment;
import de.xikolo.controller.fragments.WebViewFragment;
import de.xikolo.controller.navigation.NavigationFragment;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.util.Config;


public class MainActivity extends FragmentActivity
        implements NavigationFragment.NavigationDrawerCallbacks, ContentFragment.OnFragmentInteractionListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationFragment mNavigationFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private ContentFragment mFragment;

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationFragment = (NavigationFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mActionBar = getActionBar();

        Log.d(TAG, "Build Type: " + BuildConfig.buildType);
        Log.d(TAG, "Build Flavor: " + BuildConfig.buildFlavor);
    }

    @Override
    public void attachFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onTopLevelFragmentAttached(int id, String title) {
        setTitle(title);
        mNavigationFragment.markItem(id);
        mNavigationFragment.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void onLowLevelFragmentAttached(int id, String title) {
        setTitle(title);
        mNavigationFragment.markItem(id);
        mNavigationFragment.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case NavigationAdapter.NAV_ID_PROFILE:
                mFragment = ProfileFragment.newInstance();
                break;
            case NavigationAdapter.NAV_ID_ALL_COURSES:
                mFragment = CourseListFragment.newInstance(CourseListFragment.FILTER_ALL);
                break;
            case NavigationAdapter.NAV_ID_MY_COURSES:
                mFragment = CourseListFragment.newInstance(CourseListFragment.FILTER_MY);
                break;
            case NavigationAdapter.NAV_ID_NEWS:
                mFragment = WebViewFragment.newInstance(Config.URI_HPI + Config.PATH_NEWS, true, null);
                break;
            case NavigationAdapter.NAV_ID_DOWNLOADS:
                mFragment = DownloadsFragment.newInstance();
                break;
            case NavigationAdapter.NAV_ID_SETTINGS:
                mFragment = SettingsFragment.newInstance();
                break;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, mFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setTitle(String title) {
        mTitle = title;
        if (getActionBar() != null) {
            getActionBar().setTitle(mTitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationFragment.isDrawerOpen()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1)
                getSupportFragmentManager().popBackStack();
            else
                finish();
        } else {
            mNavigationFragment.closeDrawer();
        }
    }

    public NavigationFragment getNavigationDrawer() {
        return this.mNavigationFragment;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isDrawerOpen() {
        return this.mNavigationFragment.isDrawerOpen();
    }

    @Override
    public void updateDrawer() {
        this.mNavigationFragment.updateDrawer();
    }

    @Override
    public void toggleDrawer(int pos) {
        this.mNavigationFragment.selectItem(pos);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().sync();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalApplication app = (GlobalApplication) getApplicationContext();
        app.flushCache();
    }

}