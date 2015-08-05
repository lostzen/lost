package com.mapzen.android.lost.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class GeofencingRequest implements Parcelable {

    public static final int INITIAL_TRIGGER_DWELL = 4;
    public static final int INITIAL_TRIGGER_ENTER = 1;
    public static final int INITIAL_TRIGGER_EXIT = 2;

    public static final Parcelable.Creator<GeofencingRequest> CREATOR = null;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    public static final class Builder {
        GeofencingRequest builtGeofencingRequest;

        public GeofencingRequest.Builder addGeofence(Geofence geofence) {
            throw new RuntimeException("Sorry, not yet implemented");

//            return this;
        }

        public GeofencingRequest.Builder addGeofences(List<Geofence> geofences) {
            throw new RuntimeException("Sorry, not yet implemented");

//            return this;
        }

        public GeofencingRequest build() {
            throw new RuntimeException("Sorry, not yet implemented");
//            builtGeofencingRequest=new GeofencingRequestImpl();
//            return builtGeofencingRequest;
        }

        public GeofencingRequest.Builder setInitialTrigger(int initialTrigger) {
            throw new RuntimeException("Sorry, not yet implemented");

//            return this;
        }
    }
}
