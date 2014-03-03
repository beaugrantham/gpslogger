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
    private static boolean newFileOnceADay;
    private static boolean preferCellTower;
    private static boolean logToCustomUrl;
    private static String customLoggingUrl;
    private static boolean showInNotificationBar;
    private static int minimumSeconds;
    private static boolean keepFix;
    private static int retryInterval;
    private static Float autoSendDelay = 0f;
    private static boolean autoSendEnabled = false;

    private static boolean autoEmailEnabled = false;
    private static String smtpServer;
    private static String smtpPort;
    private static String smtpUsername;
    private static String smtpPassword;
    private static String smtpFrom;
    private static String autoEmailTargets;
    private static boolean smtpSsl;
    private static boolean debugToFile;
    private static int minimumDistance;
    private static int minimumAccuracy;

    private static boolean autoPostEnabled = false;
    private static String postUrl;

    private static String gpsLoggerFolder;

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
     * @return the newFileOnceADay
     */
    public static boolean shouldCreateNewFileOnceADay()
    {
        return newFileOnceADay;
    }

    /**
     * @param newFileOnceADay the newFileOnceADay to set
     */
    static void setNewFileOnceADay(boolean newFileOnceADay)
    {
        AppSettings.newFileOnceADay = newFileOnceADay;
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
     * @return the autoSendDelay
     */
    public static Float getAutoSendDelay()
    {
        if (autoSendDelay >= 8f)
        {
            return 8f;
        }
        else
        {
            return autoSendDelay;
        }


    }

    /**
     * @param autoSendDelay the autoSendDelay to set
     */
    static void setAutoSendDelay(Float autoSendDelay)
    {

        if (autoSendDelay >= 8f)
        {
            AppSettings.autoSendDelay = 8f;
        }
        else
        {
            AppSettings.autoSendDelay = autoSendDelay;
        }


    }


   public static boolean isAutoPostEnabled() {
      return autoPostEnabled;
   }

   public static void setAutoPostEnabled(boolean autoPostEnabled) {
      AppSettings.autoPostEnabled = autoPostEnabled;
   }

    /**
     * @return the autoEmailEnabled
     */
    public static boolean isAutoEmailEnabled()
    {
        return autoEmailEnabled;
    }

    /**
     * @param autoEmailEnabled the autoEmailEnabled to set
     */
    static void setAutoEmailEnabled(boolean autoEmailEnabled)
    {
        AppSettings.autoEmailEnabled = autoEmailEnabled;
    }


    static void setSmtpServer(String smtpServer)
    {
        AppSettings.smtpServer = smtpServer;
    }

    public static String getSmtpServer()
    {
        return smtpServer;
    }

    static void setSmtpPort(String smtpPort)
    {
        AppSettings.smtpPort = smtpPort;
    }

    public static String getSmtpPort()
    {
        return smtpPort;
    }

    static void setSmtpUsername(String smtpUsername)
    {
        AppSettings.smtpUsername = smtpUsername;
    }

    public static String getSmtpUsername()
    {
        return smtpUsername;
    }


    static void setSmtpPassword(String smtpPassword)
    {
        AppSettings.smtpPassword = smtpPassword;
    }

    public static String getSmtpPassword()
    {
        return smtpPassword;
    }

    static void setSmtpSsl(boolean smtpSsl)
    {
        AppSettings.smtpSsl = smtpSsl;
    }

    public static boolean isSmtpSsl()
    {
        return smtpSsl;
    }

    static void setAutoEmailTargets(String autoEmailTargets)
    {
        AppSettings.autoEmailTargets = autoEmailTargets;
    }

    public static String getAutoEmailTargets()
    {
        return autoEmailTargets;
    }

    public static boolean isDebugToFile()
    {
        return debugToFile;
    }

    public static void setDebugToFile(boolean debugToFile)
    {
        AppSettings.debugToFile = debugToFile;
    }

    private static String getSmtpFrom()
    {
        return smtpFrom;
    }

    public static void setSmtpFrom(String smtpFrom)
    {
        AppSettings.smtpFrom = smtpFrom;
    }

    /**
     * Returns the from value to use when sending an email
     *
     * @return
     */
    public static String getSenderAddress()
    {
        if (getSmtpFrom() != null && getSmtpFrom().length() > 0)
        {
            return getSmtpFrom();
        }

        return getSmtpUsername();
    }

    public static boolean isAutoSendEnabled()
    {
        return autoSendEnabled;
    }

    public static void setAutoSendEnabled(boolean autoSendEnabled)
    {
        AppSettings.autoSendEnabled = autoSendEnabled;
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
}
