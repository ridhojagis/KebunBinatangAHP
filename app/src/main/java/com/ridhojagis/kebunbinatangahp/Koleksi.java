package com.ridhojagis.kebunbinatangahp;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Blob;

public class Koleksi implements Parcelable {
    private int id;
    private String nama;
    private String latitude;
    private String longitude;
    private String deskripsi;
    private String jam_buka;
    private String jam_tutup;
    private String status_buka;
    private String jenis;
    private String minat;
    private String waktuKunjungan;
    private boolean isVisited;
    private double jarak;
    private double ahp_score;

    public Koleksi() {
    }

    public Koleksi(int id, String nama, String latitude, String longitude, String deskripsi,
                   String jam_buka, String jam_tutup, String status_buka, String jenis,
                   String minat, Blob foto) {
        this.id = id;
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deskripsi = deskripsi;
        this.jam_buka = jam_buka;
        this.jam_tutup = jam_tutup;
        this.status_buka = status_buka;
        this.jenis = jenis;
        this.minat = minat;
    }

    public Koleksi(int id, String nama, String latitude, String longitude) {
        this.id = id;
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Koleksi(int id, String nama, String latitude, String longitude, String waktuKunjungan, boolean isVisited) {
        this.id = id;
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
        this.waktuKunjungan = waktuKunjungan;
        this.isVisited = isVisited;
    }

    // ImplementasiParcelable
    protected Koleksi(Parcel in) {
        // Baca data dari Parcel dan inisialisasi objek Koleksi
        // Sesuaikan dengan tipe data atribut Koleksi Anda
        nama = in.readString();
        deskripsi = in.readString();
        jam_buka = in.readString();
        jam_tutup = in.readString();
        status_buka = in.readString();
        jenis = in.readString();
        minat = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        waktuKunjungan = in.readString();
        isVisited = in.readByte() != 0;
        jarak = in.readDouble();
        ahp_score = in.readDouble();
    }

    // Implementasikan Creator untuk Parcelable
    public static final Creator<Koleksi> CREATOR = new Creator<Koleksi>() {
        @Override
        public Koleksi createFromParcel(Parcel in) {
            return new Koleksi(in);
        }

        @Override
        public Koleksi[] newArray(int size) {
            return new Koleksi[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Tulis data objek Koleksi ke dalam Parcel
        // Sesuaikan dengan tipe data atribut Koleksi Anda
        dest.writeString(nama);
        dest.writeString(deskripsi);
        dest.writeString(jam_buka);
        dest.writeString(jam_tutup);
        dest.writeString(status_buka);
        dest.writeString(jenis);
        dest.writeString(minat);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(waktuKunjungan);
        dest.writeByte((byte) (isVisited ? 1 : 0));
        dest.writeDouble(jarak);
        dest.writeDouble(ahp_score);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Double getLatitude() {
        return Double.valueOf(latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return Double.valueOf(longitude);
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String[] getCoordinate() {
        String[] coordinate = {this.latitude, this.longitude};
        return coordinate;
    }

    public String getJam_buka() {
        return jam_buka;
    }

    public void setJam_buka(String jam_buka) {
        this.jam_buka = jam_buka;
    }

    public String getJam_tutup() {
        return jam_tutup;
    }

    public void setJam_tutup(String jam_tutup) {
        this.jam_tutup = jam_tutup;
    }

    public String getStatus_buka() {
        return status_buka;
    }

    public void setStatus_buka(String status_buka) {
        this.status_buka = status_buka;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public String getMinat() {
        return minat;
    }

    public void setMinat(String minat) {
        this.minat = minat;
    }

    public double getJarak() {
        return jarak;
    }

    public void setJarak(double jarak) {
        this.jarak = jarak;
    }

    public double getAhp_score() { return ahp_score; }

    public void setAhp_score(double ahp_score) { this.ahp_score = ahp_score; }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public String getWaktuKunjungan() {
        return waktuKunjungan;
    }

    public void setWaktuKunjungan(String waktuKunjungan) {
        this.waktuKunjungan = waktuKunjungan;
    }
}

