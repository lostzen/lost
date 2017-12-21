package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LocationSettingsRequest;

import org.junit.Test;

import static org.mockito.Mockito.mock;

public class RxSettingsApiImplTest {

    RxSettingsApi api = new RxSettingsApiImpl();

    @Test(expected = RuntimeException.class)
    public void checkLocationSettings() throws Exception {
        api.checkLocationSettings(mock(RxLostApiClient.class), mock(LocationSettingsRequest.class));
    }
}
