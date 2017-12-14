package mapzen.com.rxlost;

/**
 * Created by sarahlensing on 10/23/17.
 */

public class RxLocationServices {

  public static final RxFusedLocationProviderApi FusedLocationApi =
      new RxFusedLocationProviderApiImpl();
  public static final RxGeofencingApi GeofencingApi;
  public static final RxSettingsApi SettingsApi;
}
