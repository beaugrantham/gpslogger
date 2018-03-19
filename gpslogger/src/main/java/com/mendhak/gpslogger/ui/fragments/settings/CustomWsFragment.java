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
