package com.mendhak.gpslogger.db;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 3/7/14
 * Time: 8:33 PM
 * To change this template use File | Settings | File Templates.
 */
public enum LocationDbColumnType {

   ID ("id"),
   LATITUDE ("latitude"),
   LONGITUDE ("longitude"),
   ANNOTATION ("annotation"),
   SATELLITES ("satellites"),
   ALTITUDE ("altitude"),
   SPEED ("speed"),
   ACCURACY ("accuracy"),
   DIRECTION ("direction"),
   PROVIDER ("provider"),
   TIME ("time"),
   PUBLISHED ("published");

   private final String columnName;

   private LocationDbColumnType(String columnName) {
      this.columnName = columnName;
   }

   public String getColumnName() {
      return this.columnName;
   }

   public static String[] getColumnNames() {
      LocationDbColumnType[] values = LocationDbColumnType.values();

      String[] columnNames = new String[values.length];

      for (int i = 0; i < values.length; i++) {
         columnNames[i] = values[i].getColumnName();
      }

      return columnNames;
   }

}
