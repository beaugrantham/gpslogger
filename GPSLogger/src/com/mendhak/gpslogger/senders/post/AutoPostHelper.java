package com.mendhak.gpslogger.senders.post;

import android.content.Context;
import com.google.gson.Gson;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.db.LocationDbHelper;
import com.mendhak.gpslogger.senders.IPublisher;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/26/14
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoPostHelper implements IActionListener, IPublisher {

   private final Context context;
   private final IActionListener callback;

   public AutoPostHelper(IActionListener callback, Context context)
   {
      this.callback = callback;
      this.context = context;
   }

   @Override
   public void publish()
   {
      Utilities.logInfo("AutoPostHelper - publish");

      Thread t = new Thread(new AutoSendHandler(this, context));
      t.start();
   }

   public void onComplete()
   {
      Utilities.logInfo("POST complete");

      callback.onComplete();
   }

   public void onFailure()
   {
      callback.onFailure();
   }

}

class AutoSendHandler implements Runnable
{

   private final IActionListener helper;

   private final LocationDbHelper locationDbHelper;

   public AutoSendHandler(IActionListener helper, Context context)
   {
      this.helper = helper;

      locationDbHelper = new LocationDbHelper(context);
   }

   public void run()
   {
      try
      {
         Utilities.logInfo("Publishing via POST");

         Map<Integer, Map<String, String>> records = locationDbHelper.getUnPublishedRecords();

         if (records == null || records.isEmpty()) {
            Utilities.logInfo("No records to publish");

            helper.onComplete();

            return;
         }

         Gson gson = new Gson();
         String json = gson.toJson(records);

         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httppost = new HttpPost(AppSettings.getPostUrl());

         StringEntity se = new StringEntity(json);
         se.setContentType("application/json");
         httppost.setEntity(se);

         HttpResponse response = httpclient.execute(httppost);

         // TODO - evaluate response for success

         int rowsUpdated = locationDbHelper.publish(records.keySet());

         if (rowsUpdated != records.size()) {
            throw new Exception("Only " + rowsUpdated + " of " + records.size() + " flagged as published");
         }

         Utilities.logInfo("POSTed " + rowsUpdated + " records");
      }
      catch (Exception e)
      {
         Utilities.logError("AutoSendHandler.run", e);

         helper.onFailure();

         return;
      }

      helper.onComplete();
   }

}