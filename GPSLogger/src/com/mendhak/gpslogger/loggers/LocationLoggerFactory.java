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

package com.mendhak.gpslogger.loggers;

import android.content.Context;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.Session;
import com.mendhak.gpslogger.db.LocationDbHelper;
import com.mendhak.gpslogger.loggers.customurl.HttpUrlLogger;

import java.util.ArrayList;
import java.util.List;

public class LocationLoggerFactory
{
    public static List<ILocationLogger> getLoggers(Context context)
    {
        List<ILocationLogger> loggers = new ArrayList<ILocationLogger>();

        if(AppSettings.shouldLogToCustomUrl())
        {
            loggers.add(new HttpUrlLogger(AppSettings.getCustomLoggingUrl(), Session.getSatelliteCount()));
        }

        loggers.add(new LocationDbHelper(context));

        return loggers;
    }
}
