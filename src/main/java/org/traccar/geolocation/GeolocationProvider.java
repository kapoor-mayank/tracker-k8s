package org.traccar.geolocation;

import org.traccar.model.Network;

public interface GeolocationProvider {
  void getLocation(Network paramNetwork, LocationProviderCallback paramLocationProviderCallback);
  
  public static interface LocationProviderCallback {
    void onSuccess(double param1Double1, double param1Double2, double param1Double3);
    
    void onFailure(Throwable param1Throwable);
  }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geolocation\GeolocationProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */