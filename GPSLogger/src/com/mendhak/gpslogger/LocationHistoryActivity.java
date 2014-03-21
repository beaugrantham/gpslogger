package com.mendhak.gpslogger;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mendhak.gpslogger.common.Utilities;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/24/14
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocationHistoryActivity extends SherlockActivity {

   /**
    * Event raised when the form is created for the first time
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {

      Utilities.LogDebug("LocationHistoryActivity.onCreate");

      super.onCreate(savedInstanceState);

      setContentView(R.layout.location_history);

      // TODO

      // enable the home button so you can go back to the main screen
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   /**
    * Called when one of the menu items is selected.
    */
   @Override
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

}
