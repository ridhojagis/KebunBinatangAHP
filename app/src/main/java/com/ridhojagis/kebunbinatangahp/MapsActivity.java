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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    double RADIUS = 6378160;

    // AHP
    private double[][] pairwiseMatrix;
    double prioritas_jarak = 0.0;
    double prioritas_jenis = 0.0;
    double prioritas_statusBuka = 0.0;
    double prioritas_minat = 0.0;
    ArrayList<Koleksi> koleksiAHPList;
    ArrayList<Koleksi> koleksiAHPFinal;

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
        final String[] koleksiGoals = new String[1];

        pairwiseMatrix = new double[][]{
                {1.0, 3.0, 0.2, 5.0},   // Matriks perbandingan kriteria jarak
                {0.3333333333, 1.0, 0.1428571429, 3.0},   // Matriks perbandingan kriteria jenis
                {5.0, 7.0, 1.0, 7.0},  // Matriks perbandingan kriteria status buka
                {0.2, 0.3333333333, 0.1428571429, 1.0}  // Matriks perbandingan kriteria minat
        };

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

        btnNavigation.setOnClickListener(new View.OnClickListener() {
            Polyline previousPolyline = null;
            @Override
            public void onClick(View v) {
                isButtonPressed[0] = !isButtonPressed[0]; // Mengubah status tombol saat tombol ditekan
                checkLocationPermission();

                if (isGPSEnabled()) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000, 2, new LocationListener() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onLocationChanged(Location location) {
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

                                            // Fungsi AHP
                                            priorityCriteria(pairwiseMatrix);
                                            Log.i("prioritas_jarak", String.valueOf(prioritas_jarak));
                                            Log.i("prioritas_jenis", String.valueOf(prioritas_jenis));
                                            Log.i("prioritas_statusBuka", String.valueOf(prioritas_statusBuka));
                                            Log.i("prioritas_minat", String.valueOf(prioritas_minat));
                                            koleksiAHPList = new ArrayList<>();
                                            koleksiAHPFinal = new ArrayList<>();
                                            setKoleksiAHPScore(koleksiList, koleksiAHPList, koleksiAHPFinal, koleksiGoals[0]);

                                            List<LatLng> points = new ArrayList<>();

                                            // Periksa apakah ada polyline sebelumnya
                                            if (previousPolyline != null) {
                                                previousPolyline.remove(); // Hapus polyline sebelumnya
                                            }

                                            // Menambahkan polyline koordinat pengguna
                                            points.add(new LatLng(LatLong[0], LatLong[1]));

                                            // Menambahkan polyline koordinat list koleksi hasil AHP
                                            for(int i=0;i<koleksiAHPFinal.size();i++) {
                                                points.add(new LatLng(koleksiAHPFinal.get(i).getLatitude(), koleksiAHPFinal.get(i).getLongitude()));
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

    private void navigateToFormActivity() {
        Intent intent = new Intent(MapsActivity.this, FormActivity.class);

        startActivity(intent);
    }

    private void setKoleksiAHPScore(List<Koleksi> koleksiList, ArrayList<Koleksi> koleksiAHPList, ArrayList<Koleksi> koleksiAHPFinal, String koleksiGoals) {
        double nilaiJarak, nilaiJenis, nilaiStatus, nilaiMinat;
        double skorAHP;
        double jarak;
        String minat;
        for(int i=0;i<koleksiList.size();i++) {
            skorAHP = 0.0;
            jarak = koleksiList.get(i).getJarak();
            minat = koleksiList.get(i).getMinat();

            nilaiJarak = setNilaiJarak(jarak);
            nilaiJenis = 0.283050283050283;
            nilaiStatus = 0.4666666667;
            nilaiMinat = setNilaiMinat(minat);

            skorAHP = prioritas_jarak * nilaiJarak + prioritas_jenis * nilaiJenis + prioritas_statusBuka * nilaiStatus + prioritas_minat * nilaiMinat;
            koleksiList.get(i).setAhp_score(skorAHP);

            String logMessageAHP = "NAMA: " + koleksiList.get(i).getNama()+ ", JARAK: " + koleksiList.get(i).getJarak()+ ", AHP SKOR: " + koleksiList.get(i).getAhp_score();
            Log.i("GET_KOLEKSI_AHP", logMessageAHP);
        }
        // Sort list koleksi berdasarkan skor AHP tertinggi
        Collections.sort(koleksiList, new KoleksiComparator());

        koleksiAHPList.clear();
        int index_tujuan = 0;

        // Menginput koleksi ke dalam list baru hingga menemukan koleksi tujuan
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
        double min_distance = 500;
        // Menginput koleksi yang telah di sort ke dalam list final
        for(int i=0;i<koleksiAHPList.size();i++) {
            double dLat;
            double dLon;
            double distance;
            if(koleksiAHPList.get(i).getNama().equals(koleksiGoals)) {
                continue;
            }
            // Menghitung jarak koleksi terhadap tujuan
            dLat = Math.toRadians(koleksiAHPList.get(i).getLatitude() - koleksiList.get(index_tujuan).getLatitude());
            dLon = Math.toRadians(koleksiAHPList.get(i).getLongitude() - koleksiList.get(index_tujuan).getLongitude());
            distance = RADIUS * 2 *
                    Math.asin(
                            Math.sqrt(
                                    Math.pow(Math.sin(dLat/2),2) + Math.cos(Math.toRadians(koleksiList.get(index_tujuan).getLatitude())) * Math.cos(Math.toRadians(koleksiAHPList.get(i).getLatitude())) * Math.pow(Math.sin(dLon/2),2)));
            if(distance <= min_distance){
                min_distance = distance;
                koleksiAHPFinal.add(koleksiAHPList.get(i));
            }

            String logMessageAHP = "NAMA: " + koleksiAHPList.get(i).getNama()+ ", JARAK: " + koleksiAHPList.get(i).getJarak()+ ", AHP SKOR: " + koleksiAHPList.get(i).getAhp_score();
            Log.i("GET_KOLEKSI_RANGE_SORT", logMessageAHP);
        }
        // Meletakkan koleksi tujuan pada index terakhir dalam list
        koleksiAHPFinal.add(koleksiList.get(index_tujuan));

        for(int i=0;i<koleksiAHPFinal.size();i++) {
            String logMessageAHP = "NAMA: " + koleksiAHPFinal.get(i).getNama()+ ", JARAK: " + koleksiAHPFinal.get(i).getJarak()+ ", AHP SKOR: " + koleksiAHPFinal.get(i).getAhp_score();
            Log.i("GET_KOLEKSI_FINAL", logMessageAHP);
        }
    }

    private double setNilaiMinat(String minat) {
        double nilaiMinat;
        // Nilai Minat
        if(minat.contains("Sangat diminati")) {
            nilaiMinat = 0.4132746574;
        }
        else if(minat.contains("Diminati")) {
            nilaiMinat = 0.2593763138;
        }
        else if(minat.contains("Netral")) {
            nilaiMinat = 0.1591648463;
        }
        else if(minat.contains("Tidak diminati")) {
            nilaiMinat = 0.1097133259;
        }
        else{
            nilaiMinat = 0.0584708566;
        }
        return nilaiMinat;
    }

    private double setNilaiJarak(double jarak) {
        double nilaiJarak;
        if(jarak <= 30.0) {
            nilaiJarak = 0.1850155391; // jarak <=30m
        }
        else if(jarak > 30.0 && jarak <= 60.0) {
            nilaiJarak = 0.1536497571; // jarak 30m-60m
        }
        else if(jarak > 60.0 && jarak <= 90.0) {
            nilaiJarak = 0.1269910538; // jarak 60m-90m
        }
        else if(jarak > 90.0 && jarak <= 120.0) {
            nilaiJarak = 0.1049547207; // jarak 90m-120m
        }
        else if(jarak > 120.0 && jarak <= 150.0) {
            nilaiJarak = 0.08657335996; // jarak 120m-150m
        }
        else if(jarak > 150.0 && jarak <= 180.0) {
            nilaiJarak = 0.07129172186; // jarak 150m-180m
        }
        else if(jarak > 180.0 && jarak <= 210.0) {
            nilaiJarak = 0.05860454529; // jarak 180m-210m
        }
        else if(jarak > 210.0 && jarak <= 240.0) {
            nilaiJarak = 0.04808579014; // jarak 210m-240m
        }
        else if(jarak > 240.0 && jarak <= 270.0) {
            nilaiJarak = 0.03938663928; // jarak 240m-270m
        }
        else if(jarak > 270.0 && jarak <= 300.0) {
            nilaiJarak = 0.0322243846; // jarak 270m-300m
        }
        else if(jarak > 300.0 && jarak <= 330.0) {
            nilaiJarak = 0.02637013988; // jarak 300m-330m
        }
        else if(jarak > 330.0 && jarak <= 360.0) {
            nilaiJarak = 0.02163795589; // jarak 330m-360m
        }
        else if(jarak > 360.0 && jarak <= 390.0) {
            nilaiJarak = 0.01787591641; // jarak 360m-390m
        }
        else if(jarak > 390.0 && jarak <= 420.0) {
            nilaiJarak = 0.01484188065; // jarak 390m-420m
        }
        else{
            nilaiJarak = 0.01249659541; // jarak >420m
        }
        return nilaiJarak;
    }

    private void priorityCriteria(double[][] pairwiseMatrix) {
        int matrix_size = pairwiseMatrix.length;
        double nilaiMatrix[][] = new double[matrix_size][matrix_size];

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

        double CI = (total_eigen_value-matrix_size)/(matrix_size-1);
        double RI = 0.9;
        double CR = CI/RI;
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
//                .strokeColor(Color.argb(255, 255, 0, 0)) // Red color
//                .fillColor(Color.argb(64, 255,0,0))
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
