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

package com.mendhak.gpslogger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mendhak.gpslogger.common.FileDialog.FileDialog;
import com.mendhak.gpslogger.common.Utilities;

public class GpsSettingsActivity extends SherlockPreferenceActivity
{

    private final static int SELECT_FOLDER_DIALOG=420;
    private final Handler handler = new Handler();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // enable the home button so you can go back to the main screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.settings);

        if (getIntent().getBooleanExtra("autosend_preferencescreen", false))
        {
            PreferenceScreen screen = (PreferenceScreen) findPreference("gpslogger_preferences");
            int pos = findPreference("autosend_preferencescreen").getOrder();
            screen.onItemClick(null, null, pos, 0);
        }


        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean useImperial = prefs.getBoolean("useImperial", false);

        EditTextPreference distanceBeforeLogging = (EditTextPreference) findPreference("distance_before_logging");

        if (useImperial)
        {
            distanceBeforeLogging.setDialogTitle(R.string.settings_distance_in_feet);
            distanceBeforeLogging.getEditText().setHint(R.string.settings_enter_feet);
        }
        else
        {
            distanceBeforeLogging.setDialogTitle(R.string.settings_distance_in_meters);
            distanceBeforeLogging.getEditText().setHint(R.string.settings_enter_meters);
        }
        
        EditTextPreference accuracyBeforeLogging = (EditTextPreference) findPreference("accuracy_before_logging");

        if (useImperial)
        {
            accuracyBeforeLogging.setDialogTitle(R.string.settings_accuracy_in_feet);
            accuracyBeforeLogging.getEditText().setHint(R.string.settings_enter_feet);
        }
        else
        {
            accuracyBeforeLogging.setDialogTitle(R.string.settings_accuracy_in_meters);
            accuracyBeforeLogging.getEditText().setHint(R.string.settings_enter_meters);
        }


        Preference gpsLoggerFolder = findPreference("gpslogger_folder");
        gpsLoggerFolder.setOnPreferenceClickListener(new GpsLoggerFolderPreferenceClickListener());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        gpsLoggerFolder.setSummary(prefs.getString("gpslogger_folder", Environment.getExternalStorageDirectory() + "/GPSLogger"));

        CheckBoxPreference imperialCheckBox = (CheckBoxPreference) findPreference("useImperial");
        imperialCheckBox.setOnPreferenceChangeListener(new ImperialPreferenceChangeListener(prefs, distanceBeforeLogging, accuracyBeforeLogging));


