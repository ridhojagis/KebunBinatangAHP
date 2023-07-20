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
import android.content.res.Configuration;
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
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ridhojagis.kebunbinatangahp.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    double RADIUS = 6378160;
    int SIZE_MATRIX_JARAK = 15;
    int SIZE_MATRIX_JENIS = 6;
    int SIZE_MATRIX_STATUS = 3;
    int SIZE_MATRIX_MINAT = 5;

    DatabaseHelper databaseHelper;

    String currentTime;

    // AHP
    private double[][] pairwiseMatrix;
    private double[][] pairwiseMatrixJarak;
    private double[][] pairwiseMatrixJenis;
    private double[][] pairwiseMatrixStatus;
    private double[][] pairwiseMatrixMinat;

    // Priotitas kriteria utama
    double prioritas_jarak = 0.0;
    double prioritas_jenis = 0.0;
    double prioritas_statusBuka = 0.0;
    double prioritas_minat = 0.0;

    double[] prioritas_kriteria_jarak = new double[SIZE_MATRIX_JARAK];
    double[] prioritas_kriteria_jenis = new double[SIZE_MATRIX_JENIS];
    double[] prioritas_kriteria_status = new double[SIZE_MATRIX_STATUS];
    double[] prioritas_kriteria_minat = new double[SIZE_MATRIX_MINAT];

    ArrayList<Koleksi> koleksiAHPList;
    ArrayList<Koleksi> koleksiAHPFinal;
    List<Koleksi> shortestRoute;
    List<Koleksi> riwayatList;

    Button btnChat;
    Button btnRiwayat;
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
    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnChat = findViewById(R.id.btnChat);
        btnRiwayat = findViewById(R.id.btnRiwayat);
        btnNavigation = findViewById(R.id.btnNavigation);

        databaseHelper = new DatabaseHelper(this);
