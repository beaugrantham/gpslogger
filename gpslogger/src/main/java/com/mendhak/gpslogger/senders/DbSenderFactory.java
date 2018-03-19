package com.mendhak.gpslogger.senders;

import android.content.Context;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.mendhak.gpslogger.senders.customWs.CustomWsManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DbSenderFactory {

    private static final Logger LOG = Logs.of(DbSenderFactory.class);

    public static DbSender getCustomWsSender(Context context) {
        return new CustomWsManager(context, PreferenceHelper.getInstance());
    }

    public static void autoSend(Context context) {
        LOG.info("Auto-sending DB records");

        List<DbSender> senders = getDbAutosenders(context);

        for (DbSender sender : senders) {
            LOG.debug("Sender: " + sender.getClass().getName());

            sender.upload();
        }
    }

    private static List<DbSender> getDbAutosenders(Context context) {
        List<DbSender> senders = new ArrayList<>();

        if (getCustomWsSender(context).isAutoSendAvailable()){
            senders.add(getCustomWsSender(context));
        }

        return senders;
    }

}