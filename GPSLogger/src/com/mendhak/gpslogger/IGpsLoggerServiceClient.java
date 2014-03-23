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

import android.app.Activity;
import android.location.Location;

interface IGpsLoggerServiceClient
{

    /**
     * New message from the service to be displayed on the activity form.
     *
     * @param message
     */
    public void onStatusMessage(String message);

    /**
     * Indicates that a fatal error has occurred, logging will stop.
     *
     * @param message
     */
    public void onFatalMessage(String message);

    /**
     * A new location fix has been obtained.
     *
     * @param loc
     */
    public void onLocationUpdate(Location loc);

    /**
     * New satellite count has been obtained.
     *
     * @param count
     */
    public void onSatelliteCount(int count);

    /**
     * Asking the calling activity form to clear itself.
     */
    public void clearForm();

    /**
     * Asking the calling activity form to indicate that logging has stopped
     */
    public void onStopLogging();

    /**
     * Asking the calling activity form to indicate that an annotation is pending
     */
    public void onSetAnnotation();

    /**
     * Asking the calling activity form to indicate that no annotation is pending
     */
    public void onClearAnnotation();

    /**
     * Returns the activity
     *
     * @return
     */
    public Activity getActivity();

}
