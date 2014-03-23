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

//TODO: Simplify email logic (too many methods)
//TODO: Allow messages in IActionListener callback methods
//TODO: Handle case where a fix is not found and GPS gives up - restart alarm somehow?

package com.mendhak.gpslogger;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.DateFormat;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.Session;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.loggers.LocationLoggerFactory;
import com.mendhak.gpslogger.loggers.ILocationLogger;
import com.mendhak.gpslogger.senders.AlarmReceiver;
import com.mendhak.gpslogger.senders.PublisherFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class GpsLoggingService extends Service implements IActionListener {

   private static NotificationManager gpsNotifyManager;
   private static int NOTIFICATION_ID = 8675309;
   private static IGpsLoggerServiceClient mainServiceClient;
   private final IBinder mBinder = new GpsLoggingBinder();

   LocationManager gpsLocationManager;
   AlarmManager nextPointAlarmManager;

   private Notification notification = null;
   private boolean forceLogOnce = false;

   // ---------------------------------------------------
   // Helpers and managers
   // ---------------------------------------------------
   private GeneralLocationListener gpsLocationListener;
   private GeneralLocationListener towerLocationListener;
   private LocationManager towerLocationManager;
   private Intent alarmIntent;

   // ---------------------------------------------------

   /**
    * Sets the activity form for this service. The activity form needs to
    * implement IGpsLoggerServiceClient.
    *
    * @param mainForm The calling client
    */
   protected static void setServiceClient(IGpsLoggerServiceClient mainForm) {
      mainServiceClient = mainForm;
   }

   @Override
   public IBinder onBind(Intent arg0) {
      Utilities.logDebug("GpsLoggingService.onBind");

      return mBinder;
   }

   @Override
   public void onCreate() {
      Utilities.logDebug("GpsLoggingService.onCreate");

      nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

      Utilities.logInfo("GPSLoggerService created");
   }

   @Override
   public void onStart(Intent intent, int startId) {
      Utilities.logDebug("GpsLoggingService.onStart");
      handleIntent(intent);
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Utilities.logDebug("GpsLoggingService.onStartCommand");

      handleIntent(intent);
      return START_REDELIVER_INTENT;
   }

   @Override
   public void onDestroy() {
      Utilities.logWarning("GpsLoggingService is being destroyed by Android OS.");

      mainServiceClient = null;
      super.onDestroy();
   }

   @Override
   public void onLowMemory() {
      Utilities.logWarning("Android is low on memory.");

      super.onLowMemory();
   }

   private void handleIntent(Intent intent) {
      Utilities.logDebug("GpsLoggingService.handleIntent");

      getPreferences();

      Utilities.logDebug("Null intent? " + String.valueOf(intent == null));

      if (intent != null) {
         Bundle bundle = intent.getExtras();

         if (bundle != null) {
            boolean stopRightNow = bundle.getBoolean("immediatestop");
            boolean startRightNow = bundle.getBoolean("immediate");
            boolean sendEmailNow = bundle.getBoolean("emailAlarm");
            boolean getNextPoint = bundle.getBoolean("getnextpoint");

            Utilities.logDebug("startRightNow - " + String.valueOf(startRightNow));

            Utilities.logDebug("emailAlarm - " + String.valueOf(sendEmailNow));

            if (startRightNow) {
               Utilities.logInfo("Auto starting logging");

               startLogging();
            }

            if (stopRightNow) {
               Utilities.logInfo("Auto stop logging");
               stopLogging();
            }

            if (sendEmailNow) {

               Utilities.logDebug("setReadyToBeAutoSent = true");

               Session.setReadyToBeAutoSent(true);
               autoPublishFile();
            }

            if (getNextPoint && Session.isStarted()) {
               Utilities.logDebug("handleIntent - getNextPoint");
               startGpsManager();
            }

         }
      } else {
         // A null intent is passed in if the service has been killed and
         // restarted.
         Utilities.logDebug("Service restarted with null intent. Start logging.");
         startLogging();

      }
   }

   @Override
   public void onComplete() {
      Utilities.hideProgress();
   }

   @Override
   public void onFailure() {
      Utilities.hideProgress();
   }

   /**
    * Sets up the auto send timers based on user preferences.
    */
   public void setupAutoSendTimers() {
      Utilities.logDebug("GpsLoggingService.setupAutoSendTimers");
      Utilities.logDebug("isAutoSendEnabled - " + String.valueOf(AppSettings.isAutoSendEnabled()));
      Utilities.logDebug("Session.getAutoSendDelay - " + String.valueOf(Session.getAutoSendDelay()));

      if (AppSettings.isAutoSendEnabled() && Session.getAutoSendDelay() > 0) {
         Utilities.logDebug("Setting up autosend alarm");
         long triggerTime = System.currentTimeMillis() + (long) (Session.getAutoSendDelay() * 60 * 60 * 1000);

         alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
         cancelAlarm();
         Utilities.logDebug("New alarm intent");
         PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
         am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender);
         Utilities.logDebug("Alarm has been set");

      } else {
         Utilities.logDebug("Checking if alarmIntent is null");
         if (alarmIntent != null) {
            Utilities.logDebug("alarmIntent was null, canceling alarm");
            cancelAlarm();
         }
      }
   }

   private void setForceLogOnce(boolean flag) {
      forceLogOnce = flag;
   }

   private boolean forceLogOnce() {
      return forceLogOnce;
   }

   public void logOnce() {
      setForceLogOnce(true);
      if (Session.isStarted()) {
         startGpsManager();
      }
      else {
         Session.setSinglePointMode(true);
         startLogging();
      }
   }

   private void cancelAlarm() {
      Utilities.logDebug("GpsLoggingService.cancelAlarm");

      if (alarmIntent != null) {
         Utilities.logDebug("GpsLoggingService.cancelAlarm");
         AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
         PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         Utilities.logDebug("Pending alarm intent was null? " + String.valueOf(sender == null));
         am.cancel(sender);
      }
   }

   /**
    * Method to be called if user has chosen to auto publish when logging stops
    */
   private void autoPublishOnStop() {
      Utilities.logDebug("GpsLoggingService.autoPublishOnStop");
      Utilities.logVerbose("isAutoSendEnabled - " + AppSettings.isAutoSendEnabled());

      // autoSendDelay 0 means send it when you stop logging.
      if (AppSettings.isAutoSendEnabled() && Session.getAutoSendDelay() == 0) {
         Session.setReadyToBeAutoSent(true);
         autoPublishFile();
      }
   }

   /**
    * Calls the publisher(s)
    */
   private void autoPublishFile() {
      Utilities.logDebug("GpsLoggingService.autoPublishFile");
      Utilities.logVerbose("isReadyToBeAutoSent - " + Session.isReadyToBeAutoSent());

      // Check that auto emailing is enabled, there's a valid location
      if (Session.isReadyToBeAutoSent() && Session.hasValidLocation()) {

         //Don't show a progress bar when auto-emailing
         Utilities.logInfo("Auto Sending Location History");

         PublisherFactory.publish(this, getApplicationContext());
         Session.setReadyToBeAutoSent(true);
         setupAutoSendTimers();

      }
   }

   protected void forcePublish() {
      Utilities.logDebug("GpsLoggingService.forcePublish");

      if (AppSettings.isAutoSendEnabled()) {
         if (isMainFormVisible()) {
            Utilities.showProgress(mainServiceClient.getActivity(), getString(R.string.autosend_sending), getString(R.string.please_wait));
         }

         Utilities.logInfo("Force publishing");
         PublisherFactory.publish(this, getApplicationContext());
      }
   }

   /**
    * Gets preferences chosen by the user and populates the AppSettings object.
    * Also sets up email timers if required.
    */
   private void getPreferences() {
      Utilities.logDebug("GpsLoggingService.getPreferences");
      Utilities.populateAppSettings(getApplicationContext());

      Utilities.logDebug("Session.getAutoSendDelay: " + Session.getAutoSendDelay());
      Utilities.logDebug("AppSettings.getAutoSendDelay: " + AppSettings.getAutoSendDelay());

      if (Session.getAutoSendDelay() != AppSettings.getAutoSendDelay()) {
         Utilities.logDebug("Old autoSendDelay - " + String.valueOf(Session.getAutoSendDelay()) + "; New -" + String.valueOf(AppSettings.getAutoSendDelay()));
         Session.setAutoSendDelay(AppSettings.getAutoSendDelay());
         setupAutoSendTimers();
      }

   }

   /**
    * Resets the form, resets file name if required, reobtains preferences
    */
   protected void startLogging() {
      Utilities.logDebug("GpsLoggingService.startLogging");

      if (Session.isStarted()) {
         return;
      }

      Utilities.logInfo("Starting logging procedures");
      try {
         startForeground(NOTIFICATION_ID, null);
      } catch (Exception ex) {
         System.out.print(ex.getMessage());
      }


      Session.setStarted(true);

      this.getPreferences();
      this.Notify();
      this.clearForm();
      this.startGpsManager();
   }

   /**
    * Asks the main service client to clear its form.
    */
   private void clearForm() {
      if (isMainFormVisible()) {
         mainServiceClient.clearForm();
      }
   }

   /**
    * Stops logging, removes notification, stops GPS manager, stops email timer
    */
   public void stopLogging() {
      Utilities.logDebug("GpsLoggingService.stopLogging");

      Utilities.logInfo("Stopping logging");
      Session.setStarted(false);
      // Email log file before setting location info to null
      this.autoPublishOnStop();
      this.cancelAlarm();
      Session.setCurrentLocationInfo(null);
      this.stopForeground(true);

      this.removeNotification();
      this.stopAlarm();
      this.stopGpsManager();
      this.stopMainActivity();
   }

   /**
    * Manages the notification in the status bar
    */
   private void Notify() {

      Utilities.logDebug("GpsLoggingService.Notify");
      if (AppSettings.shouldShowInNotificationBar()) {
         gpsNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

         showNotification();
      } else {
         removeNotification();
      }
   }

   /**
    * Hides the notification icon in the status bar if it's visible.
    */
   private void removeNotification() {
      Utilities.logDebug("GpsLoggingService.removeNotification");
      try {
         if (Session.isNotificationVisible()) {
            gpsNotifyManager.cancelAll();
         }
      } catch (Exception ex) {
         Utilities.logError("removeNotification", ex);
      } finally {
         notification = null;
         Session.setNotificationVisible(false);
      }
   }

   /**
    * Shows a notification icon in the status bar for GPS Logger
    */
   private void showNotification() {
      Utilities.logDebug("GpsLoggingService.showNotification");
      // What happens when the notification item is clicked
      Intent contentIntent = new Intent(this, GpsMainActivity.class);

      PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, contentIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

      NumberFormat nf = new DecimalFormat("###.#####");

      String contentText = getString(R.string.gpslogger_still_running);
      if (Session.hasValidLocation()) {
         contentText = DateFormat.format("HH:mm:ss", Session.getCurrentLocationInfo().getTime()) + ": ";
         contentText += "Lat: " + nf.format(Session.getCurrentLatitude()) + ", Lon: " + nf.format(Session.getCurrentLongitude());
      }

      if (notification == null) {
         notification = new Notification(R.drawable.gpsloggericon2, null, System.currentTimeMillis());
         notification.flags |= Notification.FLAG_ONGOING_EVENT;
      }

      notification.setLatestEventInfo(getApplicationContext(), getString(R.string.gpslogger_still_running), contentText, pending);

      gpsNotifyManager.notify(NOTIFICATION_ID, notification);
      Session.setNotificationVisible(true);
   }

   /**
    * Starts the location manager. There are two location managers - GPS and
    * Cell Tower. This code determines which manager to request updates from
    * based on user preference and whichever is enabled. If GPS is enabled on
    * the phone, that is used. But if the user has also specified that they
    * prefer cell towers, then cell towers are used. If neither is enabled,
    * then nothing is requested.
    */
   private void startGpsManager() {
      Utilities.logDebug("GpsLoggingService.startGpsManager");

      getPreferences();

      if (gpsLocationListener == null) {
         gpsLocationListener = new GeneralLocationListener(this);
      }

      if (towerLocationListener == null) {
         towerLocationListener = new GeneralLocationListener(this);
      }

      gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      towerLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

      checkTowerAndGpsStatus();

      if (Session.isGpsEnabled() && !AppSettings.shouldPreferCellTower()) {
         Utilities.logInfo("Requesting GPS location updates");
         // gps satellite based
         gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, gpsLocationListener);

         gpsLocationManager.addGpsStatusListener(gpsLocationListener);

         Session.setUsingGps(true);
      } else if (Session.isTowerEnabled()) {
         Utilities.logInfo("Requesting tower location updates");
         Session.setUsingGps(false);
         // Cell tower and wifi based
         towerLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, towerLocationListener);

      } else {
         Utilities.logInfo("No provider available");
         Session.setUsingGps(false);
         setStatus(R.string.gpsprovider_unavailable);
         setFatalMessage(R.string.gpsprovider_unavailable);
         stopLogging();
         return;
      }

      setStatus(R.string.started);
   }

   /**
    * This method is called periodically to determine whether the cell tower /
    * gps providers have been enabled, and sets class level variables to those
    * values.
    */
   private void checkTowerAndGpsStatus() {
      Session.setTowerEnabled(towerLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
      Session.setGpsEnabled(gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
   }

   /**
    * Stops the location managers
    */
   private void stopGpsManager() {
      Utilities.logDebug("GpsLoggingService.stopGpsManager");

      if (towerLocationListener != null) {
         Utilities.logDebug("Removing towerLocationManager updates");
         towerLocationManager.removeUpdates(towerLocationListener);
      }

      if (gpsLocationListener != null) {
         Utilities.logDebug("Removing gpsLocationManager updates");
         gpsLocationManager.removeUpdates(gpsLocationListener);
         gpsLocationManager.removeGpsStatusListener(gpsLocationListener);
      }

      setStatus(getString(R.string.stopped));
   }

   /**
    * Gives a status message to the main service client to display
    *
    * @param status The status message
    */
   void setStatus(String status) {
      if (isMainFormVisible()) {
         mainServiceClient.onStatusMessage(status);
      }
   }

   /**
    * Gives an error message to the main service client to display
    *
    * @param messageId ID of string to lookup
    */
   void setFatalMessage(int messageId) {
      if (isMainFormVisible()) {
         mainServiceClient.onFatalMessage(getString(messageId));
      }
   }

   /**
    * Gets string from given resource ID, passes to setStatus(String)
    *
    * @param stringId ID of string to lookup
    */
   private void setStatus(int stringId) {
      String s = getString(stringId);
      setStatus(s);
   }

   /**
    * Notifies main form that logging has stopped
    */
   void stopMainActivity() {
      if (isMainFormVisible()) {
         mainServiceClient.onStopLogging();
      }
   }

   /**
    * Stops location manager, then starts it.
    */
   void restartGpsManagers() {
      Utilities.logDebug("GpsLoggingService.restartGpsManagers");

      stopGpsManager();
      startGpsManager();
   }

   /**
    * This event is raised when the GeneralLocationListener has a new location.
    * This method in turn updates notification, writes to file, reobtains
    * preferences, notifies main service client and resets location managers.
    *
    * @param loc Location object
    */
   void onLocationChanged(Location loc) {
      int retryTimeout = Session.getRetryTimeout();

      if (!Session.isStarted()) {
         Utilities.logDebug("onLocationChanged called, but Session.isStarted is false");
         stopLogging();
         return;
      }

      Utilities.logDebug("GpsLoggingService.onLocationChanged");

      long currentTimeStamp = System.currentTimeMillis();

      // Wait some time even on 0 frequency so that the UI doesn't lock up

      if ((currentTimeStamp - Session.getLatestTimeStamp()) < 1000) {
         return;
      }

      // Don't do anything until the user-defined time has elapsed
      if (!forceLogOnce() && (currentTimeStamp - Session.getLatestTimeStamp()) < (AppSettings.getMinimumSeconds() * 1000)) {
         return;
      }

      // Don't do anything until the user-defined accuracy is reached
      if (AppSettings.getMinimumAccuracyInMeters() > 0) {
         if (AppSettings.getMinimumAccuracyInMeters() < Math.abs(loc.getAccuracy())) {
            if (retryTimeout < 50) {
               Session.setRetryTimeout(retryTimeout + 1);
               setStatus("Only accuracy of " + String.valueOf(Math.floor(loc.getAccuracy())) + " reached");
               stopManagerAndResetAlarm(AppSettings.getRetryInterval());
               return;
            } else {
               Session.setRetryTimeout(0);
               setStatus("Only accuracy of " + String.valueOf(Math.floor(loc.getAccuracy())) + " reached and timeout reached");
               stopManagerAndResetAlarm();
               return;
            }
         }
      }

      //Don't do anything until the user-defined distance has been traversed
      if (!forceLogOnce() && AppSettings.getMinimumDistanceInMeters() > 0 && Session.hasValidLocation()) {

         double distanceTraveled = Utilities.calculateDistance(loc.getLatitude(), loc.getLongitude(),
                 Session.getCurrentLatitude(), Session.getCurrentLongitude());

         if (AppSettings.getMinimumDistanceInMeters() > distanceTraveled) {
            setStatus("Only " + String.valueOf(Math.floor(distanceTraveled)) + " m traveled.");
            stopManagerAndResetAlarm();
            return;
         }

      }

      Utilities.logInfo("New location obtained");
      Session.setLatestTimeStamp(System.currentTimeMillis());
      Session.setCurrentLocationInfo(loc);
      setDistanceTraveled(loc);
      Notify();
      writeToFile(loc);
      getPreferences();
      stopManagerAndResetAlarm();
      setForceLogOnce(false);

      if (isMainFormVisible()) {
         mainServiceClient.onLocationUpdate(loc);
      }
   }

   private void setDistanceTraveled(Location loc) {
      // Distance
      if (Session.getPreviousLocationInfo() == null) {
         Session.setPreviousLocationInfo(loc);
      }

      // Calculate this location and the previous location location and add to the current running total distance.
      // NOTE: Should be used in conjunction with 'distance required before logging' for more realistic values.
      double distance = Utilities.calculateDistance(
              Session.getPreviousLatitude(),
              Session.getPreviousLongitude(),
              loc.getLatitude(),
              loc.getLongitude());

      Session.setPreviousLocationInfo(loc);
      Session.setTotalTravelled(Session.getTotalTravelled() + distance);
   }

   protected void stopManagerAndResetAlarm() {
      Utilities.logDebug("GpsLoggingService.stopManagerAndResetAlarm");

      if (!AppSettings.shouldkeepFix()) {
         stopGpsManager();
      }

      setAlarmForNextPoint();
   }

   protected void stopManagerAndResetAlarm(int retryInterval) {
      Utilities.logDebug("GpsLoggingService.StopManagerAndResetAlarm_retryInterval");

      if (!AppSettings.shouldkeepFix()) {
         stopGpsManager();
      }

      setAlarmForNextPoint(retryInterval);
   }

   private void stopAlarm() {
      Utilities.logDebug("GpsLoggingService.stopAlarm");

      Intent i = new Intent(this, GpsLoggingService.class);
      i.putExtra("getnextpoint", true);
      PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
      nextPointAlarmManager.cancel(pi);
   }

   private void setAlarmForNextPoint() {
      Utilities.logDebug("GpsLoggingService.setAlarmForNextPoint");

      Intent i = new Intent(this, GpsLoggingService.class);

      i.putExtra("getnextpoint", true);

      PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
      nextPointAlarmManager.cancel(pi);

      nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AppSettings.getMinimumSeconds() * 1000, pi);
   }

   private void setAlarmForNextPoint(int retryInterval) {
      Utilities.logDebug("GpsLoggingService.SetAlarmForNextPoint_retryInterval");

      Intent i = new Intent(this, GpsLoggingService.class);

      i.putExtra("getnextpoint", true);

      PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
      nextPointAlarmManager.cancel(pi);

      nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + retryInterval * 1000, pi);
   }

   /**
    * Calls file helper to write a given location to a file.
    *
    * @param location Location object
    */
   private void writeToFile(Location location) {
      Utilities.logDebug("GpsLoggingService.writeToFile");

      List<ILocationLogger> loggers = LocationLoggerFactory.getLoggers(this);
      boolean atLeastOneAnnotationSuccess = false;

      for (ILocationLogger locationLogger : loggers) {
         try {
            locationLogger.log(location);
            if (Session.hasDescription()) {
               locationLogger.log(location, Session.getDescription());
               atLeastOneAnnotationSuccess = true;
            }
         } catch (Exception e) {
            setStatus(R.string.could_not_write_to_file);
         }
      }

      if (atLeastOneAnnotationSuccess) {
         Session.clearDescription();
         if (isMainFormVisible()) {
            mainServiceClient.onClearAnnotation();
         }
      }
   }

   /**
    * Informs the main service client of the number of visible satellites.
    *
    * @param count Number of Satellites
    */
   void setSatelliteInfo(int count) {
      if (isMainFormVisible()) {
         mainServiceClient.onSatelliteCount(count);
      }
   }

   private boolean isMainFormVisible() {
      return mainServiceClient != null;
   }

   /**
    * Can be used from calling classes as the go-between for methods and
    * properties.
    */
   public class GpsLoggingBinder extends Binder {
      public GpsLoggingService getService() {
         Utilities.logDebug("GpsLoggingBinder.getService");
         return GpsLoggingService.this;
      }
   }

}
