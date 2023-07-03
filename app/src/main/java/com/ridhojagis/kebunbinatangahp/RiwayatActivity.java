package com.ridhojagis.kebunbinatangahp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

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
            riwayatKunjungan.add(new Koleksi("Orangutan", "-7.295834", "112.735039", "2023-06-03 08:00:00", true));
        }

        // Refresh adapter setelah riwayatKunjungan diperbarui
        adapter.notifyDataSetChanged();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}