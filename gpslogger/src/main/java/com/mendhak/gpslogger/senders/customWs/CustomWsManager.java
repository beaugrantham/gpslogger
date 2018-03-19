package com.mendhak.gpslogger.senders.customWs;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.db.LocationDatabase;
import com.mendhak.gpslogger.common.db.Point;
import com.mendhak.gpslogger.common.events.UploadEvents;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.mendhak.gpslogger.senders.DbSender;

import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class CustomWsManager extends DbSender {

    private static final Logger LOG = Logs.of(CustomWsManager.class);

    PreferenceHelper preferenceHelper;

    private Context context;

    public CustomWsManager(Context context, PreferenceHelper helper) {
        this.context = context;
        this.preferenceHelper = helper;
    }

    @Override
    public void upload() {
        final LocationDatabase db = LocationDatabase.getLocationDatabase(context);

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                List<Point> unpublishedPoints = db.pointDao().getUnpublished();

                if (!unpublishedPoints.isEmpty()) {
                    Map<String, Map<String, String>> unpublishedPointMap = new LinkedHashMap<>();

                    for (Point p : unpublishedPoints) {
                        unpublishedPointMap.put(String.valueOf(p.getId()), pointToMap(p));
                    }

                    try {
                        String service = preferenceHelper.getCustomWsService();

                        LOG.debug("Sending to custom WS: " + service);

                        URL url = new URL(service);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        String basicAuth = preferenceHelper.getCustomWsBasicAuth();
                        if (!Strings.isNullOrEmpty(basicAuth)) {
                            LOG.debug("Using provided authentication");

                            String auth = "Basic " + Base64.encodeToString(basicAuth.getBytes(), Base64.NO_WRAP);
                            conn.setRequestProperty("Authorization", auth);
                        }

                        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                        os.writeBytes((new JSONObject(unpublishedPointMap)).toString());

                        os.flush();
                        os.close();

                        LOG.debug("STATUS: " + String.valueOf(conn.getResponseCode()));
                        LOG.debug("MSG: " + conn.getResponseMessage());

                        conn.disconnect();

                        for (Point p : unpublishedPoints) {
                            db.pointDao().publish(p.getId());
                        }
                    } catch (Exception e) {
                        LOG.error("Failed while sending data to custom WS", e);
                    }
                }

                return unpublishedPoints.size();
            }

            @Override
            protected void onPostExecute(Integer param) {
                LOG.info("Uploaded " + param + " records to custom WS");

                UploadEvents.BaseUploadEvent callbackEvent = new UploadEvents.CustomWs();
                EventBus.getDefault().post(callbackEvent.succeeded());
            }
        }.execute();
    }

    @Override
    public boolean isAvailable() {
        return !Strings.isNullOrEmpty(preferenceHelper.getCustomWsService());
    }

    @Override
    public boolean hasUserAllowedAutoSending() {
        return preferenceHelper.isCustomWsEnabled();
    }

    private Map<String, String> pointToMap(Point point) {
        Map<String, String> map = new HashMap<>();

        map.put("time", String.valueOf(point.getTime()));
        map.put("user", !Strings.isNullOrEmpty(preferenceHelper.getCustomWsUserId()) ? preferenceHelper.getCustomWsUserId() : "");
        map.put("satellites", null);
        map.put("annotation", null);
        map.put("altitude", null);
        map.put("latitude", String.valueOf(point.getLatitude()));
        map.put("accuracy", String.valueOf(point.getAccuracy()));
        map.put("speed", null);
        map.put("direction", null);
        map.put("longitude", String.valueOf(point.getLongitude()));

        return map;
    }

}