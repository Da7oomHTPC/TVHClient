package org.tvheadend.tvhclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import org.tvheadend.tvhclient.R;
import org.tvheadend.tvhclient.TVHClientApplication;
import org.tvheadend.tvhclient.data.tasks.WakeOnLanTaskCallback;
import org.tvheadend.tvhclient.ui.channels.ChannelListFragment;
import org.tvheadend.tvhclient.ui.epg.ProgramGuideViewPagerFragment;
import org.tvheadend.tvhclient.ui.misc.InfoFragment;
import org.tvheadend.tvhclient.ui.misc.StatusFragment;
import org.tvheadend.tvhclient.ui.misc.UnlockerFragment;
import org.tvheadend.tvhclient.ui.dvr.recordings.CompletedRecordingListFragment;
import org.tvheadend.tvhclient.ui.dvr.recordings.FailedRecordingListFragment;
import org.tvheadend.tvhclient.ui.dvr.recordings.RemovedRecordingListFragment;
import org.tvheadend.tvhclient.ui.dvr.recordings.ScheduledRecordingListFragment;
import org.tvheadend.tvhclient.ui.dvr.series_recordings.SeriesRecordingListFragment;
import org.tvheadend.tvhclient.ui.dvr.timer_recordings.TimerRecordingListFragment;
import org.tvheadend.tvhclient.ui.settings.SettingsActivity;

// TODO make nav image blasser
// TODO icons in dual pane mode
// TODO show confirmation of added/edited recordings...

public class NavigationActivity extends MainActivity implements WakeOnLanTaskCallback, NavigationDrawerCallback {

    // Default navigation drawer menu position and the list positions
    private int selectedNavigationMenuId = NavigationDrawer.MENU_UNKNOWN;
    private NavigationDrawer navigationDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        navigationDrawer = new NavigationDrawer(this, savedInstanceState, toolbar, this);
        navigationDrawer.createHeader();
        navigationDrawer.createMenu();

        if (savedInstanceState == null) {
            // When the activity is created it got called by the main activity. Get the initial
            // navigation menu position and show the associated fragment with it. When the device
            // was rotated just restore the position from the saved instance.
            selectedNavigationMenuId = getIntent().getIntExtra("navigation_menu_position", NavigationDrawer.MENU_CHANNELS);
            if (selectedNavigationMenuId >= 0) {
                handleDrawerItemSelected(selectedNavigationMenuId);
            }
        } else {
            selectedNavigationMenuId = savedInstanceState.getInt("navigation_menu_position", NavigationDrawer.MENU_CHANNELS);
        }
    }

    /**
     * Called when a menu item from the navigation drawer was selected. It loads
     * and shows the correct fragment or fragments depending on the selected
     * menu item.
     *
     * @param position Selected position within the menu array
     */
    private void handleDrawerItemSelected(int position) {

        Intent intent;
        Fragment fragment = null;
        switch (position) {
            case NavigationDrawer.MENU_CHANNELS:
                fragment = new ChannelListFragment();
                break;
            case NavigationDrawer.MENU_PROGRAM_GUIDE:
                fragment = new ProgramGuideViewPagerFragment();
                break;
            case NavigationDrawer.MENU_COMPLETED_RECORDINGS:
                fragment = new CompletedRecordingListFragment();
                break;
            case NavigationDrawer.MENU_SCHEDULED_RECORDINGS:
                fragment = new ScheduledRecordingListFragment();
                break;
            case NavigationDrawer.MENU_SERIES_RECORDINGS:
                fragment = new SeriesRecordingListFragment();
                break;
            case NavigationDrawer.MENU_TIMER_RECORDINGS:
                fragment = new TimerRecordingListFragment();
                break;
            case NavigationDrawer.MENU_FAILED_RECORDINGS:
                fragment = new FailedRecordingListFragment();
                break;
            case NavigationDrawer.MENU_REMOVED_RECORDINGS:
                fragment = new RemovedRecordingListFragment();
                break;
            case NavigationDrawer.MENU_STATUS:
                fragment = new StatusFragment();
                break;
            case NavigationDrawer.MENU_INFORMATION:
                fragment = new InfoFragment();
                break;
            case NavigationDrawer.MENU_SETTINGS:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case NavigationDrawer.MENU_UNLOCKER:
                fragment = new UnlockerFragment();
                break;
        }

        if (fragment != null) {
            // Save the menu position so we know which one was selected
            selectedNavigationMenuId = position;
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.main, fragment).commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        switch (selectedNavigationMenuId) {
            case NavigationDrawer.MENU_STATUS:
            case NavigationDrawer.MENU_INFORMATION:
            case NavigationDrawer.MENU_UNLOCKER:
                MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
                if (mediaRouteMenuItem != null) {
                    mediaRouteMenuItem.setVisible(false);
                }
                MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
                if (searchMenuItem != null) {
                    searchMenuItem.setVisible(false);
                }
                MenuItem reconnectMenuItem = menu.findItem(R.id.menu_refresh);
                if (reconnectMenuItem != null) {
                    reconnectMenuItem.setVisible(false);
                }
                break;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // A connection exists and is active, register to receive
        // information from the server and connect to the server
        TVHClientApplication.getInstance().addListener(navigationDrawer);
        // Show the defined fragment from the menu position or the
        // status if the connection state is not fine
        handleDrawerItemSelected(selectedNavigationMenuId);

        // Update the drawer menu so that all available menu items are
        // shown in case the recording counts have changed or the user has
        // bought the unlocked version to enable all features
        navigationDrawer.updateDrawerItemBadges();
        navigationDrawer.updateDrawerHeader();
    }

    @Override
    public void onPause() {
        super.onPause();
        TVHClientApplication.getInstance().removeListener(navigationDrawer);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // add the values which need to be saved from the drawer to the bundle
        outState = navigationDrawer.getDrawer().saveInstanceState(outState);
        // add the values which need to be saved from the accountHeader to the bundle
        outState = navigationDrawer.getHeader().saveInstanceState(outState);
        outState.putInt("navigation_menu_position", selectedNavigationMenuId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void notify(String message) {
        if (getCurrentFocus() != null) {
            Snackbar.make(getCurrentFocus(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNavigationMenuSelected(int id) {
        if (selectedNavigationMenuId != id) {
            handleDrawerItemSelected(id);
        }
    }
}