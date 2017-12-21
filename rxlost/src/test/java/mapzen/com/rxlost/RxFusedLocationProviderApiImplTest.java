package mapzen.com.rxlost;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;

import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import org.junit.Test;

import static org.mockito.Mockito.mock;

public class RxFusedLocationProviderApiImplTest {

    RxFusedLocationProviderApi api = new RxFusedLocationProviderApiImpl();

    @Test(expected = RuntimeException.class)
    public void getLastLocation() throws Exception {
        api.getLastLocation(mock(RxLostApiClient.class));
    }

    @Test(expected = RuntimeException.class)
    public void getLocationAvailability() throws Exception {
        api.getLocationAvailability(mock(RxLostApiClient.class));
    }

    @Test(expected = RuntimeException.class)
    public void requestLocationUpdates_listener() throws Exception {
        api.requestLocationUpdates(mock(RxLostApiClient.class), mock(LocationRequest.class),
                mock(LocationListener.class));
    }

    @Test(expected = RuntimeException.class)
    public void requestLocationUpdates_listener_looper() throws Exception {
        api.requestLocationUpdates(mock(RxLostApiClient.class), mock(LocationRequest.class),
                mock(LocationListener.class), mock(Looper.class));
    }

    @Test(expected = RuntimeException.class)
    public void requestLocationUpdates_callback() throws Exception {
        api.requestLocationUpdates(mock(RxLostApiClient.class), mock(LocationRequest.class),
                mock(LocationCallback.class), mock(Looper.class));
    }

    @Test(expected = RuntimeException.class)
    public void requestLocationUpdates_pendingIntent() throws Exception {
        api.requestLocationUpdates(mock(RxLostApiClient.class), mock(LocationRequest.class),
                mock(PendingIntent.class));
    }

    @Test(expected = RuntimeException.class)
    public void removeLocationUpdates_listener() throws Exception {
        api.removeLocationUpdates(mock(RxLostApiClient.class), mock(LocationListener.class));
    }

    @Test(expected = RuntimeException.class)
    public void removeLocationUpdates_callback() throws Exception {
        api.removeLocationUpdates(mock(RxLostApiClient.class), mock(LocationCallback.class));
    }

    @Test(expected = RuntimeException.class)
    public void removeLocationUpdates_pendingIntent() throws Exception {
        api.removeLocationUpdates(mock(RxLostApiClient.class), mock(PendingIntent.class));
    }

    @Test(expected = RuntimeException.class)
    public void setMockMode() throws Exception {
        api.setMockMode(mock(RxLostApiClient.class), mock(Boolean.class));
    }

    @Test(expected = RuntimeException.class)
    public void setMockLocation() throws Exception {
        api.setMockLocation(mock(RxLostApiClient.class), mock(Location.class));
    }

    @Test(expected = RuntimeException.class)
    public void setMockTrace() throws Exception {
        api.setMockTrace(mock(RxLostApiClient.class), mock(String.class), mock(String.class));
    }

    @Test(expected = RuntimeException.class)
    public void requestLocationUpdates() throws Exception {
        api.requestLocationUpdates(mock(RxLostApiClient.class), mock(LocationRequest.class));
    }
}
