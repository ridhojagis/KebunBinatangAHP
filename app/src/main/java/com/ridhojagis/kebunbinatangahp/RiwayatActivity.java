package com.ridhojagis.kebunbinatangahp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        databaseHelper = new DatabaseHelper(this);

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
            saveRiwayatKunjungan(riwayatKunjungan);
        }
//        deleteAllRiwayatKunjungan();

//        riwayatKunjungan = getRiwayatKunjungan();

        // Refresh adapter setelah riwayatKunjungan diperbarui
        adapter.notifyDataSetChanged();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        loadRiwayatKunjungan();
    }

    private void loadRiwayatKunjungan() {
        // Baca data dari database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {"id", "nama", "lat", "lng", "waktu", "visited"};
        Cursor cursor = db.query("riwayat_kunjungan", projection, null, null, null, null, null);

        // Bersihkan data riwayat kunjungan sebelum memuat data baru
        riwayatKunjungan.clear();

        // Iterasi cursor dan tambahkan data ke ArrayList riwayatKunjungan
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"));
                String lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"));
                String lng = cursor.getString(cursor.getColumnIndexOrThrow("lng"));
                String waktu = cursor.getString(cursor.getColumnIndexOrThrow("waktu"));
                int visited = cursor.getInt(cursor.getColumnIndexOrThrow("visited"));

                Koleksi koleksi = new Koleksi(nama, lat, lng, waktu, visited == 1);
                koleksi.setId(id);
                riwayatKunjungan.add(koleksi);
            } while (cursor.moveToNext());
        }

        // Tutup cursor dan database
        cursor.close();
        db.close();

        // Refresh adapter setelah riwayatKunjungan diperbarui
        adapter.notifyDataSetChanged();
    }

    private void saveRiwayatKunjungan(ArrayList<Koleksi> riwayatKunjungan) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        for (Koleksi koleksi : riwayatKunjungan) {
            ContentValues values = new ContentValues();
            values.put("nama", koleksi.getNama());
            values.put("waktu", koleksi.getWaktuKunjungan());
            values.put("visited", koleksi.isVisited() ? 1 : 0);

            long id = db.insert("riwayat_kunjungan", null, values);
            koleksi.setId((int) id);
        }

        db.close();

        // Refresh adapter setelah riwayatKunjungan diperbarui
        adapter.notifyDataSetChanged();
    }

    private void deleteRiwayatKunjungan(Koleksi koleksi) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String selection = "id=?";
        String[] selectionArgs = {String.valueOf(koleksi.getId())};

        db.delete("riwayat_kunjungan", selection, selectionArgs);

        db.close();

        // Refresh adapter setelah riwayatKunjungan diperbarui
        adapter.notifyDataSetChanged();
    }

    private void deleteAllRiwayatKunjungan() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.delete("riwayat_kunjungan", null, null);

        db.close();

        // Refresh adapter setelah riwayatKunjungan diperbarui
        adapter.notifyDataSetChanged();
    }
}