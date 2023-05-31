package com.ridhojagis.kebunbinatangahp;

public class Fasilitas {
    private int id;
    private String nama;
    private String latitude;
    private String longitude;
    private String deskripsi;
    private String jam_buka;
    private String jam_tutup;


    public Fasilitas(){}

    public Fasilitas(int id,String nama, String latitude, String longitude, String deskripsi, String jam_buka, String jam_tutup){
        this.id = id;
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deskripsi = deskripsi;
        this.jam_buka = jam_buka;
        this.jam_tutup = jam_tutup;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String[] getCoordinate(){
        String[] coordinate= {this.latitude, this.longitude};
        return coordinate;
    }

}

