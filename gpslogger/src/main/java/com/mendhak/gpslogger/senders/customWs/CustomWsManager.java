package com.mendhak.gpslogger.senders.customWs;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

        final ContentResolver contentResolver = this.context.getContentResolver();

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                List<Point> unpublishedPoints = db.pointDao().getUnpublished();

                if (!unpublishedPoints.isEmpty()) {
                    Map<String, Map<String, String>> unpublishedPointMap = new LinkedHashMap<>();

                    for (Point p : unpublishedPoints) {
                        unpublishedPointMap.put(String.valueOf(p.getId()), pointToMap(p, contentResolver));
                    }

                    try {
                        String service = preferenceHelper.getCustomWsService();

                        LOG.debug("Sending to custom WS: " + service);

                        URL url = new URL(service);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

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

                        int status = conn.getResponseCode();

                        conn.disconnect();

                        LOG.debug("STATUS: " + status);

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            for (Point p : unpublishedPoints) {
                                db.pointDao().publish(p.getId());
                            }

                            return unpublishedPoints.size();
                        }
                    } catch (Exception e) {
                        LOG.error("Failed while sending data to custom WS", e);
                        LOG.error(e.getMessage());
                    }
                }

                return 0;
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

    private Map<String, String> pointToMap(Point point, ContentResolver contentResolver) {
        Map<String, String> map = new HashMap<>();

        map.put("time", String.valueOf(point.getTime()));
        map.put("user", !Strings.isNullOrEmpty(preferenceHelper.getCustomWsUserId()) ? preferenceHelper.getCustomWsUserId() : "");
        map.put("satellites", null);
        map.put("annotation", point.getAnnotation());
        map.put("altitude", null);
        map.put("latitude", String.valueOf(point.getLatitude()));
        map.put("accuracy", String.valueOf(point.getAccuracy()));
        map.put("speed", null);
        map.put("direction", null);
        map.put("longitude", String.valueOf(point.getLongitude()));
        map.put("media", uriToBase64(point.getMedia(), point.getId(), contentResolver));

        return map;
    }

    private static String uriToBase64(String uri, int id, ContentResolver contentResolver) {
        if (Strings.isNullOrEmpty(uri)) {
            return null;
        }

        try {
            Bitmap scaledImage = scaleImage(Uri.parse(uri), contentResolver);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledImage.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageAsBytes = stream.toByteArray();

            String imageAsBase64 = Base64.encodeToString(imageAsBytes, Base64.NO_WRAP);

            LOG.info("Point " + id + " had media of " + imageAsBase64.length() + " bytes");

            return imageAsBase64;
        } catch (Exception e) {
            LOG.error("Unable to extract content from URI " + uri, e);
        }

        return null;
    }

    private static Bitmap scaleImage(Uri uri, ContentResolver contentResolver) throws IOException {
        InputStream input = contentResolver.openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 600) ? (originalSize / 600) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
        input = contentResolver.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));

        return k == 0 ? 1 : k;
    }

}