package com.mendhak.gpslogger.senders.post;

import android.content.Context;
import android.os.Build;
import com.google.gson.Gson;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.db.LocationDbColumnType;
import com.mendhak.gpslogger.db.LocationDbHelper;
import com.mendhak.gpslogger.senders.IPublisher;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
      Utilities.LogInfo("AutoPostHelper - publish");

      Thread t = new Thread(new AutoSendHandler(this, context));
      t.start();
   }

   public void onComplete()
   {
      Utilities.LogInfo("POST complete");

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
         Utilities.LogInfo("Publishing via POST");

         Map<Integer, Map<String, String>> records = locationDbHelper.getUnPublishedRecords();

         if (records == null || records.isEmpty()) {
            Utilities.LogInfo("No records to publish");

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

         Utilities.LogInfo("POSTed " + rowsUpdated + " records");
      }
      catch (Exception e)
      {
         Utilities.LogError("AutoSendHandler.run", e);

         helper.onFailure();

         return;
      }

      helper.onComplete();
   }

}