//        // Query ke database untuk mendapatkan koleksi yang ada
//        riwayatList = databaseHelper.getRiwayatList();

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

        setCurrentTime();

        // Mengecek apakah Intent memiliki extra dengan kunci "pairwiseMatrix"
        if (getIntent().hasExtra("pairwiseMatrix")) {

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                pairwiseMatrix = (double[][]) extras.getSerializable("pairwiseMatrix");
                Log.d("GET_PAIRWISE_INTENT", "Berhasil mengatur pairwise intent");
                for (int i = 0; i < pairwiseMatrix.length; i++) {
                    for (int j = 0; j < pairwiseMatrix[i].length; j++) {
                        Log.d("PAIRWISE_MATRIX", "Value at [" + i + "][" + j + "]: " + pairwiseMatrix[i][j]);
                    }
                }
            }
        }
        else {
            // Matrix Default
            pairwiseMatrix = new double[][]{
                    {1.0, 0.5, 7.0, 3.0},   // Matriks perbandingan kriteria jarak
                    {2.0, 1.0, 8.0, 3.0},   // Matriks perbandingan kriteria jenis
                    {0.1428571429, 0.125, 1.0, 0.2},  // Matriks perbandingan kriteria status buka
                    {0.3333333333, 0.3333333333, 5.0, 1.0}  // Matriks perbandingan kriteria minat
            };
//            pairwiseMatrix = new double[][]{
//                    {1.0, 3.0, 0.2, 3.0},   // Matriks perbandingan kriteria jarak
//                    {0.3333333333, 1.0, 0.1428571429, 1.0},   // Matriks perbandingan kriteria jenis
//                    {5.0, 7.0, 1.0, 7.0},  // Matriks perbandingan kriteria status buka
//                    {0.3333333333, 1.0, 0.1428571429, 1.0}  // Matriks perbandingan kriteria minat
//            };
            Log.d("GET_PAIRWISE_DEFAULT", "Berhasil mengatur pairwise default");
            for (int i = 0; i < pairwiseMatrix.length; i++) {
                for (int j = 0; j < pairwiseMatrix[i].length; j++) {
                    Log.d("PAIRWISE_MATRIX", "Value at [" + i + "][" + j + "]: " + pairwiseMatrix[i][j]);
                }
            }
        }

        // Matriks perbandingan jarak koleksi
        pairwiseMatrixJarak = new double[][]{
                {1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 8, 9}, // Matriks perbandingan jarak <30
                {0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 8}, // Matriks perbandingan jarak 30-60
                {0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7}, // Matriks perbandingan jarak 60m-90m
                {0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5}, // Matriks perbandingan jarak 90m-120m
                {0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6}, // Matriks perbandingan jarak 120m-150m
                {0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5}, // Matriks perbandingan jarak 150m-180m
                {0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5}, // Matriks perbandingan jarak 180m-210m
                {0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5}, // Matriks perbandingan jarak 210m-240m
                {0.2, 0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5, 4}, // Matriks perbandingan jarak 240m-270m
                {0.1818181818, 0.2, 0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3, 3.5}, // Matriks perbandingan jarak 270m-300m
                {0.1666666667, 0.1818181818, 0.2, 0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5, 3}, // Matriks perbandingan jarak 300m-330m
                {0.1538461538, 0.1666666667, 0.1818181818, 0.2, 0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2, 2.5}, // Matriks perbandingan jarak 330m-360m
                {0.1428571429, 0.1538461538, 0.1666666667, 0.1818181818, 0.2, 0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5, 2}, // Matriks perbandingan jarak 360m-390m
                {0.125, 0.1428571429, 0.1538461538, 0.1666666667, 0.1818181818, 0.2, 0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1, 1.5}, // Matriks perbandingan jarak 390m-420m
                {0.1111111111, 0.125, 0.1428571429, 0.1538461538, 0.1666666667, 0.1818181818, 0.2, 0.2222222222, 0.25, 0.2857142857, 0.3333333333, 0.4, 0.5, 0.6666666667, 1} // Matriks perbandingan jarak >420m
        };

        // Matriks perbandingan status buka fasilitas kebun binatang
        pairwiseMatrixStatus = new double[][] {
                {1, 0.1428571429, 0.1428571429}, // Matriks perbandingan tidak buka
                {7, 1, 1}, // Matriks perbandingan selalu buka
                {7, 1, 1} // Matriks perbandingan buka
        };

        // Matriks perbandingan tingkat minat satwa atau fasilitas
        pairwiseMatrixMinat = new double[][] {
                {1, 2, 3, 4, 5}, // Matriks perbandingan Sangat diminati
                {0.5, 1, 2, 3, 4}, // Matriks perbandingan diminati
                {0.3333333333, 0.5, 1, 2, 3}, // Matriks perbandingan netral
                {0.25, 0.3333333333, 0.5, 1, 3}, // Matriks perbandingan tidak diminati
                {0.2, 0.25, 0.3333333333, 0.3333333333, 1} // Matriks perbandingan sangat tidak diminati
        };

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
            }
        });

        btnRiwayat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RiwayatActivity.class);
                if(shortestRoute != null) {
                    ArrayList<Koleksi> riwayatKunjungan = new ArrayList<>(shortestRoute);

                    // Mengirim daftar kunjungan menggunakan ArrayList melalui Intent
                    intent.putParcelableArrayListExtra("riwayatKunjungan", riwayatKunjungan);
                    shortestRoute.clear();
                }
                startActivity(intent);
            }
        });
    }

    private String setCurrentTime() {
        // Membuat objek Date
        Date date = new Date();
        // Membuat objek SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Mendapatkan waktu saat ini dalam bentuk string
        String currTime = sdf.format(date);
        currentTime = currTime;

        // Melakukan sesuatu dengan waktu saat ini (misalnya, mencetaknya di logcat)
        Log.d("Waktu Saat Ini", currentTime);
        return currTime;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        fasilitasList = new ArrayList<>();
        koleksiList = new ArrayList<>();
        final String[] koleksiGoals = new String[2];
        Log.d("ON_MAPS_READY", "Berhasil menjalankan map");

        btnNavigation.setVisibility(View.GONE); // Deklarasi visible button navigasi

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
                                    .snippet(fasilitas.getDeskripsi()+", Jam Buka "+fasilitas.getJam_buka()+"-" + fasilitas.getJam_tutup()))
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
                                        .snippet(koleksi.getJenis() + ", Jam Buka " + koleksi.getJam_buka() + "-" + koleksi.getJam_tutup()))
                                .setTitle(koleksi.getNama());
                    }
                    koleksiList.add(koleksi);
                    String logMessageKoleksi = "MAP_GET_KOLEKSI: " + koleksi.getNama() + ", MAP_GET_MINAT: " + koleksi.getMinat();
                    Log.i("MAP_GET_KOLEKSI", logMessageKoleksi);
                    Log.i("BANYAK_KOLEKSI", String.valueOf(koleksiList.size()));
                    LatLng koleksi_coor = new LatLng(koleksi.getLatitude(), koleksi.getLongitude());
                    if(isBackgroundLocationPermited()){
                        createGeofence(koleksi_coor, koleksi);
//                        createCircle(koleksi_coor);
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

        final boolean[] isButtonPressed = {false};
        final boolean[] isOnNavigation = {false};

        // fungsi ketika marker diklik
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("MARKER_CLICKED", marker.getTitle());
                // Dapatkan koleksi yang sesuai dengan marker yang diklik
                Koleksi koleksi = null;
                for (Koleksi k : koleksiList) {
                    if (k.getNama().equals(marker.getTitle())) {
                        koleksi = k;
                        break;
                    }
                }

                // Tampilkan Toast dengan informasi jenis
                if (koleksi != null) {
                    koleksiGoals[0] = koleksi.getNama();
                    koleksiGoals[1] = koleksi.getJenis();
                    String toastMessage = "Jenis: " + koleksi.getJenis() + "\nJam Buka: " + koleksi.getJam_buka() + "-" + koleksi.getJam_tutup();
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }

                btnNavigation.setVisibility(View.VISIBLE);

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Apakah sedang dalam navigasi
                if(!(isOnNavigation[0])) {
                    btnNavigation.setVisibility(View.GONE);
                }
            }
        });

        // Fungsi set nilai prioritas AHP
        priorityMainCriteria(pairwiseMatrix);
        priorityJarak(pairwiseMatrixJarak);
        priorityMinat(pairwiseMatrixMinat);

        btnNavigation.setOnClickListener(new View.OnClickListener() {
            Polyline previousPolyline = null;
            @Override
            public void onClick(View v) {
                Log.d("BTN_NAVIGATION_CLICKED", "Berhasil klik button navigasi");
                isButtonPressed[0] = !isButtonPressed[0]; // Mengubah status tombol saat tombol ditekan
                checkLocationPermission();

                if (isGPSEnabled()) {
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("Atur Bobot Kriteria");
                    builder.setMessage("Apakah Anda ingin mengatur bobot kriteria terlebih dahulu?");

                    // Tombol "iya"
                    builder.setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Kode aksi jika pengguna memilih "iya"
                            dialog.dismiss();
                            isButtonPressed[0] = !isButtonPressed[0];

                            // Lanjutkan dengan navigasi
                            navigateToFormActivity();
                        }
                    });

                    // Tombol "tidak"
                    builder.setNegativeButton("Tidak, Lanjut Navigasi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isOnNavigation[0] = true;
                            // Kode aksi jika pengguna memilih "tidak"
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

                            // Matriks perbandingan jenis koleksi
                            if(koleksiGoals[1].equals("Mamalia")){
                                pairwiseMatrixJenis = new double[][] {
                                        {1.0, 2.0, 2.0, 2.0, 2.0, 2.0}, // Matriks perbandingan mamalia
                                        {0.5, 1.0, 1.0, 1.0, 1.0, 1.0}, // Matriks perbandingan aves
                                        {0.5, 1.0, 1.0, 1.0, 1.0, 1.0}, // Matriks perbandingan reptil
                                        {0.5, 1.0, 1.0, 1.0, 1.0, 1.0}, // Matriks perbandingan amfibi
                                        {0.5, 1.0, 1.0, 1.0, 1.0, 1.0}, // Matriks perbandingan pisces
                                        {0.5, 1.0, 1.0, 1.0, 1.0, 1.0} // Matriks perbandingan fasilitas
                                };
                            }
                            else if(koleksiGoals[1].equals("Aves")){
                                pairwiseMatrixJenis = new double[][] {
                                        {1.0, 0.5, 1.0, 1.0, 1.0, 1.0},
                                        {2.0, 1.0, 2.0, 2.0, 2.0, 2.0},
                                        {1.0, 0.5, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 0.5, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 0.5, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 0.5, 1.0, 1.0, 1.0, 1.0}
                                };
                            }
                            else if(koleksiGoals[1].equals("Reptil")){
                                pairwiseMatrixJenis = new double[][] {
                                        {1.0, 1.0, 0.5, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 0.5, 1.0, 1.0, 1.0},
                                        {2.0, 2.0, 1.0, 2.0, 2.0, 2.0},
                                        {1.0, 1.0, 0.5, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 0.5, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 0.5, 1.0, 1.0, 1.0}
                                };
                            }
                            else if(koleksiGoals[1].equals("Amfibi")){
                                pairwiseMatrixJenis = new double[][] {
                                        {1.0, 1.0, 1.0, 0.5, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 0.5, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 0.5, 1.0, 1.0},
                                        {2.0, 2.0, 2.0, 1.0, 2.0, 2.0},
                                        {1.0, 1.0, 1.0, 0.5, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 0.5, 1.0, 1.0}
                                };
                            }
                            else if(koleksiGoals[1].equals("Pisces")){
                                pairwiseMatrixJenis = new double[][] {
                                        {1.0, 1.0, 1.0, 1.0, 0.5, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 0.5, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 0.5, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 0.5, 1.0},
                                        {2.0, 2.0, 2.0, 2.0, 1.0, 2.0},
                                        {1.0, 1.0, 1.0, 1.0, 0.5, 1.0}
                                };
                            }
                            else if(koleksiGoals[1].equals("Fasilitas")){
                                pairwiseMatrixJenis = new double[][] {
                                        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                                        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0}
                                };
                            }
                            priorityJenis(pairwiseMatrixJenis);

                            Log.i("prioritas_jarak", String.valueOf(prioritas_jarak));
                            Log.i("prioritas_jenis", String.valueOf(prioritas_jenis));
                            Log.i("prioritas_statusBuka", String.valueOf(prioritas_statusBuka));
                            Log.i("prioritas_minat", String.valueOf(prioritas_minat));

                            koleksiAHPList = new ArrayList<>();
                            koleksiAHPFinal = new ArrayList<>();
                            setKoleksiAHPScore(location.getLatitude(), location.getLongitude(), koleksiList, koleksiAHPList, koleksiAHPFinal, koleksiGoals[0]);

                            // Set lokasi pengguna menjadi True
                            int indexUser = -1;
                            for (int i = 0; i < shortestRoute.size(); i++) {
                                String waktuKunjungan = setCurrentTime();
                                if (shortestRoute.get(i).getNama().equals("Lokasi Pengunjung")) {
                                    shortestRoute.get(i).setVisited(true);
                                    shortestRoute.get(i).setWaktuKunjungan(waktuKunjungan);
                                    indexUser = i;
                                    break;
                                }
                            }


                            List<LatLng> points = new ArrayList<>();

                            // Periksa apakah ada polyline sebelumnya
                            if (previousPolyline != null) {
                                previousPolyline.remove(); // Hapus polyline sebelumnya
                            }

                            // Menambahkan polyline koordinat pengguna
                            points.add(new LatLng(LatLong[0], LatLong[1]));

                            for(int i=0;i<shortestRoute.size();i++) {
                                double distance = calculateDistance(shortestRoute.get(i), shortestRoute.get(indexUser));
                                Log.i("DISTANCE_TO_USER", "Urutan: " + i + ", Nama: " + shortestRoute.get(i).getNama() + " Jarak: " +
                                        distance + " Jenis: " + shortestRoute.get(i).getJenis() + " Status Buka: " + shortestRoute.get(i).getStatus_buka() +
                                        " Minat: " + shortestRoute.get(i).getMinat() + " AHP Score: " + shortestRoute.get(i).getAhp_score());
                                points.add(new LatLng(shortestRoute.get(i).getLatitude(), shortestRoute.get(i).getLongitude()));
                            }

                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.addAll(points);
                            polylineOptions.color(Color.BLUE);
                            polylineOptions.width(10);

                            previousPolyline = mMap.addPolyline(polylineOptions);
                            btnNavigation.setText("Stop Navigasi");

                            dialog.dismiss();
                        }
                    });

                    // Membuat dan menampilkan AlertDialog
                    AlertDialog dialog = builder.create();
                    if (isButtonPressed[0]) {
                        dialog.show();
                    }
                    else {
                        // Periksa apakah ada polyline sebelumnya
                        if (previousPolyline != null) {
                            previousPolyline.remove(); // Hapus polyline sebelumnya
                        }
                        isButtonPressed[0] = false;
                        isOnNavigation[0] = false;
                        btnNavigation.setText("Mulai Navigasi");
                        btnNavigation.setVisibility(View.GONE);
                    }
                }
            }
        });
        Boolean isIntent = false;

        if (getIntent().hasExtra("COORDINATE_FACILITY")) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                LatLng latLngFacility = extras.getParcelable("COORDINATE_FACILITY");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngFacility, 20.0F));
            }
        } else if (getIntent().hasExtra("COORDINATE_RIWAYAT")){
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                LatLng latLngRiwayat = extras.getParcelable("COORDINATE_RIWAYAT");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngRiwayat, 20.0F));
            }
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
                                // Query ke database untuk mendapatkan koleksi yang ada
                                riwayatList = databaseHelper.getRiwayatList();

                                LatLong[0] = location.getLatitude();
                                LatLong[1] = location.getLongitude();
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(LatLong[0], LatLong[1])));
                                mMap.setMinZoomPreference(ZOOM_CHAT);
