package org.traccar.geocoder;

public interface Geocoder {
  String getAddress(double paramDouble1, double paramDouble2, ReverseGeocoderCallback paramReverseGeocoderCallback);
  
  public static interface ReverseGeocoderCallback {
    void onSuccess(String param1String);
    
    void onFailure(Throwable param1Throwable);
  }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\Geocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */