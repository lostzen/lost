package mapzen.com.rxlost;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RxLocationServicesTest {

    @Test public void fusedApi_isNotNull() throws Exception {
        assertThat(RxLocationServices.FusedLocationApi).isNotNull();
    }

    @Test public void geofencingApi_isNotNull() throws Exception {
        assertThat(RxLocationServices.GeofencingApi).isNotNull();
    }

    @Test public void settingsApi_isNotNull() throws Exception {
        assertThat(RxLocationServices.SettingsApi).isNotNull();
    }
}