//                                locationManager.removeUpdates(this);

                                drawCircleAreaLokasiSekarang(location);

                                String waktuKunjungan = setCurrentTime();

                                // Mendapatkan referensi ke RiwayatActivity
                                RiwayatActivity riwayatActivity = new RiwayatActivity();

                                // identifikasi apakah koleksi dalam shortestRoute telah dikunjungi
                                if(shortestRoute != null) {
                                    int userIndex = 0;
                                    for (int i = 0; i < shortestRoute.size(); i++) {
                                        double jarak;
                                        if (shortestRoute.get(i).getNama().equals("Lokasi Pengunjung")) {
                                            userIndex = i;
                                            shortestRoute.get(i).setLatitude(String.valueOf(location.getLatitude()));
                                            shortestRoute.get(i).setLongitude(String.valueOf(location.getLongitude()));
                                            continue;
                                        }

                                        // Menghitung jarak koleksi terhadap pengguna
                                        jarak = calculateDistance(shortestRoute.get(i), shortestRoute.get(userIndex));
                                        if (jarak <= 20) {
                                            shortestRoute.get(i).setVisited(true);
                                            shortestRoute.get(i).setWaktuKunjungan(waktuKunjungan);
                                        }
                                    }
                                }

                                // Dapatkan lokasi pengguna
                                double userLatitude = location.getLatitude();
                                double userLongitude = location.getLongitude();

                                // Loop melalui koleksi yang ada
                                if(riwayatList != null) {
                                    for (Koleksi koleksi : riwayatList) {
                                        if (!koleksi.isVisited()) {
                                            Log.i("KOLEKSI_NOT_VISITED", "ID: " + koleksi.getId() + " " + koleksi.getNama() + " Visited: " + koleksi.isVisited() + " " + koleksi.getWaktuKunjungan());
                                            double koleksiLatitude = koleksi.getLatitude();
                                            double koleksiLongitude = koleksi.getLongitude();

                                            // Hitung jarak antara lokasi pengguna dan koleksi menggunakan metode distanceBetween dari kelas Location
                                            float[] distanceResult = new float[1];

                                            Location.distanceBetween(userLatitude, userLongitude, koleksiLatitude, koleksiLongitude, distanceResult);
                                            float distance = distanceResult[0];

                                            // Jika jarak kurang dari batas tertentu, update status "visited" dan waktu kunjungan
                                            if (distance <= 20) {
                                                koleksi.setVisited(true);
                                                koleksi.setWaktuKunjungan(waktuKunjungan);

                                                // Update data di SQLite
                                                databaseHelper.updateRiwayatList(koleksi);
                                                riwayatActivity.updateRiwayatKunjungan(koleksi);
                                                String toastMessage = "Anda mengunjungi " + koleksi.getNama();
                                                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
//                                                    databaseHelper.updateRiwayatList(koleksi);
                                            }
                                        }
                                        else {
                                            Log.i("KOLEKSI_VISITED", "ID: " + koleksi.getId() + " " + koleksi.getNama() + " Visited: " + koleksi.isVisited() + " " + koleksi.getWaktuKunjungan());
                                        }
                                    }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Tambahkan kode yang ingin Anda jalankan saat terjadi perubahan konfigurasi
        // Misalnya, atur tampilan ulang atau lakukan tindakan lain yang diperlukan
    }

    private void drawCircleAreaLokasiSekarang(Location location) {
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());

        CircleOptions circleOptions = new CircleOptions().center(center).radius(20)  // radius dalam meter
                .strokeWidth(2) // ketebalan garis lingkaran
                .strokeColor(Color.BLUE) // warna garis lingkaran
                .fillColor(Color.parseColor("#804D90FE")); // warna isi lingkaran, di sini biru dengan alpha 50%

        if (circle != null) {
            circle.remove();
        }

        circle = mMap.addCircle(circleOptions);
    }

    private void navigateToFormActivity() {
        Intent intent = new Intent(MapsActivity.this, FormActivity.class);

        startActivity(intent);
    }

    private void setKoleksiAHPScore(double latitude, double longitude, List<Koleksi> koleksiList, ArrayList<Koleksi> koleksiAHPList, ArrayList<Koleksi> koleksiAHPFinal, String koleksiGoals) {
        double nilaiJarak, nilaiJenis, nilaiStatus, nilaiMinat;
        double skorAHP;
        double jarak;
        String minat;
        String jenis;

        // Menghitung skor AHP tiap koleksi
        for(int i=0;i<koleksiList.size();i++) {
            skorAHP = 0.0;
            jarak = koleksiList.get(i).getJarak();
            minat = koleksiList.get(i).getMinat();
            jenis = koleksiList.get(i).getJenis();

            nilaiJarak = setNilaiJarak(jarak);
            nilaiJenis = setNilaiJenis(jenis);
            nilaiStatus = 0.4666666667;
            nilaiMinat = setNilaiMinat(minat);

            skorAHP = prioritas_jarak * nilaiJarak + prioritas_jenis * nilaiJenis + prioritas_statusBuka * nilaiStatus + prioritas_minat * nilaiMinat;
            koleksiList.get(i).setAhp_score(skorAHP);

            String logMessageAHP = "NAMA: " + koleksiList.get(i).getNama()+ ", JARAK: " + koleksiList.get(i).getJarak()+ ", AHP SKOR: " + koleksiList.get(i).getAhp_score();
            Log.i("GET_KOLEKSI_AHP", logMessageAHP);
        }
        // Sort list koleksi berdasarkan skor AHP tertinggi
        Collections.sort(koleksiList, new KoleksiSortAHP());

        koleksiAHPList.clear();
        int index_tujuan = 0;

        // Menginput koleksi ke dalam list AHP Rank hingga menemukan koleksi tujuan
        for(int i=0;i<koleksiList.size();i++) {
            if(koleksiList.get(i).getNama().equals(koleksiGoals)){
                koleksiAHPList.add(koleksiList.get(i));
                String logMessageAHP = "NAMA: " + koleksiList.get(i).getNama()+ ", JARAK: " + koleksiList.get(i).getJarak()+ ", AHP SKOR: " + koleksiList.get(i).getAhp_score();
                Log.i("GET_KOLEKSI_AHP_SORT", logMessageAHP);

                index_tujuan = i;
                break;
            }
            else{
                koleksiAHPList.add(koleksiList.get(i));
                String logMessageAHP = "NAMA: " + koleksiList.get(i).getNama()+ ", JARAK: " + koleksiList.get(i).getJarak()+ ", AHP SKOR: " + koleksiList.get(i).getAhp_score();
                Log.i("GET_KOLEKSI_AHP_SORT", logMessageAHP);
                continue;
            }
        }
        // Sort list koleksi AHP berdasarkan jarak terdekat
        Collections.sort(koleksiAHPList, new KoleksiSortJarak());

        koleksiAHPFinal.clear();

        // Mneambah lokasi user ke index awal
        Koleksi user = new Koleksi();
        user.setNama("Lokasi Pengunjung");
        user.setLatitude(String.valueOf(LatLong[0]));
        user.setLongitude(String.valueOf(LatLong[1]));

        double min_distance = 500;
        // Menginput koleksi yang telah di sort ke dalam list final
        for(int i=0;i<koleksiAHPList.size();i++) {
            double dLat;
            double dLon;
            double distance;
            double distance_ij;
            double distance_to_user = 0;
            if(koleksiAHPList.get(i).getNama().equals(koleksiGoals)) {
                continue;
            }

            // Menghitung jarak koleksi dengan user
            distance_to_user = calculateDistance(user, koleksiAHPList.get(i));

            // Menghitung jarak koleksi terhadap tujuan
            dLat = Math.toRadians(koleksiAHPList.get(i).getLatitude() - koleksiList.get(index_tujuan).getLatitude());
            dLon = Math.toRadians(koleksiAHPList.get(i).getLongitude() - koleksiList.get(index_tujuan).getLongitude());
            distance = RADIUS * 2 *
                    Math.asin(
                            Math.sqrt(
                                    Math.pow(Math.sin(dLat/2),2) + Math.cos(Math.toRadians(koleksiList.get(index_tujuan).getLatitude())) * Math.cos(Math.toRadians(koleksiAHPList.get(i).getLatitude())) * Math.pow(Math.sin(dLon/2),2)));
            if((distance <= min_distance || distance_to_user <= 50)){
                min_distance = distance;
                koleksiAHPFinal.add(koleksiAHPList.get(i));
            }
            else if(i>0){
                for(int j=0; j<koleksiAHPFinal.size(); j++) {
                    if(calculateDistance(koleksiAHPFinal.get(j), koleksiAHPList.get(i)) <= 50) {
                        koleksiAHPFinal.add(koleksiAHPList.get(i));
                        break;
                    }
                }
            }

            String logMessageAHP = "Urutan ke-" + i + "NAMA: " + koleksiAHPList.get(i).getNama()+ ", LatLng: " + koleksiAHPList.get(i).getLatitude() + "," + koleksiAHPList.get(i).getLongitude() + ", JARAK: " + koleksiAHPList.get(i).getJarak()+ ", AHP SKOR: " + koleksiAHPList.get(i).getAhp_score();
            Log.i("GET_KOLEKSI_RANGE_SORT", logMessageAHP);
        }

    // Jarak tempuh terpendek dengan bruteforce
        List<Koleksi> tempRoute = new ArrayList<>();
        tempRoute = findShortestRoute(user, koleksiList.get(index_tujuan), koleksiAHPFinal);

        // Menambah koleksi tujuan ke akhir list
        shortestRoute = new ArrayList<>();
        for(int i=0; i<tempRoute.size();i++){
            if(tempRoute.get(i).getNama().equals(koleksiGoals)) {
                continue;
            }
            shortestRoute.add(tempRoute.get(i));
        }
        shortestRoute.add(koleksiList.get(index_tujuan));

        // Cek isi shortestRoute
        double tempuh = 0;
        for(int i=0; i<shortestRoute.size();i++){
            double dLat;
            double dLon;
            double distance;
            if(i==shortestRoute.size()-1) {
                String logMessageAHP = "Urutan ke-" + i + "= NAMA: " + shortestRoute.get(i).getNama()+ ", LatLng: " + shortestRoute.get(i).getLatitude() + "," + shortestRoute.get(i).getLongitude() + ", JARAK: " + shortestRoute.get(i).getJarak()+ ", AHP SKOR: " + shortestRoute.get(i).getAhp_score() + ", JARAK TEMPUH: " + tempuh;
                Log.i("GET_KOLEKSI_SHORTEST", logMessageAHP);
                break;
            }
            dLat = Math.toRadians(shortestRoute.get(i).getLatitude() - shortestRoute.get(i+1).getLatitude());
            dLon = Math.toRadians(shortestRoute.get(i).getLongitude() - shortestRoute.get(i+1).getLongitude());
            distance = RADIUS * 2 *
                    Math.asin(
                            Math.sqrt(
                                    Math.pow(Math.sin(dLat/2),2) + Math.cos(Math.toRadians(shortestRoute.get(i+1).getLatitude())) * Math.cos(Math.toRadians(shortestRoute.get(i).getLatitude())) * Math.pow(Math.sin(dLon/2),2)));
            tempuh += distance;
            String logMessageAHP = "Urutan ke-" + i + "= NAMA: " + shortestRoute.get(i).getNama()+ ", LatLng: " + shortestRoute.get(i).getLatitude() + "," + shortestRoute.get(i).getLongitude() + ", JARAK: " + shortestRoute.get(i).getJarak()+ ", AHP SKOR: " + shortestRoute.get(i).getAhp_score() + ", JARAK TEMPUH: " + tempuh;
            Log.i("GET_KOLEKSI_SHORTEST", logMessageAHP);
        }
    // End jarak terpendek dengan bruteforce

        // Meletakkan koleksi tujuan pada index terakhir dalam list
        koleksiAHPFinal.add(koleksiList.get(index_tujuan));

        double sum_distance = 0;
        for(int i=0;i<koleksiAHPFinal.size();i++) {
            double dLat;
            double dLon;
            double distance;
            if(i==koleksiAHPFinal.size()-1) {
                String logMessageAHP = "Urutan ke-" + i + "= NAMA: " + koleksiAHPFinal.get(i).getNama()+ ", LatLng: " +
                        koleksiAHPList.get(i).getLatitude() + "," + koleksiAHPList.get(i).getLongitude() + ", JARAK: " +
                        koleksiAHPFinal.get(i).getJarak()+ ", AHP SKOR: " + koleksiAHPFinal.get(i).getAhp_score() +
                        ", JARAK TEMPUH: " + sum_distance;
                Log.i("GET_KOLEKSI_FINAL", logMessageAHP);
                break;
            }
            // Menghitung jarak koleksi terhadap tujuan
            dLat = Math.toRadians(koleksiAHPList.get(i).getLatitude() - koleksiAHPList.get(i+1).getLatitude());
            dLon = Math.toRadians(koleksiAHPList.get(i).getLongitude() - koleksiAHPList.get(i+1).getLongitude());
            distance = RADIUS * 2 *
                    Math.asin(
                            Math.sqrt(
                                    Math.pow(Math.sin(dLat/2),2) + Math.cos(Math.toRadians(koleksiAHPList.get(i+1).getLatitude())) * Math.cos(Math.toRadians(koleksiAHPList.get(i).getLatitude())) * Math.pow(Math.sin(dLon/2),2)));
            sum_distance += distance;
            String logMessageAHP = "Urutan ke-" + i + "= NAMA: " + koleksiAHPFinal.get(i).getNama()+ ", LatLng: " + koleksiAHPList.get(i).getLatitude() + "," + koleksiAHPList.get(i).getLongitude() + ", JARAK: " + koleksiAHPFinal.get(i).getJarak()+ ", AHP SKOR: " + koleksiAHPFinal.get(i).getAhp_score() + ", JARAK TEMPUH: " + sum_distance;
            Log.i("GET_KOLEKSI_FINAL", logMessageAHP);
        }
    }

