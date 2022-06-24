package pt.ulisboa.tecnico.cmov.cmovproject.chat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;

import pt.ulisboa.tecnico.cmov.cmovproject.ChatActivity;
import pt.ulisboa.tecnico.cmov.cmovproject.R;


public class ChatGPSListener implements LocationListener {
    private double latitude;
    private double longitude;
    private ChatActivity chatActivity;
    private Location currentLoc;
    private Location loc; // room center
    private Double roomRadius;

    public ChatGPSListener(ChatActivity myActivity, Double roomLat, Double roomLon, Double roomRadius) {
        this.chatActivity = myActivity;
        LocationManager locationManager = (LocationManager) chatActivity.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(myActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);

        loc = new Location("");
        loc.setLatitude(roomLat);
        loc.setLongitude(roomLon);
        this.roomRadius = roomRadius;

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("Location", location.toString());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        currentLoc = new Location(location);

        while(currentLoc == null) {}
        Float distance = loc.distanceTo(currentLoc);
        if (distance > roomRadius) {
            this.chatActivity.setIsOutOfRoom(true);
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}
