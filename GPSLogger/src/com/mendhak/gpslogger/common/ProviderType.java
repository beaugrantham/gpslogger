package com.mendhak.gpslogger.common;

import org.nologs.gpslogger.R;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 4/4/14
 * Time: 8:48 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ProviderType {

   GPS(R.string.provider_value_gps),
   GPS_WITH_NETWORK_FALLBACK(R.string.provider_value_gps_with_network_fallback),
   NETWORK(R.string.provider_value_network);

   private int resx;

   private ProviderType(int resx) {
      this.resx = resx;
   }

   public int getResx() {
      return this.resx;
   }
}
