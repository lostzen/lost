package mapzen.com.rxlost;

/**
 * Main entry point for RxJava APIs cooresponding to
 * {@link com.mapzen.android.lost.api.LocationServices}.
 */
public class RxLocationServices {

  public static final RxFusedLocationProviderApi FusedLocationApi =
      new RxFusedLocationProviderApiImpl();

  public static final RxGeofencingApi GeofencingApi = new RxGeofencingApiImpl();

  public static final RxSettingsApi SettingsApi = new RxSettingsApiImpl();
}