//    private List<Koleksi> findShortestRoute(Koleksi start, Koleksi destination, ArrayList<Koleksi> koleksiAHPFinal) {
//        List<Koleksi> shortestRoute = new ArrayList<>(koleksiAHPFinal);
//        shortestRoute.add(0, start);
//        shortestRoute.add(destination);
//
//        double shortestDistance = calculateTotalDistance(shortestRoute);
//
//        List<Koleksi> tempRoute = new ArrayList<>(shortestRoute);
//
//        // Menggunakan pendekatan brute force untuk mencari kombinasi urutan titik terpendek
//        for (int i = 1; i < koleksiAHPFinal.size() - 1; i++) {
//            for (int j = i + 1; j < koleksiAHPFinal.size() - 1; j++) {
//                // Menukar posisi titik i dan j
//                Collections.swap(tempRoute, i, j);
//
//                // Hitung jarak baru
//                double tempDistance = calculateTotalDistance(tempRoute);
//
//                // Periksa apakah jarak baru lebih pendek
//                if (tempDistance < shortestDistance) {
//                    shortestDistance = tempDistance;
//                    shortestRoute = new ArrayList<>(tempRoute);
//                }
//
//                // Kembalikan posisi titik ke semula
//                Collections.swap(tempRoute, i, j);
//            }
//        }
//        return shortestRoute;
//    }
    private List<Koleksi> findShortestRoute(Koleksi start, Koleksi destination, ArrayList<Koleksi> koleksiAHPFinal) {
        List<Koleksi> shortestRoute = new ArrayList<>();
        Set<Koleksi> unvisited = new HashSet<>(koleksiAHPFinal);
        Koleksi current = start;
        shortestRoute.add(current);
        unvisited.remove(current);

        while (!unvisited.isEmpty()) {
            Koleksi nearestNeighbor = null;
            double minDistance = Double.MAX_VALUE;

            for (Koleksi neighbor : unvisited) {
                double distance = calculateDistance(current, neighbor);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestNeighbor = neighbor;
                }
            }

            shortestRoute.add(nearestNeighbor);
            unvisited.remove(nearestNeighbor);
            current = nearestNeighbor;
        }

        shortestRoute.add(destination);
        return shortestRoute;
    }

    private double calculateTotalDistance(List<Koleksi> shortestRoute) {
        double totalDistance = 0;
        for (int i = 0; i < shortestRoute.size() - 1; i++) {
            totalDistance += calculateDistance(shortestRoute.get(i), shortestRoute.get(i + 1));
        }
        return totalDistance;
    }

    private double calculateDistance(Koleksi koleksi1, Koleksi koleksi2) {
        double lat1 = Math.toRadians(koleksi1.getLatitude());
        double lon1 = Math.toRadians(koleksi1.getLongitude());
        double lat2 = Math.toRadians(koleksi2.getLatitude());
        double lon2 = Math.toRadians(koleksi2.getLongitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIUS * c;
    }

    private double setNilaiJenis(String jenis) {
        double nilaiJenis = 0;
        // Nilai Minat
        if(jenis.contains("Mamalia")) {
            nilaiJenis = prioritas_kriteria_jenis[0];
        }
        else if(jenis.contains("Aves")) {
            nilaiJenis = prioritas_kriteria_jenis[1];
        }
        else if(jenis.contains("Reptil")) {
            nilaiJenis = prioritas_kriteria_jenis[2];
        }
        else if(jenis.contains("Amfibi")) {
            nilaiJenis = prioritas_kriteria_jenis[3];
        }
        else if(jenis.contains("Pisces")) {
            nilaiJenis = prioritas_kriteria_jenis[4];
        }
        else if(jenis.contains("Fasilitas")) {
            nilaiJenis = prioritas_kriteria_jenis[5];
        }
        return nilaiJenis;
    }

    private double setNilaiMinat(String minat) {
        double nilaiMinat;
        // Nilai Minat
        if(minat.contains("Sangat diminati")) {
            nilaiMinat = prioritas_kriteria_minat[0];
        }
        else if(minat.contains("Diminati")) {
            nilaiMinat = prioritas_kriteria_minat[1];
        }
        else if(minat.contains("Netral")) {
            nilaiMinat = prioritas_kriteria_minat[2];
        }
        else if(minat.contains("Tidak diminati")) {
            nilaiMinat = prioritas_kriteria_minat[3];
        }
        else{
            nilaiMinat = prioritas_kriteria_minat[4];
        }
        return nilaiMinat;
    }

    private double setNilaiJarak(double jarak) {
        double nilaiJarak;
        if(jarak <= 30.0) {
            nilaiJarak = prioritas_kriteria_jarak[0]; // jarak <=30m
        }
        else if(jarak > 30.0 && jarak <= 60.0) {
            nilaiJarak = prioritas_kriteria_jarak[1]; // jarak 30m-60m
        }
        else if(jarak > 60.0 && jarak <= 90.0) {
            nilaiJarak = prioritas_kriteria_jarak[2]; // jarak 60m-90m
        }
        else if(jarak > 90.0 && jarak <= 120.0) {
            nilaiJarak = prioritas_kriteria_jarak[3]; // jarak 90m-120m
        }
        else if(jarak > 120.0 && jarak <= 150.0) {
            nilaiJarak = prioritas_kriteria_jarak[4]; // jarak 120m-150m
        }
        else if(jarak > 150.0 && jarak <= 180.0) {
            nilaiJarak = prioritas_kriteria_jarak[5]; // jarak 150m-180m
        }
        else if(jarak > 180.0 && jarak <= 210.0) {
            nilaiJarak = prioritas_kriteria_jarak[6]; // jarak 180m-210m
        }
        else if(jarak > 210.0 && jarak <= 240.0) {
            nilaiJarak = prioritas_kriteria_jarak[7]; // jarak 210m-240m
        }
        else if(jarak > 240.0 && jarak <= 270.0) {
            nilaiJarak = prioritas_kriteria_jarak[8]; // jarak 240m-270m
        }
        else if(jarak > 270.0 && jarak <= 300.0) {
            nilaiJarak = prioritas_kriteria_jarak[9]; // jarak 270m-300m
        }
        else if(jarak > 300.0 && jarak <= 330.0) {
            nilaiJarak = prioritas_kriteria_jarak[10]; // jarak 300m-330m
        }
        else if(jarak > 330.0 && jarak <= 360.0) {
            nilaiJarak = prioritas_kriteria_jarak[11]; // jarak 330m-360m
        }
        else if(jarak > 360.0 && jarak <= 390.0) {
            nilaiJarak = prioritas_kriteria_jarak[12]; // jarak 360m-390m
        }
        else if(jarak > 390.0 && jarak <= 420.0) {
            nilaiJarak = prioritas_kriteria_jarak[13]; // jarak 390m-420m
        }
        else{
            nilaiJarak = prioritas_kriteria_jarak[14]; // jarak >420m
        }
        return nilaiJarak;
    }

    private void priorityMainCriteria(double[][] pairwiseMatrix) {
        int matrix_size = pairwiseMatrix.length;
        double nilaiMatrix[][] = new double[matrix_size][matrix_size];

        double CI = 0;
        double RI = 0.9;
        double CR = 0;

        //    Variabel matriks kriteria
        double sum_pairwiseMatrix_jarak = 0.0;
        double sum_pairwiseMatrix_jenis = 0.0;
        double sum_pairwiseMatrix_statusBuka = 0.0;
        double sum_pairwiseMatrix_minat = 0.0;
        double sum_nilai_jarak = 0.0;
        double sum_nilai_jenis = 0.0;
        double sum_nilai_statusBuka = 0.0;
        double sum_nilai_minat = 0.0;
        double eigen_value_jarak = 0.0;
        double eigen_value_jenis = 0.0;
        double eigen_value_statusBuka = 0.0;
        double eigen_value_minat = 0.0;
        double total_eigen_value = 0.0;

        for(int i=0;i<matrix_size;i++) {
            for(int j=0;j<matrix_size;j++) {
                if(j == 0) {
                    sum_pairwiseMatrix_jarak += pairwiseMatrix[i][j];
                }
                else if(j == 1) {
                    sum_pairwiseMatrix_jenis += pairwiseMatrix[i][j];
                }
                else if(j == 2) {
                    sum_pairwiseMatrix_statusBuka += pairwiseMatrix[i][j];
                }
                else if(j == 3) {
                    sum_pairwiseMatrix_minat += pairwiseMatrix[i][j];
                }
            }
        }

        for(int i=0;i<matrix_size;i++) {
            for(int j=0;j<matrix_size;j++) {
                if(j == 0) {
                    nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_jarak;
                }
                else if(j == 1) {
                    nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_jenis;
                }
                else if(j == 2) {
                    nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_statusBuka;
                }
                else if(j == 3) {
                    nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_minat;
                }
            }
        }

        for(int i=0;i<matrix_size;i++) {
            for(int j=0;j<matrix_size;j++) {
                if(i == 0) {
                    sum_nilai_jarak += nilaiMatrix[i][j];
                }
                else if(i == 1) {
                    sum_nilai_jenis += nilaiMatrix[i][j];
                }
                else if(i == 2) {
                    sum_nilai_statusBuka += nilaiMatrix[i][j];
                }
                else if(i == 3) {
                    sum_nilai_minat += nilaiMatrix[i][j];
                }
            }
        }

        prioritas_jarak = sum_nilai_jarak/matrix_size;
        prioritas_jenis = sum_nilai_jenis/matrix_size;
        prioritas_statusBuka = sum_nilai_statusBuka/matrix_size;
        prioritas_minat = sum_nilai_minat/matrix_size;

        eigen_value_jarak = prioritas_jarak * sum_pairwiseMatrix_jarak;
        eigen_value_jenis = prioritas_jenis * sum_pairwiseMatrix_jenis;
        eigen_value_statusBuka = prioritas_statusBuka * sum_pairwiseMatrix_statusBuka;
        eigen_value_minat = prioritas_minat * sum_pairwiseMatrix_minat;

        total_eigen_value = eigen_value_jarak + eigen_value_jenis + eigen_value_statusBuka + eigen_value_minat;

        CI = (total_eigen_value-matrix_size)/(matrix_size-1);
        CR = CI/RI;
        Log.i("CR_VALUE_MAIN", "CR = " + CR);
    }

    private void priorityJarak(double[][] pairwiseMatrixJarak) {
        int matrix_size = pairwiseMatrixJarak.length;

        double nilaiMatrix[][] = new double[matrix_size][matrix_size];
        double[] sum_pairwise_jarak = new double[matrix_size];
        double[] sum_nilai_jarak = new double[matrix_size];
        double[] eigen_value_jarak = new double[matrix_size];
        double total_eigen_value = 0;

        double CI = 0;
        double RI = 1.59;
        double CR = 0;

        for (int i = 0; i < matrix_size; i++) {
            for (int j = 0; j < matrix_size; j++) {
                if (j < matrix_size) {
                    sum_pairwise_jarak[j] += pairwiseMatrixJarak[i][j];
                }
            }
        }

        for(int i=0;i<matrix_size;i++) {
            for(int j=0;j<matrix_size;j++) {
                if(j < matrix_size) {
                    nilaiMatrix[i][j] = pairwiseMatrixJarak[i][j]/sum_pairwise_jarak[j];
                }
            }
        }

        for (int i = 0; i < matrix_size; i++) {
            for (int j = 0; j < matrix_size; j++) {
                sum_nilai_jarak[i] += nilaiMatrix[i][j];
            }
        }

        for (int i = 0; i < matrix_size; i++) {
            prioritas_kriteria_jarak[i] = sum_nilai_jarak[i]/matrix_size;
        }

        for (int i = 0; i < matrix_size; i++) {
            eigen_value_jarak[i] = prioritas_kriteria_jarak[i] * sum_pairwise_jarak[i];
        }

        for (int i = 0; i < matrix_size; i++) {
            total_eigen_value += eigen_value_jarak[i];
        }

        CI = (total_eigen_value-matrix_size)/(matrix_size-1);
        CR = CI/RI;
        Log.i("CR_VALUE_JARAK", "CR = " + CR);
    }

    private void priorityJenis(double[][] pairwiseMatrixJenis) {
        int matrix_size = pairwiseMatrixJenis.length;

        double nilaiMatrix[][] = new double[matrix_size][matrix_size];
        double[] sum_pairwise_jenis = new double[matrix_size];
        double[] sum_nilai_jenis = new double[matrix_size];
        double[] eigen_value_jenis = new double[matrix_size];
        double total_eigen_value = 0;

        double CI = 0;
        double RI = 1.24;
        double CR = 0;

        for (int i = 0; i < matrix_size; i++) {
            for (int j = 0; j < matrix_size; j++) {
                if (j < matrix_size) {
                    sum_pairwise_jenis[j] += pairwiseMatrixJenis[i][j];
                }
            }
        }

        for(int i=0;i<matrix_size;i++) {
            for(int j=0;j<matrix_size;j++) {
                if(j < matrix_size) {
                    nilaiMatrix[i][j] = pairwiseMatrixJenis[i][j]/sum_pairwise_jenis[j];
                }
            }
        }

        for (int i = 0; i < matrix_size; i++) {
            for (int j = 0; j < matrix_size; j++) {
                sum_nilai_jenis[i] += nilaiMatrix[i][j];
            }
        }

        for (int i = 0; i < matrix_size; i++) {
            prioritas_kriteria_jenis[i] = sum_nilai_jenis[i]/matrix_size;
        }

        for (int i = 0; i < matrix_size; i++) {
            eigen_value_jenis[i] = prioritas_kriteria_jenis[i] * sum_pairwise_jenis[i];
        }

        for (int i = 0; i < matrix_size; i++) {
            total_eigen_value += eigen_value_jenis[i];
        }

        CI = (total_eigen_value-matrix_size)/(matrix_size-1);
        CR = CI/RI;
        Log.i("CR_VALUE_JENIS", "CR = " + CR);
    }

    private void priorityStatus(double[][] pairwiseMatrixStatus) {

    }

    private void priorityMinat(double[][] pairwiseMatrixMinat) {
        int matrix_size = pairwiseMatrixMinat.length;

        double nilaiMatrix[][] = new double[matrix_size][matrix_size];
        double[] sum_pairwise_minat = new double[matrix_size];
        double[] sum_nilai_minat = new double[matrix_size];
        double[] eigen_value_minat = new double[matrix_size];
        double total_eigen_value = 0;

        double CI = 0;
        double RI = 1.12;
        double CR = 0;

        for (int i = 0; i < matrix_size; i++) {
            for (int j = 0; j < matrix_size; j++) {
                if (j < matrix_size) {
                    sum_pairwise_minat[j] += pairwiseMatrixMinat[i][j];
                }
            }
        }

        for(int i=0;i<matrix_size;i++) {
            for(int j=0;j<matrix_size;j++) {
                if(j < matrix_size) {
                    nilaiMatrix[i][j] = pairwiseMatrixMinat[i][j]/sum_pairwise_minat[j];
                }
            }
        }

        for (int i = 0; i < matrix_size; i++) {
            for (int j = 0; j < matrix_size; j++) {
                sum_nilai_minat[i] += nilaiMatrix[i][j];
            }
        }

        for (int i = 0; i < matrix_size; i++) {
            prioritas_kriteria_minat[i] = sum_nilai_minat[i]/matrix_size;
        }

        for (int i = 0; i < matrix_size; i++) {
            eigen_value_minat[i] = prioritas_kriteria_minat[i] * sum_pairwise_minat[i];
        }

        for (int i = 0; i < matrix_size; i++) {
            total_eigen_value += eigen_value_minat[i];
        }

        CI = (total_eigen_value-matrix_size)/(matrix_size-1);
        CR = CI/RI;
        Log.i("CR_VALUE_MINAT", "CR = " + CR);
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
                .radius(25)
                .strokeColor(Color.parseColor("#6495ED")) // Blue color
                .fillColor(Color.parseColor("#6495ED64")) // Green color
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
