package com.ridhojagis.kebunbinatangahp;

import java.sql.Blob;

public class Koleksi {
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
    private double jarak;

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
}

