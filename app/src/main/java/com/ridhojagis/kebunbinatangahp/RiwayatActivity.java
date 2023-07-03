package com.ridhojagis.kebunbinatangahp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

public class RiwayatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RiwayatKunjunganAdapter adapter;
    private ArrayList<Koleksi> riwayatKunjungan = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        ImageView backButton = findViewById(R.id.backButton);

        recyclerView = findViewById(R.id.recyclerViewRiwayat);
        adapter = new RiwayatKunjunganAdapter(riwayatKunjungan);
        recyclerView.setAdapter(adapter);

        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra("riwayatKunjungan")) {
            // Mengambil daftar kunjungan dari Intent
            ArrayList<Parcelable> parcelableList = getIntent().getParcelableArrayListExtra("riwayatKunjungan");
            if (parcelableList != null) {
                for (Parcelable parcelable : parcelableList) {
                    if (parcelable instanceof Koleksi) {
                        riwayatKunjungan.add((Koleksi) parcelable);
                    }
                }
            }
        }
        else {
            riwayatKunjungan.add(new Koleksi("Kuda nil kerdil", "-7.295593", "112.734267", "2023-06-01 08:00:00", true));
            riwayatKunjungan.add(new Koleksi("Kuda", "-7.295795", "112.734809", "2023-06-02 08:00:00", false));
            riwayatKunjungan.add(new Koleksi("Orangutan", "-7.295834", "112.735039", "2023-06-03 08:00:00", false));
        }

        saveRiwayatKunjungan(riwayatKunjungan);

        riwayatKunjungan = getRiwayatKunjungan();

        // Refresh adapter setelah riwayatKunjungan diperbarui
        adapter.notifyDataSetChanged();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    // Simpan daftar kunjungan ke dalam Shared Preferences
    private void saveRiwayatKunjungan(ArrayList<Koleksi> riwayatKunjungan) {
        SharedPreferences sharedPreferences = getSharedPreferences("RiwayatPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonKunjungan = gson.toJson(riwayatKunjungan);

        editor.putString("riwayatKunjungan", jsonKunjungan);
        editor.apply();
        Log.i("PREFERENCED_SAVED", "Riwayat kunjungan berhasil disimpan");
    }

    // Ambil daftar kunjungan dari Shared Preferences
    private ArrayList<Koleksi> getRiwayatKunjungan() {
        SharedPreferences sharedPreferences = getSharedPreferences("RiwayatPrefs", MODE_PRIVATE);

        String jsonKunjungan = sharedPreferences.getString("riwayatKunjungan", "");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Koleksi>>() {}.getType();
        ArrayList<Koleksi> riwayatKunjungan = gson.fromJson(jsonKunjungan, type);

        if (riwayatKunjungan == null) {
            riwayatKunjungan = new ArrayList<>();
        }

        return riwayatKunjungan;
    }
}