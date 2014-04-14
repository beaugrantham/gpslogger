package com.mendhak.gpslogger;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.db.LocationDbColumnType;
import com.mendhak.gpslogger.db.LocationDbHelper;
import org.nologs.gpslogger.R;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/24/14
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocationHistoryActivity extends SherlockActivity implements LoaderManager.LoaderCallbacks<Cursor> {

   private SimpleCursorAdapter mAdapter;

   private static final String[] PROJECTION = new String[] {
           LocationDbColumnType.ID.getColumnName(),
           LocationDbColumnType.LATITUDE.getColumnName(),
           LocationDbColumnType.LONGITUDE.getColumnName(),
           "DATETIME(" + LocationDbColumnType.TIME.getColumnName() + " / 1000, 'unixepoch') AS " + LocationDbColumnType.TIME.getColumnName(),
           LocationDbColumnType.ACCURACY.getColumnName(),
           LocationDbColumnType.SATELLITES.getColumnName(),
           LocationDbColumnType.ANNOTATION.getColumnName(),
           "CASE WHEN " + LocationDbColumnType.PUBLISHED.getColumnName() + " = 1 THEN 'published' else '' END AS " + LocationDbColumnType.PUBLISHED.getColumnName()};

   /**
    * Event raised when the form is created for the first time
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      Utilities.logDebug("LocationHistoryActivity.onCreate");

      super.onCreate(savedInstanceState);

      setContentView(R.layout.location_history);

      ListView listView = (ListView) findViewById(R.id.location_history);

      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView latitudeTextView = (TextView) view.findViewById(R.id.loc_history_row_latitude);
            CharSequence latitude = latitudeTextView.getText();

            TextView longitudeTextView = (TextView) view.findViewById(R.id.loc_history_row_longitude);
            CharSequence longitude = longitudeTextView.getText();

            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=" + latitude + "," + longitude));
            startActivity(intent);
         }
      });

      // Create a progress bar to display while the list loads
      ProgressBar progressBar = new ProgressBar(this);
      progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      progressBar.setIndeterminate(true);
      listView.setEmptyView(progressBar);

      // Must add the progress bar to the root of the layout
      ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
      root.addView(progressBar);

      String[] fromColumns = {
              LocationDbColumnType.ID.getColumnName(),
              LocationDbColumnType.LATITUDE.getColumnName(),
              LocationDbColumnType.LONGITUDE.getColumnName(),
              LocationDbColumnType.TIME.getColumnName(),
              LocationDbColumnType.ACCURACY.getColumnName(),
              LocationDbColumnType.SATELLITES.getColumnName(),
              LocationDbColumnType.ANNOTATION.getColumnName(),
              LocationDbColumnType.PUBLISHED.getColumnName()};
      int[] toViews = {
              R.id.loc_history_row_id,
              R.id.loc_history_row_latitude,
              R.id.loc_history_row_longitude,
              R.id.loc_history_row_time,
              R.id.loc_history_row_accuracy,
              R.id.loc_history_row_satellites,
              R.id.loc_history_row_annotation,
              R.id.loc_history_row_published};

      mAdapter = new SimpleCursorAdapter(this, R.layout.location_history_row, null, fromColumns, toViews, 0);
      listView.setAdapter(mAdapter);

      getLoaderManager().initLoader(0, null, this);

      // enable the home button so you can go back to the main screen
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      return new CursorLoader(this, null, PROJECTION, null, null, LocationDbColumnType.ID.getColumnName() + " DESC") {
         @Override
         public Cursor loadInBackground() {
            LocationDbHelper helper = new LocationDbHelper(getApplicationContext());
            SQLiteDatabase database = helper.getReadableDatabase();

            return database.query(LocationDbHelper.TABLE_NAME, getProjection(), getSelection(), getSelectionArgs(), null, null, getSortOrder(), null);
         }
      };
   }

   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      mAdapter.swapCursor(data);
   }

   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
      mAdapter.swapCursor(null);
   }

   /**
    * Called when one of the menu items is selected.
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      int itemId = item.getItemId();

      Utilities.logInfo("Option item selected - " + String.valueOf(item.getTitle()));

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
