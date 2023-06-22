package com.ridhojagis.kebunbinatangahp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ridhojagis.kebunbinatangahp.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    double RADIUS = 6378160;

    Button btnChat;
    Button btnNavigation;

    private float ZOOM_PREFERENCE = 20.0F;
    private float ZOOM_CHAT = 17.0f;
    private float ZOOM_MAX = 21.0F;

    List<Fasilitas> fasilitasList;
    List<Koleksi> koleksiList;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRefFasilitas = database.getReference("Fasilitas");
    DatabaseReference mtRefKoleksi = database.getReference("Koleksi");

    //maps
    double[] LatLong;

    FusedLocationProviderClient fusedLocationProviderClient;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnChat = findViewById(R.id.btnChat);
        btnNavigation = findViewById(R.id.btnNavigation);

        LatLong = new double[2];

        if (!isConnected(this)) {
            showInternetAlert();
        }
        if(!isGPSEnabled()){
            showGPSAlert();
        }
        if(!isBackgroundLocationPermited()){
            showThis("Akses Lokasi pada Latar Belakang","Izinkan aplikasi mengakses lokasi sepanjang waktu untuk fitur yang lebih lengkap");
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //Geofencing
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Chatbot.class);
                startActivity(intent);
//                onBackPressed();
            }

        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        fasilitasList = new ArrayList<>();
        koleksiList = new ArrayList<>();

        mMap = googleMap;
        LatLng camera_coordinate = new LatLng(-7.294990, 112.737138);
        UiSettings uiSettings = googleMap.getUiSettings();
        Intent intent = new Intent(this, GeofenceBot.class);

        //Add marker to the map
        myRefFasilitas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fasilitasList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Log.i("MAP_GET_DATA_CHILDREN", String.valueOf(dataSnapshot.getChildren()));
                    Log.i("MAP_GET_DATA_ONLY", String.valueOf(data));

                    Fasilitas fasilitas = data.getValue(Fasilitas.class);
                    mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                    .position(new LatLng(fasilitas.getLatitude(), fasilitas.getLongitude()))
                                    .snippet(fasilitas.getDeskripsi()+"\nJam Buka"+fasilitas.getJam_buka()+"\nJam Tutup"+fasilitas.getJam_tutup()))
                            .setTitle(fasilitas.getNama());
                    fasilitasList.add(fasilitas);
                    Log.i("MAP_GET_FACILITY", fasilitas.getNama());
                    Log.i("BANYAK_FASILITAS_AFT", String.valueOf(fasilitasList.size()));
                }
                Log.i("LIST_FASILITAS", String.valueOf(fasilitasList));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("message", "Failed to read value.", error.toException());
            }
        });

        mtRefKoleksi.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                koleksiList.clear();
                Log.i("MAP_GET_DATA_SNAPSHOT", String.valueOf(dataSnapshot));
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Koleksi koleksi = data.getValue(Koleksi.class);
                    String jenis = data.child("jenis").getValue(String.class);
                    Log.i("MAP_GET_JENIS", jenis);
                    if(!(jenis.equals("Fasilitas"))) {
                        mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                        .position(new LatLng(koleksi.getLatitude(), koleksi.getLongitude()))
                                        .snippet("Jenis: " + koleksi.getJenis() + "\nJam Buka" + koleksi.getJam_buka() + "-" + koleksi.getJam_tutup()))
                                .setTitle(koleksi.getNama());
                    }
                    koleksiList.add(koleksi);
                    Log.i("MAP_GET_KOLEKSI", koleksi.getNama());
                    Log.i("BANYAK_KOLEKSI", String.valueOf(koleksiList.size()));
                    LatLng koleksi_coor = new LatLng(koleksi.getLatitude(), koleksi.getLongitude());
                    if(isBackgroundLocationPermited()){
                        createGeofence(koleksi_coor, koleksi);
                    }
                }

                Log.i("LIST_KOLEKSI", String.valueOf(koleksiList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("message", "Failed to read value.", error.toException());
            }
        });

        Log.i("BANYAK_FASILITAS_tengah", String.valueOf(fasilitasList.size()));

        btnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermission();
                if (isGPSEnabled()) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000, 2, new LocationListener() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onLocationChanged(Location location) {
                                    if(location == null){
                                        showLocationRequestFailed();
                                    }
                                    else {
                                        LatLong[0] = location.getLatitude();
                                        LatLong[1] = location.getLongitude();
                                        Log.i("GET_LOCATION_LAT", Double.toString(LatLong[0]));
                                        Log.i("GET_LOCATION_LNG", Double.toString(LatLong[1]));
                                        setKoleksiDistance(koleksiList, LatLong);
                                    }
                                }

                                @Override
                                public void onStatusChanged(String s, int i, Bundle bundle) {

                                }

                                @Override
                                public void onProviderEnabled(String s) {

                                }

                                @Override
                                public void onProviderDisabled(String s) {

                                }
                            }
                    );
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            LatLng latLngFacility = extras.getParcelable("COORDINATE_FACILITY");
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngFacility));
            mMap.setMinZoomPreference(ZOOM_CHAT);
        }
        else {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            checkLocationPermission();
            if (isGPSEnabled()) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000, 2, new LocationListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onLocationChanged(Location location) {
                                LatLong[0] = location.getLatitude();
                                LatLong[1] = location.getLongitude();
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(LatLong[0], LatLong[1])));
                                mMap.setMinZoomPreference(ZOOM_CHAT);
                                locationManager.removeUpdates(this);
                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {

                            }
                        }
                );
            }
            else{
                mMap.moveCamera(CameraUpdateFactory.newLatLng(camera_coordinate));
                mMap.setMinZoomPreference(ZOOM_PREFERENCE);
                showGPSAlert();
            }

        }

        Log.i("MAP_CAMERA_POSITION", String.valueOf(camera_coordinate));

        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        checkLocationPermission();
        if(isGPSEnabled()){
            mMap.setMyLocationEnabled(true);
        }
    }

    private void showLocationRequestFailed() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Gagal Mendapatkan Lokasi");
        builder.setMessage("Gagal mendapatkan lokasi anda, silahkan coba kembali");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setKoleksiDistance(List<Koleksi> koleksiList, double[] latLong) {
        double dLat;
        double dLon;
        double distance;
        for(int i=0;i<koleksiList.size();i++) {
            dLat = Math.toRadians(koleksiList.get(i).getLatitude() - latLong[0]);
            dLon = Math.toRadians(koleksiList.get(i).getLongitude() - latLong[1]);
            distance = RADIUS * 2 *
                    Math.asin(
                            Math.sqrt(
                                    Math.pow(Math.sin(dLat/2),2) + Math.cos(Math.toRadians(latLong[0])) * Math.cos(Math.toRadians(koleksiList.get(i).getLatitude())) * Math.pow(Math.sin(dLon/2),2)));
            koleksiList.get(i).setJarak(distance);
            Log.i("NAMA", String.valueOf(koleksiList.get(i).getNama()));
            Log.i("JARAK", String.valueOf(koleksiList.get(i).getJarak()));
        }
    }

    private boolean isConnected(MapsActivity mapsActivity){
        ConnectivityManager connectivityManager = (ConnectivityManager) mapsActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(connectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE);

        if((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())){
            return true;
        }
        return false;
    }

    private boolean isGPSEnabled(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private Boolean isBackgroundLocationPermited(){
        if(Build.VERSION.SDK_INT > 28){
            if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void checkBackgroundLocationPermission() {
        if(Build.VERSION.SDK_INT > 28){
            if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 44);
            }
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
        }
    }

    private void showGPSAlert() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("GPS tidak aktif");
        builder.setMessage("Beberapa fitur tidak bisa berjalan.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showInternetAlert(){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Tidak ada koneksi");
        builder.setMessage("Aplikasi ini membutuhkan akses internet. Harap periksa kembali koneksi anda.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                onBackPressed();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createGeofence(LatLng latLng, Koleksi koleksi) {
        Geofence geofence = geofenceHelper.setGeofence(String.valueOf(koleksi.getId()), latLng);
        GeofencingRequest geofencingRequest = geofenceHelper.setGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getGeofencePendingIntent(koleksi);
        //check permission
        checkLocationPermission();
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i("GEOFENCING", "Successfully added Geofence");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("GEOFENCING ERROR", geofenceHelper.getError(e));
                    }
                });

    }

    public void createCircle(LatLng latLng){
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(50)
                .strokeColor(Color.argb(255, 255, 0, 0))
                .fillColor(Color.argb(64, 255,0,0))
                .strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    private void showThis(String title, String content){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
