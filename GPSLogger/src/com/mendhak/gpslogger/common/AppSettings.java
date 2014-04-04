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

import android.app.Application;

public class AppSettings extends Application
{
    // ---------------------------------------------------
    // User Preferences
    // ---------------------------------------------------
    private static boolean useImperial = false;
    private static boolean preferCellTower;
    private static boolean logToCustomUrl;
    private static String customLoggingUrl;
    private static boolean showInNotificationBar;
    private static int minimumSeconds;
    private static boolean keepFix;
    private static int retryInterval;
    private static Float autoPublishDelay = 0f;
    private static boolean autoPublishEnabled = false;

    private static boolean debugToFile;
    private static int minimumDistance;
    private static int minimumAccuracy;

    private static boolean autoPostEnabled = false;
    private static String postUrl;

    private static String gpsLoggerFolder;

    private static int timeoutSeconds;

    /**
     * @return the useImperial
     */
    public static boolean shouldUseImperial()
    {
        return useImperial;
    }

    /**
     * @param useImperial the useImperial to set
     */
    static void setUseImperial(boolean useImperial)
    {
        AppSettings.useImperial = useImperial;
    }

    /**
     * @return the preferCellTower
     */
    public static boolean shouldPreferCellTower()
    {
        return preferCellTower;
    }

    /**
     * @param preferCellTower the preferCellTower to set
     */
    static void setPreferCellTower(boolean preferCellTower)
    {
        AppSettings.preferCellTower = preferCellTower;
    }

    /**
     * @return the showInNotificationBar
     */
    public static boolean shouldShowInNotificationBar()
    {
        return showInNotificationBar;
    }

    /**
     * @param showInNotificationBar the showInNotificationBar to set
     */
    static void setShowInNotificationBar(boolean showInNotificationBar)
    {
        AppSettings.showInNotificationBar = showInNotificationBar;
    }


    /**
     * @return the minimumSeconds
     */
    public static int getMinimumSeconds()
    {
        return minimumSeconds;
    }

    /**
     * @param minimumSeconds the minimumSeconds to set
     */
    static void setMinimumSeconds(int minimumSeconds)
    {
        AppSettings.minimumSeconds = minimumSeconds;
    }


    /**
     * @return the keepFix
     */
    public static boolean shouldkeepFix()
    {
        return keepFix;
    }

    /**
     * @param keepFix the keepFix to set
     */
    static void setKeepFix(boolean keepFix)
    {
        AppSettings.keepFix = keepFix;
    }
    
          /**
     * @return the retryInterval
     */
    public static int getRetryInterval()
    {
        return retryInterval;
    }

    /**
     * @param retryInterval the retryInterval to set
     */
    static void setRetryInterval(int retryInterval)
    {
        AppSettings.retryInterval = retryInterval;
    }


    /**
     * @return the minimumDistance
     */
    public static int getMinimumDistanceInMeters()
    {
        return minimumDistance;
    }

    /**
     * @param minimumDistance the minimumDistance to set
     */
    static void setMinimumDistanceInMeters(int minimumDistance)
    {
        AppSettings.minimumDistance = minimumDistance;
    }

         /**
     * @return the minimumAccuracy
     */
    public static int getMinimumAccuracyInMeters()
    {
        return minimumAccuracy;
    }

    /**
     * @param minimumAccuracy the minimumAccuracy to set
     */
    static void setMinimumAccuracyInMeters(int minimumAccuracy)
    {
        AppSettings.minimumAccuracy = minimumAccuracy;
    }

    /**
     * @return the autoPublishDelay
     */
    public static Float getAutoPublishDelay()
    {
        if (autoPublishDelay >= 8f)
        {
            return 8f;
        }
        else
        {
            return autoPublishDelay;
        }


    }

    /**
     * @param autoPublishDelay the autoPublishDelay to set
     */
    static void setAutoPublishDelay(Float autoPublishDelay)
    {

        if (autoPublishDelay >= 8f)
        {
            AppSettings.autoPublishDelay = 8f;
        }
        else
        {
            AppSettings.autoPublishDelay = autoPublishDelay;
        }


    }


   public static boolean isAutoPostEnabled() {
      return autoPostEnabled;
   }

   public static void setAutoPostEnabled(boolean autoPostEnabled) {
      AppSettings.autoPostEnabled = autoPostEnabled;
   }

    public static boolean isDebugToFile()
    {
        return debugToFile;
    }

    public static void setDebugToFile(boolean debugToFile)
    {
        AppSettings.debugToFile = debugToFile;
    }

    public static boolean isAutoPublishEnabled()
    {
        return autoPublishEnabled;
    }

    public static void setAutoPublishEnabled(boolean autoPublishEnabled)
    {
        AppSettings.autoPublishEnabled = autoPublishEnabled;
    }

    public static boolean shouldLogToCustomUrl() {
        return logToCustomUrl;
    }

    public static void setLogToCustomUrl(boolean logToCustomUrl) {
        AppSettings.logToCustomUrl = logToCustomUrl;
    }


    public static String getCustomLoggingUrl() {
        return customLoggingUrl;
    }

    public static void setCustomLoggingUrl(String customLoggingUrl) {
        AppSettings.customLoggingUrl = customLoggingUrl;
    }

    public static String getGpsLoggerFolder() {
        return gpsLoggerFolder;
    }

    public static void setGpsLoggerFolder(String gpsLoggerFolder) {
        AppSettings.gpsLoggerFolder = gpsLoggerFolder;
    }

    public static String getPostUrl() {
       return postUrl;
    }

    public static void setPostUrl(String postUrl) {
       AppSettings.postUrl = postUrl;
    }

    public static int getTimeoutSeconds() {
       return timeoutSeconds;
    }

    public static void setTimeoutSeconds(int timeoutSeconds) {
       AppSettings.timeoutSeconds = timeoutSeconds;
    }
}
