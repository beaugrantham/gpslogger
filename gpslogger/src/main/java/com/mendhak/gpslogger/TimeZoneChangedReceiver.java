package com.mendhak.gpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mendhak.gpslogger.common.slf4j.Logs;

import org.slf4j.Logger;

import java.util.TimeZone;

/**
 * Listens for android.intent.action.ACTION_TIMEZONE_CHANGED and adjusts
 * default TimeZone as necessary.
 */
public class TimeZoneChangedReceiver extends BroadcastReceiver {

    private static final Logger LOG = Logs.of(TimeZoneChangedReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        String currentTimeZoneId = TimeZone.getDefault().getID();
        String newTimeZoneId = intent.getStringExtra("time-zone");

        try {
            TimeZone.setDefault(TimeZone.getTimeZone(newTimeZoneId));

            LOG.info("Timezone changed from [" + currentTimeZoneId + "] to [" + newTimeZoneId + "]");
            LOG.info("TimeZone.getDefault().getID() now returns [" + TimeZone.getDefault().getID() + "]");
        }
        catch (IllegalArgumentException e) {
            LOG.error("Could not recognize timezone id [" + newTimeZoneId + "]", e);
        }
    }

}
