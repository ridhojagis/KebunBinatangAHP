package com.ridhojagis.kebunbinatangahp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "riwayat.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Buat tabel riwayat kunjungan
        String createTableQuery = "CREATE TABLE riwayat_kunjungan (id INTEGER PRIMARY KEY AUTOINCREMENT, nama TEXT, lat TEXT, lng TEXT, waktu TEXT, visited INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Jika versi database berubah, Anda dapat melakukan operasi upgrade di sini
        db.execSQL("DROP TABLE IF EXISTS riwayat_kunjungan");
        onCreate(db);
    }

    public void updateRiwayatList(Koleksi koleksi) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("visited", koleksi.isVisited() ? 1 : 0);
        values.put("waktu", koleksi.getWaktuKunjungan());

        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(koleksi.getId())};

        db.update("riwayat_kunjungan", values, whereClause, whereArgs);
        Log.d("UPDATE_SQL_SUCCESS", "Berhasil mengupdate data sql");
        Log.d("UPDATE_SQL_SUCCESS", "Nama :" + koleksi.getNama() + " Visited: " + koleksi.isVisited());

        db.close();
    }

    public List<Koleksi> getRiwayatList() {
        List<Koleksi> riwayatList = new ArrayList<>();

        // Query untuk mendapatkan data koleksi dari tabel di database
        String query = "SELECT * FROM riwayat_kunjungan";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // Loop melalui cursor untuk membaca setiap baris data
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"));
                String lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"));
                String lng = cursor.getString(cursor.getColumnIndexOrThrow("lng"));
                String waktu = cursor.getString(cursor.getColumnIndexOrThrow("waktu"));
                int visited = cursor.getInt(cursor.getColumnIndexOrThrow("visited"));
                Koleksi koleksi = new Koleksi(id, nama, lat, lng, waktu, visited == 1);
                // Set atribut-atribut lainnya sesuai dengan kebutuhan Anda
                riwayatList.add(koleksi);
            } while (cursor.moveToNext());
        }

        // Tutup cursor dan database
        cursor.close();
        db.close();

        return riwayatList;
    }
}


