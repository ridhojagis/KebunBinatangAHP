package com.ridhojagis.kebunbinatangahp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class RiwayatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RiwayatKunjunganAdapter adapter;
    private ArrayList<Koleksi> riwayatKunjungan = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        ImageView backButton = findViewById(R.id.backButton);

        recyclerView = findViewById(R.id.recyclerViewRiwayat);
        adapter = new RiwayatKunjunganAdapter(riwayatKunjungan);
        recyclerView.setAdapter(adapter);

        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        riwayatKunjungan.add(new Koleksi("Kuda nil kerdil", "-7.295593", "112.734267", "2023-06-01 08:00:00", true));
        riwayatKunjungan.add(new Koleksi("Kuda", "-7.295795", "112.734809", "2023-06-02 08:00:00", false));
        riwayatKunjungan.add(new Koleksi("Orangutan", "-7.295834", "112.735039", "2023-06-03 08:00:00", true));

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