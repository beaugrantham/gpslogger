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

package com.mendhak.gpslogger;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import com.mendhak.gpslogger.common.Utilities;
import org.nologs.gpslogger.R;

import java.util.Iterator;

class GeneralLocationListener implements LocationListener, GpsStatus.Listener
{

    private static GpsLoggingService mainActivity;

    GeneralLocationListener(GpsLoggingService activity)
    {
        Utilities.logDebug("GeneralLocationListener constructor");
        mainActivity = activity;
    }

    /**
     * Event raised when a new fix is received.
     */
    @Override
    public void onLocationChanged(Location loc)
    {
        try
        {
            if (loc != null)
            {
                Utilities.logVerbose("GeneralLocationListener.onLocationChanged");
                mainActivity.onLocationChanged(loc);
            }

        }
        catch (Exception ex)
        {
            Utilities.logError("GeneralLocationListener.onLocationChanged", ex);
            mainActivity.setStatus(ex.getMessage());
        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Utilities.logInfo("Provider disabled");
        Utilities.logDebug(provider);
        mainActivity.restartGpsManagers();
    }

    @Override
    public void onProviderEnabled(String provider)
    {

        Utilities.logInfo("Provider enabled");
        Utilities.logDebug(provider);
        mainActivity.restartGpsManagers();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        if (status == LocationProvider.OUT_OF_SERVICE)
        {
            Utilities.logDebug(provider + " is out of service");
            mainActivity.stopManagerAndResetAlarm();
        }

        if (status == LocationProvider.AVAILABLE)
        {
            Utilities.logDebug(provider + " is available");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
        {
            Utilities.logDebug(provider + " is temporarily unavailable");
            mainActivity.stopManagerAndResetAlarm();
        }
    }

    @Override
    public void onGpsStatusChanged(int event)
    {

        switch (event)
        {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Utilities.logDebug("GPS Event First Fix");
                mainActivity.setStatus(mainActivity.getString(R.string.fix_obtained));
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                Utilities.logDebug("GPS Satellite status obtained");
                GpsStatus status = mainActivity.gpsLocationManager.getGpsStatus(null);

                int maxSatellites = status.getMaxSatellites();

                Iterator<GpsSatellite> it = status.getSatellites().iterator();
                int count = 0;

                while (it.hasNext() && count <= maxSatellites)
                {
                    it.next();
                    count++;
                }

                mainActivity.setSatelliteInfo(count);
                break;

            case GpsStatus.GPS_EVENT_STARTED:
                Utilities.logInfo("GPS started, waiting for fix");
                mainActivity.setStatus(mainActivity.getString(R.string.started_waiting));
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                Utilities.logInfo("GPS Stopped");
                mainActivity.setStatus(mainActivity.getString(R.string.gps_stopped));
                break;

        }
    }

}