        Preference enableDisablePref = findPreference("enableDisableGps");
        enableDisablePref.setOnPreferenceClickListener(new AndroidLocationPreferenceClickListener());
    }


    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {

        if(requestCode == SELECT_FOLDER_DIALOG)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                Utilities.logDebug("Folder path selected" + filePath);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("gpslogger_folder", filePath);
                editor.commit();

                Preference gpsLoggerFolder = findPreference("gpslogger_folder");
                gpsLoggerFolder.setSummary(filePath);

            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {
                Utilities.logDebug("No file selected");
            }
        }
    }


    /**
     * Called when one of the menu items is selected.
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int itemId = item.getItemId();
        Utilities.logInfo("Option item selected - " + String.valueOf(item.getTitle()));

        switch (itemId)
        {
            case android.R.id.home:
                Intent intent = new Intent(this, GpsMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private final Runnable updateResults = new Runnable()
    {
        public void run()
        {
            finish();

            startActivity(getIntent());
        }

    };


    public class GpsLoggerFolderPreferenceClickListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {

            Intent intent = new Intent(getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, prefs.getString("gpslogger_folder",
                    Environment.getExternalStorageDirectory() + "/GPSLogger"));

            intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
            startActivityForResult(intent, SELECT_FOLDER_DIALOG);

            return false;
        }
    }

    /**
     * Opens the Android Location preferences screen
     */
    private class AndroidLocationPreferenceClickListener implements OnPreferenceClickListener
    {
        public boolean onPreferenceClick(Preference preference)
        {
            startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            return true;
        }
    }

    private class ImperialPreferenceChangeListener implements Preference.OnPreferenceChangeListener
    {
        EditTextPreference distanceBeforeLogging;
        EditTextPreference accuracyBeforeLogging;
        SharedPreferences prefs;

        public ImperialPreferenceChangeListener(SharedPreferences prefs, EditTextPreference distanceBeforeLogging, EditTextPreference accuracyBeforeLogging)
        {
            this.prefs = prefs;
            this.distanceBeforeLogging = accuracyBeforeLogging;
            this.accuracyBeforeLogging = accuracyBeforeLogging;
        }

        public boolean onPreferenceChange(Preference preference, final Object newValue)
        {

            Utilities.showProgress(GpsSettingsActivity.this, getString(R.string.settings_converting_title),
                    getString(R.string.settings_converting_description));

            new Thread()
            {

                public void run()
                {

                    try
                    {
                        sleep(3000); // Give user time to read the message
                    }
                    catch (InterruptedException e)
                    {

                        Log.e("Settings", e.getMessage());

                    }

                    boolean useImp = Boolean.parseBoolean(newValue.toString());

                    String minimumDistanceString = prefs.getString("distance_before_logging", "0");
                    String minimumAccuracyString = prefs.getString("accuracy_before_logging", "0");
                    
                    int minimumDistance;

                    if (minimumDistanceString != null && minimumDistanceString.length() > 0)
                    {
                        minimumDistance = Integer.valueOf(minimumDistanceString);
                    }
                    else
                    {
                        minimumDistance = 0;
                    }
                    
                    int minimumAccuracy;

                    if (minimumAccuracyString != null && minimumAccuracyString.length() > 0)
                    {
                        minimumAccuracy = Integer.valueOf(minimumAccuracyString);
                    }
                    else
                    {
                        minimumAccuracy = 0;
                    }

                    SharedPreferences.Editor editor = prefs.edit();

                    if (useImp)
                    {
                        distanceBeforeLogging.setDialogTitle(R.string.settings_distance_in_feet);
                        distanceBeforeLogging.getEditText().setHint(R.string.settings_enter_feet);

                        minimumDistance = Utilities.metersToFeet(minimumDistance);
                        
                        accuracyBeforeLogging.setDialogTitle(R.string.settings_accuracy_in_feet);
                        accuracyBeforeLogging.getEditText().setHint(R.string.settings_enter_feet);

                        minimumAccuracy = Utilities.metersToFeet(minimumAccuracy);
                    }
                    else
                    {
                        minimumDistance = Utilities.feetToMeters(minimumDistance);
                        distanceBeforeLogging.setDialogTitle(R.string.settings_distance_in_meters);
                        distanceBeforeLogging.getEditText().setHint(R.string.settings_enter_meters);
                                                    
                        minimumAccuracy = Utilities.feetToMeters(minimumAccuracy);
                        accuracyBeforeLogging.setDialogTitle(R.string.settings_accuracy_in_meters);
                        accuracyBeforeLogging.getEditText().setHint(R.string.settings_enter_meters);
                        

                    }

                    if (minimumDistance >= 9999)
                    {
                        minimumDistance = 9999;
                    }
                    
                    if (minimumAccuracy >= 9999)
                    {
                        minimumAccuracy = 9999;
                    }

                    editor.putString("distance_before_logging", String.valueOf(minimumDistance));
                    
                    editor.putString("accuracy_before_logging", String.valueOf(minimumAccuracy));
                    editor.commit();

                    handler.post(updateResults);
                    Utilities.hideProgress();
                }
            }.start();

            return true;
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        Utilities.logDebug("GpsSettingsActivity.onWindowFocusChanged");
        if (hasFocus)
        {



        }
    }


}
