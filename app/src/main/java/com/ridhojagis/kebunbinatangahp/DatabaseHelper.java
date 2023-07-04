package com.ridhojagis.kebunbinatangahp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "riwayat.db";
    private static final int DATABASE_VERSION = 1;

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
    }
}


