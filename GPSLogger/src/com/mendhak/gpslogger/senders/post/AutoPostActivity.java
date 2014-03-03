package com.mendhak.gpslogger.senders.post;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mendhak.gpslogger.GpsMainActivity;
import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.IMessageBoxCallback;
import com.mendhak.gpslogger.common.Utilities;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/26/14
 * Time: 8:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoPostActivity extends SherlockPreferenceActivity implements
        Preference.OnPreferenceChangeListener, IMessageBoxCallback, IActionListener,
        Preference.OnPreferenceClickListener {

   private final Handler handler = new Handler();

   private final Runnable successfullySent = new Runnable() {
      public void run() {
         SuccessfulSending();
      }
   };

   private final Runnable failedSend = new Runnable() {
      public void run() {
         FailureSending();
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // enable the home button so you can go back to the main screen
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      addPreferencesFromResource(R.xml.autopostsettings);

      CheckBoxPreference chkEnabled = (CheckBoxPreference) findPreference("autopost_enabled");
      chkEnabled.setOnPreferenceChangeListener(this);

      EditTextPreference txtUrl = (EditTextPreference) findPreference("post_url");
      txtUrl.setOnPreferenceChangeListener(this);
   }

   /**
    * Called when one of the menu items is selected.
    */
   public boolean onOptionsItemSelected(MenuItem item) {

      int itemId = item.getItemId();
      Utilities.LogInfo("Option item selected - " + String.valueOf(item.getTitle()));

      switch (itemId) {
         case android.R.id.home:
            Intent intent = new Intent(this, GpsMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            break;
      }
      return super.onOptionsItemSelected(item);
   }

   public boolean onPreferenceClick(Preference preference) {

      if (!IsFormValid()) {
         Utilities.MsgBox(getString(R.string.autopost_invalid_form),
                 getString(R.string.autopost_invalid_form_message),
                 AutoPostActivity.this);
         return false;
      }

      return true;
   }

   private boolean IsFormValid() {
      CheckBoxPreference chkEnabled = (CheckBoxPreference) findPreference("autopost_enabled");
      EditTextPreference txtUrl = (EditTextPreference) findPreference("post_url");

      return !chkEnabled.isChecked() || txtUrl.getText() != null && txtUrl.getText().length() > 0;
   }

   public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
         if (!IsFormValid()) {
            Utilities.MsgBox(getString(R.string.autopost_invalid_form),
                    getString(R.string.autopost_invalid_form_message),
                    this);
            return false;
         } else {
            return super.onKeyDown(keyCode, event);
         }
      } else {
         return super.onKeyDown(keyCode, event);
      }
   }

   public void MessageBoxResult(int which) {
      finish();
   }

   public boolean onPreferenceChange(Preference preference, Object newValue) {

      return true;
   }

   private void FailureSending() {
      Utilities.HideProgress();
      Utilities.MsgBox(getString(R.string.sorry), getString(R.string.error_connection), this);
   }

   private void SuccessfulSending() {
      Utilities.HideProgress();
      // Utilities.MsgBox(getString(R.string.success), getString(R.string.autopost_success), this);
      Utilities.MsgBox(getString(R.string.success), getString(R.string.success), this);
   }

   public void onComplete() {
      handler.post(successfullySent);
   }

   public void onFailure() {

      handler.post(failedSend);

   }

}
