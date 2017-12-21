package mapzen.com.rxlost;

import android.app.PendingIntent;

import com.mapzen.android.lost.api.GeofencingRequest;

import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.mock;

public class RxGeofencingApiImplTest {

    RxGeofencingApi api = new RxGeofencingApiImpl();

    @Test(expected = RuntimeException.class)
    public void addGeofences_request() throws Exception {
        api.addGeofences(mock(RxLostApiClient.class), mock(GeofencingRequest.class),
                mock(PendingIntent.class));
    }

    @Test(expected = RuntimeException.class)
    public void addGeofences_pendingIntent() throws Exception {
        api.addGeofences(mock(RxLostApiClient.class), mock(List.class), mock(PendingIntent.class));
    }

    @Test(expected = RuntimeException.class)
    public void removeGeofences_list() throws Exception {
        api.removeGeofences(mock(RxLostApiClient.class), mock(List.class));
    }

    @Test(expected = RuntimeException.class)
    public void removeGeofences_pendingIntent() throws Exception {
        api.removeGeofences(mock(RxLostApiClient.class), mock(PendingIntent.class));
    }

    @Test(expected = RuntimeException.class)
    public void requestGeofences_request() throws Exception {
        api.requestGeofences(mock(RxLostApiClient.class), mock(GeofencingRequest.class));
    }

    @Test(expected = RuntimeException.class)
    public void requestGeofences_list() throws Exception {
        api.requestGeofences(mock(RxLostApiClient.class), mock(List.class));
    }
}
