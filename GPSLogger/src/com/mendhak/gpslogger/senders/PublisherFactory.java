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

package com.mendhak.gpslogger.senders;

import android.content.Context;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.senders.email.AutoEmailHelper;
import com.mendhak.gpslogger.senders.post.AutoPostHelper;

import java.util.ArrayList;
import java.util.List;

public class PublisherFactory
{

    public static void publish(IActionListener callback, Context context)
    {
//        if (!gpxFolder.exists())
//        {
//            callback.onFailure();
//            return;
//        }

        List<IPublisher> senders = getPublishers(callback, context);

        for (IPublisher sender : senders)
        {
            sender.publish();
        }
    }


    public static List<IPublisher> getPublishers(IActionListener callback, Context context)
    {
        Utilities.logInfo("Getting available publishers");

        List<IPublisher> senders = new ArrayList<IPublisher>();

        if (AppSettings.isAutoEmailEnabled())
        {
            senders.add(new AutoEmailHelper(callback));
        }

        if (AppSettings.isAutoPostEnabled())
        {
           Utilities.logInfo("Creating AutoPostHelper");
           senders.add(new AutoPostHelper(callback, context));
        }

        Utilities.logInfo("Returning " + senders.size() + " senders");

        return senders;

    }
}
