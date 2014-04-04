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

package com.mendhak.gpslogger.common;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import org.nologs.gpslogger.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utilities
{

    private static final int LOGLEVEL = 5;
    private static ProgressDialog pd;

    private static void logToDebugFile(String message)
    {
        if (AppSettings.isDebugToFile())
        {
            DebugLogger.log(message);
        }
    }

    public static void logInfo(String message)
    {
        if (LOGLEVEL >= 3)
        {
            Log.i("GPSLogger", message);
        }

        logToDebugFile(message);

    }

    public static void logError(String methodName, Exception ex)
    {
        try
        {
            logError(methodName + ":" + ex.getMessage());
        }
        catch (Exception e)
        {
            /**/
        }
    }

    private static void logError(String message)
    {
        Log.e("GPSLogger", message);
        logToDebugFile(message);
    }

    public static void logDebug(String message)
    {
        if (LOGLEVEL >= 4)
        {
            Log.d("GPSLogger", message);
        }
        logToDebugFile(message);
    }

    public static void logWarning(String message)
    {
        if (LOGLEVEL >= 2)
        {
            Log.w("GPSLogger", message);
        }
        logToDebugFile(message);
    }

    public static void logVerbose(String message)
    {
        if (LOGLEVEL >= 5)
        {
            Log.v("GPSLogger", message);
        }
        logToDebugFile(message);
    }

    /**
     * Gets user preferences, populates the AppSettings class.
     */
    public static void populateAppSettings(Context context)
    {

        Utilities.logInfo("Getting preferences");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        AppSettings.setUseImperial(prefs.getBoolean("useImperial", false));

        AppSettings.setLogToCustomUrl(prefs.getBoolean("log_customurl_enabled", false));
        AppSettings.setCustomLoggingUrl(prefs.getString("log_customurl_url", ""));

        AppSettings.setShowInNotificationBar(prefs.getBoolean("show_notification", true));

        AppSettings.setPreferCellTower(prefs.getBoolean("prefer_celltower", false));

        String minimumDistanceString = prefs.getString("distance_before_logging", "0");

        if (minimumDistanceString != null && minimumDistanceString.length() > 0)
        {
            AppSettings.setMinimumDistanceInMeters(Integer.valueOf(minimumDistanceString));
        }
        else
        {
            AppSettings.setMinimumDistanceInMeters(0);
        }

        String minimumAccuracyString = prefs.getString("accuracy_before_logging", "0");

        if (minimumAccuracyString != null && minimumAccuracyString.length() > 0)
        {
            AppSettings.setMinimumAccuracyInMeters(Integer.valueOf(minimumAccuracyString));
        }
        else
        {
            AppSettings.setMinimumAccuracyInMeters(0);
        }

        if (AppSettings.shouldUseImperial())
        {
            AppSettings.setMinimumDistanceInMeters(Utilities.feetToMeters(AppSettings.getMinimumDistanceInMeters()));

            AppSettings.setMinimumAccuracyInMeters(Utilities.feetToMeters(AppSettings.getMinimumAccuracyInMeters()));
        }


        String minimumSecondsString = prefs.getString("time_before_logging", "60");

        if (minimumSecondsString != null && minimumSecondsString.length() > 0)
        {
            AppSettings.setMinimumSeconds(Integer.valueOf(minimumSecondsString));
        }
        else
        {
            AppSettings.setMinimumSeconds(60);
        }

        AppSettings.setKeepFix(prefs.getBoolean("keep_fix", false));

        String retryIntervalString = prefs.getString("retry_time", "60");

        if (retryIntervalString != null && retryIntervalString.length() > 0)
        {
            AppSettings.setRetryInterval(Integer.valueOf(retryIntervalString));
        }
        else
        {
             AppSettings.setRetryInterval(60);
        }

        String timeout = prefs.getString("timeout_time", "0");
        AppSettings.setTimeoutSeconds(!TextUtils.isEmpty(timeout) ? Integer.valueOf(timeout) : 0);

        AppSettings.setAutoPublishEnabled(prefs.getBoolean("autopublish_enabled", false));

        AppSettings.setAutoPostEnabled(prefs.getBoolean("autopost_enabled", false));
        AppSettings.setPostUrl(prefs.getString("post_url", "http://localhost:8080/post.do"));

        if (Float.valueOf(prefs.getString("autopublish_frequency", "0")) >= 8f)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("autopublish_frequency", "8");
            editor.commit();
        }

        AppSettings.setAutoPublishDelay(Float.valueOf(prefs.getString("autopublish_frequency", "0")));

        AppSettings.setDebugToFile(prefs.getBoolean("debugtofile", false));

        AppSettings.setGpsLoggerFolder(prefs.getString("gpslogger_folder", Environment.getExternalStorageDirectory() + "/GPSLogger"));
    }

    public static void showProgress(Context ctx, String title, String message)
    {
        if (ctx != null)
        {
            pd = new ProgressDialog(ctx, ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(100);
            pd.setIndeterminate(true);

            pd = ProgressDialog.show(ctx, title, message, true, true);
        }
    }

    public static void hideProgress()
    {
        if (pd != null)
        {
            pd.dismiss();
        }
    }

    /**
     * Displays a message box to the user with an OK button.
     *
     * @param title
     * @param message
     * @param className The calling class, such as GpsMainActivity.this or
     *                  mainActivity.
     */
    public static void msgBox(String title, String message, Context className)
    {
        msgBox(title, message, className, null);
    }

    /**
     * Displays a message box to the user with an OK button.
     *
     * @param title
     * @param message
     * @param className   The calling class, such as GpsMainActivity.this or
     *                    mainActivity.
     * @param msgCallback An object which implements IHasACallBack so that the 
     *                    click event can call the callback method.
     */
    private static void msgBox(String title, String message, Context className,
                               final IMessageBoxCallback msgCallback)
    {
    	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(className);
    	alertBuilder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(className.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                    	
                            public void onClick(final DialogInterface dialog, 
                            		            final int which) {
                       
                            	if (msgCallback != null)
                            	{
                            		msgCallback.messageBoxResult(which);
                            	}
                            }
                    	}
                    );
    	
        AlertDialog alertDialog = alertBuilder.create();
        
        alertDialog.show();
    }

    /**
     * Converts seconds into friendly, understandable description of time.
     *
     * @param numberOfSeconds
     * @return
     */
    public static String getDescriptiveTimeString(int numberOfSeconds,
                                                  Context context)
    {

        String descriptive;
        int hours;
        int minutes;
        int seconds;

        int remainingSeconds;

        // Special cases
        if (numberOfSeconds == 1)
        {
            return context.getString(R.string.time_onesecond);
        }

        if (numberOfSeconds == 30)
        {
            return context.getString(R.string.time_halfminute);
        }

        if (numberOfSeconds == 60)
        {
            return context.getString(R.string.time_oneminute);
        }

        if (numberOfSeconds == 900)
        {
            return context.getString(R.string.time_quarterhour);
        }

        if (numberOfSeconds == 1800)
        {
            return context.getString(R.string.time_halfhour);
        }

        if (numberOfSeconds == 3600)
        {
            return context.getString(R.string.time_onehour);
        }

        if (numberOfSeconds == 4800)
        {
            return context.getString(R.string.time_oneandhalfhours);
        }

        if (numberOfSeconds == 9000)
        {
            return context.getString(R.string.time_twoandhalfhours);
        }

        // For all other cases, calculate

        hours = numberOfSeconds / 3600;
        remainingSeconds = numberOfSeconds % 3600;
        minutes = remainingSeconds / 60;
        seconds = remainingSeconds % 60;

        // Every 5 hours and 2 minutes
        // XYZ-5*2*20*

        descriptive = context.getString(R.string.time_hms_format,
                String.valueOf(hours), String.valueOf(minutes),
                String.valueOf(seconds));

        return descriptive;

    }

    /**
     * Converts given bearing degrees into a rough cardinal direction that's
     * more understandable to humans.
     *
     * @param bearingDegrees
     * @return
     */
    public static String getBearingDescription(float bearingDegrees,
                                               Context context)
    {

        String direction;
        String cardinal;

        if (bearingDegrees > 348.75 || bearingDegrees <= 11.25)
        {
            cardinal = context.getString(R.string.direction_north);
        }
        else if (bearingDegrees > 11.25 && bearingDegrees <= 33.75)
        {
            cardinal = context.getString(R.string.direction_northnortheast);
        }
        else if (bearingDegrees > 33.75 && bearingDegrees <= 56.25)
        {
            cardinal = context.getString(R.string.direction_northeast);
        }
        else if (bearingDegrees > 56.25 && bearingDegrees <= 78.75)
        {
            cardinal = context.getString(R.string.direction_eastnortheast);
        }
        else if (bearingDegrees > 78.75 && bearingDegrees <= 101.25)
        {
            cardinal = context.getString(R.string.direction_east);
        }
        else if (bearingDegrees > 101.25 && bearingDegrees <= 123.75)
        {
            cardinal = context.getString(R.string.direction_eastsoutheast);
        }
        else if (bearingDegrees > 123.75 && bearingDegrees <= 146.26)
        {
            cardinal = context.getString(R.string.direction_southeast);
        }
        else if (bearingDegrees > 146.25 && bearingDegrees <= 168.75)
        {
            cardinal = context.getString(R.string.direction_southsoutheast);
        }
        else if (bearingDegrees > 168.75 && bearingDegrees <= 191.25)
        {
            cardinal = context.getString(R.string.direction_south);
        }
        else if (bearingDegrees > 191.25 && bearingDegrees <= 213.75)
        {
            cardinal = context.getString(R.string.direction_southsouthwest);
        }
        else if (bearingDegrees > 213.75 && bearingDegrees <= 236.25)
        {
            cardinal = context.getString(R.string.direction_southwest);
        }
        else if (bearingDegrees > 236.25 && bearingDegrees <= 258.75)
        {
            cardinal = context.getString(R.string.direction_westsouthwest);
        }
        else if (bearingDegrees > 258.75 && bearingDegrees <= 281.25)
        {
            cardinal = context.getString(R.string.direction_west);
        }
        else if (bearingDegrees > 281.25 && bearingDegrees <= 303.75)
        {
            cardinal = context.getString(R.string.direction_westnorthwest);
        }
        else if (bearingDegrees > 303.75 && bearingDegrees <= 326.25)
        {
            cardinal = context.getString(R.string.direction_northwest);
        }
        else if (bearingDegrees > 326.25 && bearingDegrees <= 348.75)
        {
            cardinal = context.getString(R.string.direction_northnorthwest);
        }
        else
        {
            direction = context.getString(R.string.unknown_direction);
            return direction;
        }

        direction = context.getString(R.string.direction_roughly, cardinal);
        return direction;

    }

    /**
     * Makes string safe for writing to XML file. Removes lt and gt. Best used
     * when writing to file.
     *
     * @param desc
     * @return
     */
    public static String cleanDescription(String desc)
    {
        desc = desc.replace("<", "");
        desc = desc.replace(">", "");
        desc = desc.replace("&", "&amp;");
        desc = desc.replace("\"", "&quot;");

        return desc;
    }


    /**
     * Given a Date object, returns an ISO 8601 date time string in UTC.
     * Example: 2010-03-23T05:17:22Z but not 2010-03-23T05:17:22+04:00
     *
     * @param dateToFormat The Date object to format.
     * @return The ISO 8601 formatted string.
     */
    public static String getIsoDateTime(Date dateToFormat)
    {
    	/**
        * This function is used in gpslogger.loggers.* and for most of them the
        * default locale should be fine, but in the case of HttpUrlLogger we 
        * want machine-readable output, thus  Locale.US.
        * 
        * Be wary of the default locale
        * http://developer.android.com/reference/java/util/Locale.html#default_locale
        */
        
        // GPX specs say that time given should be in UTC, no local time.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", 
        		 									Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(dateToFormat);
    }

    public static String getReadableDateTime(Date dateToFormat)
    {
    	/**
    	 * Similar to getIsoDateTime(), this function is used in
    	 * AutoEmailHelper, and we want machine-readable output.
    	 */
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", 
        		                                    Locale.US);
        return sdf.format(dateToFormat);
    }

    /**
     * Converts given meters to feet.
     *
     * @param m
     * @return
     */
    public static int metersToFeet(int m)
    {
        return (int) Math.round(m * 3.2808399);
    }


    /**
     * Converts given feet to meters
     *
     * @param f
     * @return
     */
    public static int feetToMeters(int f)
    {
        return (int) Math.round(f / 3.2808399);
    }


    /**
     * Converts given meters to feet and rounds up.
     *
     * @param m
     * @return
     */
    public static int metersToFeet(double m)
    {
        return metersToFeet((int) m);
    }

    /**
     * Uses the Haversine formula to calculate the distnace between to lat-long coordinates
     *
     * @param latitude1  The first point's latitude
     * @param longitude1 The first point's longitude
     * @param latitude2  The second point's latitude
     * @param longitude2 The second point's longitude
     * @return The distance between the two points in meters
     */
    public static double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2)
    {
        /*
            Haversine formula:
            A = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
            C = 2.atan2(√a, √(1−a))
            D = R.c
            R = radius of earth, 6371 km.
            All angles are in radians
            */

        double deltaLatitude = Math.toRadians(Math.abs(latitude1 - latitude2));
        double deltaLongitude = Math.toRadians(Math.abs(longitude1 - longitude2));
        double latitude1Rad = Math.toRadians(latitude1);
        double latitude2Rad = Math.toRadians(latitude2);

        double a = Math.pow(Math.sin(deltaLatitude / 2), 2) +
                (Math.cos(latitude1Rad) * Math.cos(latitude2Rad) * Math.pow(Math.sin(deltaLongitude / 2), 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c * 1000; //Distance in meters

    }

}
