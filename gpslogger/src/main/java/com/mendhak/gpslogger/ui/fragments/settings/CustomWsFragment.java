/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mendhak.gpslogger.ui.fragments.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.common.EventBusHook;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.events.UploadEvents;
import com.mendhak.gpslogger.senders.PreferenceValidator;
import com.mendhak.gpslogger.senders.customWs.CustomWsManager;
import com.mendhak.gpslogger.ui.fragments.PermissionedPreferenceFragment;

import de.greenrobot.event.EventBus;

public class CustomWsFragment extends PermissionedPreferenceFragment implements
        OnPreferenceChangeListener,  OnPreferenceClickListener, PreferenceValidator {

    private final PreferenceHelper preferenceHelper;
    CustomWsManager customWsManager;

    public CustomWsFragment(){
        this.preferenceHelper = PreferenceHelper.getInstance();
        this.customWsManager = new CustomWsManager(this.getActivity(), preferenceHelper);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.customwssettings);

        findPreference("customws_enabled").setOnPreferenceChangeListener(this);
        findPreference("customws_service").setOnPreferenceChangeListener(this);
        findPreference("customws_basic_auth").setOnPreferenceChangeListener(this);
        findPreference("customws_user_id").setOnPreferenceChangeListener(this);

        registerEventBus();
    }

    @Override
    public void onDestroy() {
        unregisterEventBus();
        super.onDestroy();
    }

    private void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    private void unregisterEventBus(){
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t){
            //this may crash if registration did not go through. just be safe
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        return true;
    }


    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    @Override
    public boolean isValid() {
        return !customWsManager.hasUserAllowedAutoSending() || customWsManager.isAvailable();
    }

    @EventBusHook
    public void onEventMainThread(UploadEvents.CustomWs o) {

    }
}
