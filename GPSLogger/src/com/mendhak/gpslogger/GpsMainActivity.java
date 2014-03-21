/*
*    This file is part of GPSLogger for Android.
*
*    GPSLogger for Android is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 2 of the License, or
*    (at your option) any later version.
*
*    GPSLogger for Android is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
*/

//TODO: Move GPSMain email now call to gpsmain to allow closing of progress bar

package com.mendhak.gpslogger;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.Session;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.loggers.LocationLoggerFactory;
import com.mendhak.gpslogger.loggers.ILocationLogger;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class GpsMainActivity extends SherlockActivity implements OnCheckedChangeListener,
        IGpsLoggerServiceClient, View.OnClickListener, IActionListener
{

    /**
     * General all purpose handler used for updating the UI from threads.
     */
    private static Intent serviceIntent;

    /**
     * Provides a connection to the GPS Logging Service
     */
    private final ServiceConnection gpsServiceConnection = new ServiceConnection()
    {

        public void onServiceDisconnected(ComponentName name)
        {
            loggingService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service)
        {
            loggingService = ((GpsLoggingService.GpsLoggingBinder) service).getService();
            GpsLoggingService.setServiceClient(GpsMainActivity.this);


            Button buttonSinglePoint = (Button) findViewById(R.id.buttonSinglePoint);

            buttonSinglePoint.setOnClickListener(GpsMainActivity.this);

            if (Session.isStarted())
            {
                if (Session.isSinglePointMode())
                {
                    setMainButtonEnabled(false);
                }
                else
                {
                    setMainButtonChecked(true);
                    setSinglePointButtonEnabled(false);
                }

                displayLocationInfo(Session.getCurrentLocationInfo());
            }

            // Form setup - toggle button, display existing location info
            ToggleButton buttonOnOff = (ToggleButton) findViewById(R.id.buttonOnOff);
            buttonOnOff.setOnCheckedChangeListener(GpsMainActivity.this);

            if (Session.hasDescription())
                OnSetAnnotation();
        }
    };

    private GpsLoggingService loggingService;
    private MenuItem mnuAnnotate;
    private Menu menu;

    /**
     * Event raised when the form is created for the first time
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        Utilities.LogDebug("GpsMainActivity.onCreate");

        super.onCreate(savedInstanceState);

        Utilities.LogInfo("GPSLogger started");

        setContentView(R.layout.main);

        // Moved to onResume to update the list of loggers
        //getPreferences();

        startAndBindService();
        ShowWhatsNew();
    }

    private void ShowWhatsNew()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int currentVersionNumber = 0;

        int savedVersionNumber = prefs.getInt("SAVED_VERSION", 0);

        try
        {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        }
        catch (Exception e) {}

        if (currentVersionNumber > savedVersionNumber)
        {
            SharedPreferences.Editor editor   = prefs.edit();
            editor.putInt("SAVED_VERSION", currentVersionNumber);
            editor.commit();
        }
    }

    @Override
    protected void onStart()
    {
        Utilities.LogDebug("GpsMainActivity.onStart");
        super.onStart();
        startAndBindService();
    }

    @Override
    protected void onResume()
    {
        Utilities.LogDebug("GpsMainactivity.onResume");
        super.onResume();
        getPreferences();
        startAndBindService();

        enableDisableMenuItems();
    }

    /**
     * Starts the service and binds the activity to it.
     */
    private void startAndBindService()
    {
        Utilities.LogDebug("startAndBindService - binding now");

        serviceIntent = new Intent(this, GpsLoggingService.class);
        // Start the service in case it isn't already running
        startService(serviceIntent);
        // Now bind to service
        bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
        Session.setBoundToService(true);
    }

    /**
     * Stops the service if it isn't logging. Also unbinds.
     */
    private void stopAndUnbindServiceIfRequired()
    {
        Utilities.LogDebug("GpsMainActivity.stopAndUnbindServiceIfRequired");

        if (Session.isBoundToService())
        {
            unbindService(gpsServiceConnection);
            Session.setBoundToService(false);
        }

        if (!Session.isStarted())
        {
            Utilities.LogDebug("StopServiceIfRequired - Stopping the service");
            //serviceIntent = new Intent(this, GpsLoggingService.class);
            stopService(serviceIntent);
        }

    }

    @Override
    protected void onPause()
    {
        Utilities.LogDebug("GpsMainActivity.onPause");

        stopAndUnbindServiceIfRequired();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        Utilities.LogDebug("GpsMainActivity.onDestroy");

        stopAndUnbindServiceIfRequired();
        super.onDestroy();
    }

    /**
     * Called when the toggle button is clicked
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        Utilities.LogDebug("GpsMainActivity.onCheckedChanged");

        if (isChecked)
        {
            getPreferences();
            setSinglePointButtonEnabled(false);
            loggingService.setupAutoSendTimers();
            loggingService.startLogging();
        }
        else
        {
            setSinglePointButtonEnabled(true);
            loggingService.stopLogging();
        }
    }

    /**
     * Called when the single point button is clicked
     */
    public void onClick(View view)
    {
        Utilities.LogDebug("GpsMainActivity.onClick");

        if (!Session.isStarted())
        {
            setMainButtonEnabled(false);
            loggingService.startLogging();
            Session.setSinglePointMode(true);
        }
        else if (Session.isStarted() && Session.isSinglePointMode())
        {
            loggingService.stopLogging();
            setMainButtonEnabled(true);
            Session.setSinglePointMode(false);
        }
    }

    public void setSinglePointButtonEnabled(boolean enabled)
    {
        Button buttonSinglePoint = (Button) findViewById(R.id.buttonSinglePoint);
        buttonSinglePoint.setEnabled(enabled);
    }

    public void setAnnotationButtonMarked(boolean marked)
    {
        if (marked)
        {
            mnuAnnotate.setIcon(R.drawable.ic_menu_edit_active);
        }
        else
        {
            mnuAnnotate.setIcon(R.drawable.ic_menu_edit);
        }
    }

    public void setMainButtonEnabled(boolean enabled)
    {
        ToggleButton buttonOnOff = (ToggleButton) findViewById(R.id.buttonOnOff);
        buttonOnOff.setEnabled(enabled);
    }

    public void setMainButtonChecked(boolean checked)
    {
        ToggleButton buttonOnOff = (ToggleButton) findViewById(R.id.buttonOnOff);
        buttonOnOff.setChecked(checked);
    }

    /**
     * Gets preferences chosen by the user
     */
    private void getPreferences()
    {
        Utilities.PopulateAppSettings(getApplicationContext());
        showPreferencesSummary();
    }

    /**
     * Displays a human readable summary of the preferences chosen by the user
     * on the main form
     */
    private void showPreferencesSummary()
    {
        Utilities.LogDebug("GpsMainActivity.showPreferencesSummary");
        try
        {
            TextView txtLoggingTo = (TextView) findViewById(R.id.txtLoggingTo);
            TextView txtFrequency = (TextView) findViewById(R.id.txtFrequency);
            TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
            TextView txtAutoEmail = (TextView) findViewById(R.id.txtAutoEmail);

            List<ILocationLogger> loggers = LocationLoggerFactory.getLoggers(this);

            if (loggers.size() > 0)
            {

                ListIterator<ILocationLogger> li = loggers.listIterator();
                String logTo = li.next().getName();
                while (li.hasNext())
                {
                    logTo += ", " + li.next().getName();
                }
                txtLoggingTo.setText(logTo);

            }
            else
            {
                txtLoggingTo.setText(R.string.summary_loggingto_screen);
            }

            if (AppSettings.getMinimumSeconds() > 0)
            {
                String descriptiveTime = Utilities.GetDescriptiveTimeString(AppSettings.getMinimumSeconds(),
                        getApplicationContext());

                txtFrequency.setText(descriptiveTime);
            }
            else
            {
                txtFrequency.setText(R.string.summary_freq_max);
            }


            if (AppSettings.getMinimumDistanceInMeters() > 0)
            {
                if (AppSettings.shouldUseImperial())
                {
                    int minimumDistanceInFeet = Utilities.MetersToFeet(AppSettings.getMinimumDistanceInMeters());
                    txtDistance.setText(((minimumDistanceInFeet == 1)
                            ? getString(R.string.foot)
                            : String.valueOf(minimumDistanceInFeet) + getString(R.string.feet)));
                }
                else
                {
                    txtDistance.setText(((AppSettings.getMinimumDistanceInMeters() == 1)
                            ? getString(R.string.meter)
                            : String.valueOf(AppSettings.getMinimumDistanceInMeters()) + getString(R.string.meters)));
                }
            }
            else
            {
                txtDistance.setText(R.string.summary_dist_regardless);
            }


            if (AppSettings.isAutoSendEnabled())
            {
                String autoEmailResx;

                if (AppSettings.getAutoSendDelay() == 0)
                {
                    autoEmailResx = "autoemail_frequency_whenistop";
                }
                else
                {

                    autoEmailResx = "autoemail_frequency_"
                            + String.valueOf(AppSettings.getAutoSendDelay()).replace(".", "");
                }

                String autoEmailDesc = getString(getResources().getIdentifier(autoEmailResx, "string", getPackageName()));

                txtAutoEmail.setText(autoEmailDesc);
            }
            else
            {
                TableRow trAutoEmail = (TableRow) findViewById(R.id.trAutoEmail);
                trAutoEmail.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception ex)
        {
            Utilities.LogError("showPreferencesSummary", ex);
        }
    }

    /**
     * Handles the hardware back-button press
     */
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Utilities.LogInfo("KeyDown - " + String.valueOf(keyCode));

        if (keyCode == KeyEvent.KEYCODE_BACK && Session.isBoundToService())
        {
            stopAndUnbindServiceIfRequired();
        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_MENU){
            Utilities.LogInfo("KeyUp Menu");
            this.menu.performIdentifierAction(R.id.mnuOverflow,0);
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Called when the menu is created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.optionsmenu, menu);
        mnuAnnotate = menu.findItem(R.id.mnuAnnotate);
        enableDisableMenuItems();
        this.menu = menu;

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        return true;
    }

    /**
     * Called when one of the menu items is selected.
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();

        Utilities.LogInfo("Option item selected - " + String.valueOf(item.getTitle()));

        switch (itemId)
        {
            case R.id.mnuSettings:
                Intent settingsActivity = new Intent(getApplicationContext(), GpsSettingsActivity.class);
                startActivity(settingsActivity);
                break;
            case R.id.mnuAnnotate:
                annotate();
                break;
            case R.id.mnuEmailnow:
                publishNow();
                break;
            case R.id.mnuFAQ:
                Intent faqtivity = new Intent(getApplicationContext(), Faqtivity.class);
                startActivity(faqtivity);
                break;
           case R.id.mnuLocationHistory:
                Intent locationHistory = new Intent(getApplicationContext(), LocationHistoryActivity.class);
                startActivity(locationHistory);
                break;
            case R.id.mnuExit:
                loggingService.stopLogging();
                loggingService.stopSelf();
                finish();
                break;
        }
        return false;
    }

    private void publishNow()
    {
        Utilities.LogDebug("GpsMainActivity.publishNow");

        if (AppSettings.isAutoSendEnabled())
        {
            loggingService.forcePublish();
        }
        else
        {
            Intent pref = new Intent().setClass(this, GpsSettingsActivity.class);
            pref.putExtra("autosend_preferencescreen", true);
            startActivity(pref);
        }

    }

    /**
     * Collect a single point and annotate with a description.
     */
    private void annotate()
    {
        Utilities.LogDebug("GpsMainActivity.annotate");

        AlertDialog.Builder alert = new AlertDialog.Builder(GpsMainActivity.this);

        alert.setTitle(R.string.add_description);
        alert.setMessage(R.string.letters_numbers);

        // Set an EditText view to get user input
        final EditText input = new EditText(getApplicationContext());
        input.setText(Session.getDescription());
        alert.setView(input);

        /* ok */
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
            	final String desc = Utilities.CleanDescription(input.getText().toString());
                if (desc.length() == 0)
                {
                    Session.clearDescription();
                    OnClearAnnotation();
                }
                else
                {
                    Session.setDescription(desc);
                    OnSetAnnotation();
                    if (!Session.isStarted()) // logOnce will start single point mode.
                        setMainButtonEnabled(false);
                    loggingService.logOnce();
                }
            }

        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // Cancelled.
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        //alert.show();
    }

    /**
     * Clears the table, removes all values.
     */
    @Override
    public void ClearForm()
    {
        Utilities.LogDebug("GpsMainActivity.ClearForm");

        TextView tvLatitude = (TextView) findViewById(R.id.txtLatitude);
        TextView tvLongitude = (TextView) findViewById(R.id.txtLongitude);
        TextView tvDateTime = (TextView) findViewById(R.id.txtDateTimeAndProvider);

        TextView tvAltitude = (TextView) findViewById(R.id.txtAltitude);

        TextView txtSpeed = (TextView) findViewById(R.id.txtSpeed);

        TextView txtSatellites = (TextView) findViewById(R.id.txtSatellites);
        TextView txtDirection = (TextView) findViewById(R.id.txtDirection);
        TextView txtAccuracy = (TextView) findViewById(R.id.txtAccuracy);
        TextView txtDistance = (TextView) findViewById(R.id.txtDistanceTravelled);

        tvLatitude.setText("");
        tvLongitude.setText("");
        tvDateTime.setText("");
        tvAltitude.setText("");
        txtSpeed.setText("");
        txtSatellites.setText("");
        txtDirection.setText("");
        txtAccuracy.setText("");
        txtDistance.setText("");
        Session.setPreviousLocationInfo(null);
        Session.setTotalTravelled(0d);
    }

    @Override
    public void OnStopLogging()
    {
        Utilities.LogDebug("GpsMainActivity.OnStopLogging");

        setMainButtonChecked(false);
    }

    @Override
    public void OnSetAnnotation()
    {
        Utilities.LogDebug("GpsMainActivity.OnSetAnnotation");

        setAnnotationButtonMarked(true);
    }

    @Override
    public void OnClearAnnotation()
    {
        Utilities.LogDebug("GpsMainActivity.OnClearAnnotation");

        setAnnotationButtonMarked(false);
    }

    /**
     * Sets the message in the top status label.
     *
     * @param message The status message
     */
    private void setStatus(String message)
    {
        Utilities.LogDebug("GpsMainActivity.setStatus: " + message);

        TextView tvStatus = (TextView) findViewById(R.id.textStatus);
        tvStatus.setText(message);
        Utilities.LogInfo(message);
    }

    /**
     * Sets the number of satellites in the satellite row in the table.
     *
     * @param number The number of satellites
     */
    private void setSatelliteInfo(int number)
    {
        Session.setSatelliteCount(number);
        TextView txtSatellites = (TextView) findViewById(R.id.txtSatellites);
        txtSatellites.setText(String.valueOf(number));
    }

    /**
     * Given a location fix, processes it and displays it in the table on the
     * form.
     *
     * @param loc Location information
     */
    private void displayLocationInfo(Location loc)
    {
        Utilities.LogDebug("GpsMainActivity.displayLocationInfo");

        try
        {
            if (loc == null)
            {
                return;
            }

            TextView tvLatitude = (TextView) findViewById(R.id.txtLatitude);
            TextView tvLongitude = (TextView) findViewById(R.id.txtLongitude);
            TextView tvDateTime = (TextView) findViewById(R.id.txtDateTimeAndProvider);

            TextView tvAltitude = (TextView) findViewById(R.id.txtAltitude);

            TextView txtSpeed = (TextView) findViewById(R.id.txtSpeed);

            TextView txtSatellites = (TextView) findViewById(R.id.txtSatellites);
            TextView txtDirection = (TextView) findViewById(R.id.txtDirection);
            TextView txtAccuracy = (TextView) findViewById(R.id.txtAccuracy);
            TextView txtTravelled = (TextView) findViewById(R.id.txtDistanceTravelled);
            String providerName = loc.getProvider();

            if (providerName.equalsIgnoreCase("gps"))
            {
                providerName = getString(R.string.providername_gps);
            }
            else
            {
                providerName = getString(R.string.providername_celltower);
            }

            tvDateTime.setText(new Date(Session.getLatestTimeStamp()).toLocaleString()
                    + getString(R.string.providername_using, providerName));

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);


            tvLatitude.setText(String.valueOf(   loc.getLatitude()));
            tvLongitude.setText(String.valueOf(loc.getLongitude()));

            if (loc.hasAltitude())
            {

                double altitude = loc.getAltitude();

                if (AppSettings.shouldUseImperial())
                {
                    tvAltitude.setText( nf.format(Utilities.MetersToFeet(altitude))
                            + getString(R.string.feet));
                }
                else
                {
                    tvAltitude.setText(nf.format(altitude) + getString(R.string.meters));
                }

            }
            else
            {
                tvAltitude.setText(R.string.not_applicable);
            }

            if (loc.hasSpeed())
            {

                float speed = loc.getSpeed();
                String unit;
                if (AppSettings.shouldUseImperial())
                {
                    if (speed > 1.47)
                    {
                        speed = speed * 0.6818f;
                        unit = getString(R.string.miles_per_hour);

                    }
                    else
                    {
                        speed = Utilities.MetersToFeet(speed);
                        unit = getString(R.string.feet_per_second);
                    }
                }
                else
                {
                    if (speed > 0.277)
                    {
                        speed = speed * 3.6f;
                        unit = getString(R.string.kilometers_per_hour);
                    }
                    else
                    {
                        unit = getString(R.string.meters_per_second);
                    }
                }

                txtSpeed.setText(String.valueOf(nf.format(speed)) + unit);

            }
            else
            {
                txtSpeed.setText(R.string.not_applicable);
            }

            if (loc.hasBearing())
            {

                float bearingDegrees = loc.getBearing();
                String direction;

                direction = Utilities.GetBearingDescription(bearingDegrees, getApplicationContext());

                txtDirection.setText(direction + "(" + String.valueOf(Math.round(bearingDegrees)) + getString(R.string.degree_symbol) + ")");
            }
            else
            {
                txtDirection.setText(R.string.not_applicable);
            }

            if (!Session.isUsingGps())
            {
                txtSatellites.setText(R.string.not_applicable);
                Session.setSatelliteCount(0);
            }

            if (loc.hasAccuracy())
            {

                float accuracy = loc.getAccuracy();

                if (AppSettings.shouldUseImperial())
                {
                    txtAccuracy.setText(getString(R.string.accuracy_within, nf.format(Utilities.MetersToFeet(accuracy)), getString(R.string.feet)));

                }
                else
                {
                    txtAccuracy.setText(getString(R.string.accuracy_within, nf.format(accuracy), getString(R.string.meters)));
                }

            }
            else
            {
                txtAccuracy.setText(R.string.not_applicable);
            }


            String distanceUnit;
            double distanceValue = Session.getTotalTravelled();
            if (AppSettings.shouldUseImperial())
            {
                distanceUnit = getString(R.string.feet);
                distanceValue = Utilities.MetersToFeet(distanceValue);
                // When it passes more than 1 kilometer, convert to miles.
                if (distanceValue > 3281)
                {
                    distanceUnit = getString(R.string.miles);
                    distanceValue = distanceValue / 5280;
                }
            }
            else
            {
                distanceUnit = getString(R.string.meters);
                if (distanceValue > 1000)
                {
                    distanceUnit = getString(R.string.kilometers);
                    distanceValue = distanceValue / 1000;
                }
            }

            txtTravelled.setText(String.valueOf(Math.round(distanceValue)) + " " + distanceUnit + " (" + Session.getNumLegs() + " points)");

        }
        catch (Exception ex)
        {
            setStatus(getString(R.string.error_displaying, ex.getMessage()));
        }
    }

    private void enableDisableMenuItems() {
        if(mnuAnnotate == null)
        {
            return;
        }

        if(!AppSettings.shouldLogToCustomUrl())
        {
            // mnuAnnotate.setIcon(R.drawable.ic_menu_edit_disabled);
           mnuAnnotate.setIcon(R.drawable.ic_menu_edit_active);
        }
        else
        {
            mnuAnnotate.setIcon(R.drawable.ic_menu_edit);
        }
    }

    @Override
    public void OnLocationUpdate(Location loc)
    {
        Utilities.LogDebug("GpsMainActivity.OnLocationUpdate");

        displayLocationInfo(loc);
        showPreferencesSummary();
        setMainButtonChecked(true);

        if (Session.isSinglePointMode())
        {
            loggingService.stopLogging();
            setMainButtonEnabled(true);
            Session.setSinglePointMode(false);
        }

    }

    @Override
    public void OnSatelliteCount(int count)
    {
        setSatelliteInfo(count);

    }

    @Override
    public void OnStatusMessage(String message)
    {
        setStatus(message);
    }

    @Override
    public void OnFatalMessage(String message)
    {
        Utilities.MsgBox(getString(R.string.sorry), message, this);
    }

    @Override
    public Activity GetActivity()
    {
        return this;
    }

    @Override
    public void onComplete()
    {
        Utilities.HideProgress();
    }

    @Override
    public void onFailure()
    {
        Utilities.HideProgress();
    }
}
